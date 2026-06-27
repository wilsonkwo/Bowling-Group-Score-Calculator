package sg.sports.bowling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sg.sports.bowling.dto.request.FrameSubmitRequest;
import sg.sports.bowling.dto.response.FrameResponse;
import sg.sports.bowling.dto.response.LeaderboardEntry;
import sg.sports.bowling.dto.response.ParticipantResponse;
import sg.sports.bowling.entity.Bowler;
import sg.sports.bowling.entity.BowlerGame;
import sg.sports.bowling.entity.Frame;
import sg.sports.bowling.service.ScoreService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    /**
     * Submit all frames for a bowler in a game.
     * POST /api/scores/frames
     */
    @PostMapping("/frames")
    public ResponseEntity<List<FrameResponse>> submitFrames(
            @Valid @RequestBody FrameSubmitRequest request) {

        // Convert List<List<Integer>> to List<int[]>
        List<int[]> balls = request.getFrames().stream()
                .map(frame -> frame.stream().mapToInt(Integer::intValue).toArray())
                .collect(Collectors.toList());

        scoreService.saveFrames(request.getBowlerId(), request.getGameId(), balls);

        List<Frame> saved = scoreService.getFrames(request.getBowlerId(), request.getGameId());
        return ResponseEntity.ok(saved.stream().map(FrameResponse::from).toList());
    }

    /**
     * Get a bowler's frames for a game.
     * GET /api/scores/frames?bowlerId=1&gameId=2
     */
    @GetMapping("/frames")
    public ResponseEntity<List<FrameResponse>> getFrames(
            @RequestParam Long bowlerId,
            @RequestParam Long gameId) {

        List<Frame> frames = scoreService.getFrames(bowlerId, gameId);
        return ResponseEntity.ok(frames.stream().map(FrameResponse::from).toList());
    }

    /**
     * List every bowler entered in a game so far, with their frames.
     * Used to resume frame-by-frame entry for a game already in progress.
     * GET /api/scores/games/{gameId}/participants
     */
    @GetMapping("/games/{gameId}/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable Long gameId) {
        return ResponseEntity.ok(scoreService.getParticipants(gameId));
    }

    /**
     * Get leaderboard (total game points) for a session.
     * GET /api/scores/leaderboard?sessionId=1
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@RequestParam Long sessionId) {
        List<Object[]> raw = scoreService.getSessionLeaderboard(sessionId);
        List<LeaderboardEntry> entries = raw.stream()
                .map(row -> {
                    Bowler b = (Bowler) row[0];
                    double pts = ((Number) row[1]).doubleValue();
                    return new LeaderboardEntry(b.getId(), b.getName(), pts);
                })
                .toList();
        return ResponseEntity.ok(entries);
    }
}
