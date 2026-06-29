package sg.sports.bowling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.sports.bowling.dto.response.FrameResponse;
import sg.sports.bowling.dto.response.ParticipantResponse;
import sg.sports.bowling.entity.*;
import sg.sports.bowling.entity.BowlerGame.GameResult;
import sg.sports.bowling.repository.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private static final double SPARE_POINTS = 1.0;
    private static final double STRIKE_POINTS = 2.0;
    private static final double GAME_WIN_POINTS = 3.0;

    private final BowlerGameRepository bowlerGameRepository;
    private final FrameRepository frameRepository;
    private final BowlerRepository bowlerRepository;
    private final GameRepository gameRepository;

    /**
     * Save or replace a bowler's complete set of frames for a game.
     * Recalculates scores and win/loss results for all bowlers in the game.
     */
    @Transactional
    public BowlerGame saveFrames(Long bowlerId, Long gameId, List<int[]> balls) {
        Bowler bowler = bowlerRepository.findById(bowlerId)
                .orElseThrow(() -> new IllegalArgumentException("Bowler not found: " + bowlerId));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        BowlerGame bowlerGame = bowlerGameRepository.findByBowlerAndGame(bowler, game)
                .orElseGet(() -> BowlerGame.builder().bowler(bowler).game(game).build());
        bowlerGame = bowlerGameRepository.save(bowlerGame);

        // Remove existing frames then rebuild. The flush forces the DELETE to hit the
        // database before the new frames are inserted — without it, Hibernate may queue
        // both for the same flush in an order that violates the (bowler_game_id, frame_number)
        // unique constraint when a frame_number is being replaced.
        frameRepository.deleteAllByBowlerGame(bowlerGame);
        frameRepository.flush();

        List<Frame> frames = buildFrames(bowlerGame, balls);
        frameRepository.saveAll(frames);

        // Update total score
        int total = frames.stream().mapToInt(f -> f.getFrameScore() != null ? f.getFrameScore() : 0).sum();
        bowlerGame.setTotalScore(total);
        bowlerGameRepository.save(bowlerGame);

        // Recalculate win/loss for everyone in this game
        recalculateResults(game);

        return bowlerGame;
    }

    public List<Frame> getFrames(Long bowlerId, Long gameId) {
        Bowler bowler = bowlerRepository.findById(bowlerId)
                .orElseThrow(() -> new IllegalArgumentException("Bowler not found: " + bowlerId));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        BowlerGame bowlerGame = bowlerGameRepository.findByBowlerAndGame(bowler, game)
                .orElseThrow(() -> new IllegalArgumentException("No score entry found"));
        return frameRepository.findByBowlerGameOrderByFrameNumberAsc(bowlerGame);
    }

    public List<Object[]> getSessionLeaderboard(Long sessionId) {
        return bowlerGameRepository.findSessionLeaderboard(sessionId);
    }

    /**
     * List every bowler currently entered in a game, with their frames so far.
     * Used to resume frame-by-frame entry for a game already in progress.
     */
    public List<ParticipantResponse> getParticipants(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        return bowlerGameRepository.findByGame(game).stream()
                .map(bg -> ParticipantResponse.builder()
                        .bowlerId(bg.getBowler().getId())
                        .bowlerName(bg.getBowler().getName())
                        .totalScore(bg.getTotalScore())
                        .gamePoints(bg.getGamePoints())
                        .result(bg.getResult())
                        .frames(frameRepository.findByBowlerGameOrderByFrameNumberAsc(bg).stream()
                                .map(FrameResponse::from)
                                .toList())
                        .build())
                .toList();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Build Frame entities from raw ball data.
     * balls: list of int[] per frame — [ball1], [ball1, ball2], or [ball1, ball2, ball3] for 10th.
     */
    private List<Frame> buildFrames(BowlerGame bowlerGame, List<int[]> balls) {
        List<Frame> frames = new ArrayList<>();
        int cumulative = 0;

        for (int i = 0; i < Math.min(balls.size(), 10); i++) {
            int[] b = balls.get(i);
            int frameNumber = i + 1;
            boolean isTenth = frameNumber == 10;

            Frame.FrameBuilder fb = Frame.builder()
                    .bowlerGame(bowlerGame)
                    .frameNumber(frameNumber)
                    .ball1(b.length > 0 ? b[0] : null)
                    .ball2(b.length > 1 ? b[1] : null)
                    .ball3(isTenth && b.length > 2 ? b[2] : null);

            int frameScore = calculateFrameScore(balls, i);
            cumulative += frameScore;

            Frame frame = fb.frameScore(frameScore).cumulativeScore(cumulative).build();
            frame.setFramePoints(framePointsFor(frame));
            frames.add(frame);
        }
        return frames;
    }

    /**
     * Standard bowling frame score with strike/spare look-ahead.
     */
    private int calculateFrameScore(List<int[]> balls, int frameIndex) {
        // Flatten all balls into a single list for look-ahead
        List<Integer> flat = new ArrayList<>();
        for (int[] b : balls) {
            for (int pin : b) flat.add(pin);
        }

        // Find the starting index in the flat list for this frame
        int pos = 0;
        for (int i = 0; i < frameIndex; i++) {
            int[] b = balls.get(i);
            pos += (b[0] == 10 && i < 9) ? 1 : b.length; // strike in frames 1-9 = 1 ball
        }

        if (frameIndex == 9) {
            // 10th frame: sum all balls thrown
            int[] b = balls.get(9);
            int score = 0;
            for (int pin : b) score += pin;
            return score;
        }

        int b1 = pos < flat.size() ? flat.get(pos) : 0;
        int b2 = pos + 1 < flat.size() ? flat.get(pos + 1) : 0;
        int b3 = pos + 2 < flat.size() ? flat.get(pos + 2) : 0;

        if (b1 == 10) return 10 + b2 + b3; // strike
        if (b1 + b2 == 10) return 10 + b3; // spare
        return b1 + b2;
    }

    /**
     * Points for a single frame: 1 for a spare, 2 for a strike, 0 otherwise.
     */
    private double framePointsFor(Frame frame) {
        if (frame.isStrike()) return STRIKE_POINTS;
        if (frame.isSpare()) return SPARE_POINTS;
        return 0.0;
    }

    /**
     * After all bowlers' scores are in for a game, determine win/loss and assign points.
     * Points = sum of this bowler's frame points (spares/strikes) + a fixed 3-point bonus
     * for the game winner, split evenly if multiple bowlers tie for the highest score.
     *
     * Win/loss/draw is only decided once every participant has finished all 10 frames —
     * this runs after every single frame save (see saveFrames), so comparing partial
     * totals mid-game would hand out a misleading early win bonus (e.g. two bowlers tied
     * on just frame 1 would each show a "DRAW" with a split bonus, well before the game
     * is actually over). Until then, gamePoints is just the frame points earned so far.
     */
    private void recalculateResults(Game game) {
        List<BowlerGame> participants = bowlerGameRepository.findByGame(game);
        if (participants.isEmpty()) return;

        boolean allComplete = participants.stream()
                .allMatch(bg -> isGameComplete(frameRepository.findByBowlerGameOrderByFrameNumberAsc(bg)));

        int maxScore = participants.stream()
                .filter(bg -> bg.getTotalScore() != null)
                .mapToInt(BowlerGame::getTotalScore)
                .max()
                .orElse(0);

        long winnersCount = participants.stream()
                .filter(bg -> bg.getTotalScore() != null && bg.getTotalScore() == maxScore)
                .count();

        double winShare = GAME_WIN_POINTS / winnersCount;

        for (BowlerGame bg : participants) {
            if (bg.getTotalScore() == null) continue;

            double framePoints = frameRepository.findByBowlerGameOrderByFrameNumberAsc(bg).stream()
                    .mapToDouble(Frame::getFramePoints)
                    .sum();

            if (!allComplete) {
                bg.setResult(null);
                bg.setGamePoints(framePoints);
            } else if (bg.getTotalScore() == maxScore) {
                bg.setResult(winnersCount > 1 ? GameResult.DRAW : GameResult.WIN);
                bg.setGamePoints(framePoints + winShare);
            } else {
                bg.setResult(GameResult.LOSS);
                bg.setGamePoints(framePoints);
            }
        }
        bowlerGameRepository.saveAll(participants);
    }

    /** Whether a bowler has thrown every ball they're entitled to across all 10 frames. */
    private boolean isGameComplete(List<Frame> frames) {
        if (frames.size() != 10) return false;
        Frame tenth = frames.get(9);
        if (tenth.getBall1() == null) return false;
        boolean strikeOrSpare = tenth.getBall1() == 10
                || (tenth.getBall2() != null && tenth.getBall1() + tenth.getBall2() == 10);
        return strikeOrSpare ? tenth.getBall3() != null : tenth.getBall2() != null;
    }
}
