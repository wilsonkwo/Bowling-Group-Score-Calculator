package sg.sports.bowling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.sports.bowling.entity.Bowler;
import sg.sports.bowling.entity.BowlerGame;
import sg.sports.bowling.entity.BowlerGame.GameResult;
import sg.sports.bowling.entity.Frame;
import sg.sports.bowling.entity.Game;
import sg.sports.bowling.repository.BowlerGameRepository;
import sg.sports.bowling.repository.BowlerRepository;
import sg.sports.bowling.repository.FrameRepository;
import sg.sports.bowling.repository.GameRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock
    private BowlerGameRepository bowlerGameRepository;
    @Mock
    private FrameRepository frameRepository;
    @Mock
    private BowlerRepository bowlerRepository;
    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private ScoreService scoreService;

    @Captor
    private ArgumentCaptor<List<Frame>> framesCaptor;

    private Bowler bowler;
    private Game game;
    private BowlerGame bowlerGame;

    @BeforeEach
    void setUp() {
        bowler = Bowler.builder().id(1L).name("Alice").build();
        game = Game.builder().id(10L).gameNumber(1).build();
        bowlerGame = BowlerGame.builder().id(100L).bowler(bowler).game(game).build();

        lenient().when(bowlerRepository.findById(1L)).thenReturn(Optional.of(bowler));
        lenient().when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        lenient().when(bowlerGameRepository.findByBowlerAndGame(bowler, game)).thenReturn(Optional.of(bowlerGame));
        lenient().when(bowlerGameRepository.save(any(BowlerGame.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(bowlerGameRepository.findByGame(game)).thenReturn(List.of(bowlerGame));
    }

    private List<int[]> allOpenFrames() {
        // 9 frames of [3,4] (open, no spare/strike) + a 10th frame of [3,4]
        List<int[]> frames = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            frames.add(new int[]{3, 4});
        }
        return frames;
    }

    @Test
    void perfectGameScoresThreeHundred() {
        List<int[]> balls = new ArrayList<>();
        for (int i = 0; i < 9; i++) balls.add(new int[]{10});
        balls.add(new int[]{10, 10, 10});

        scoreService.saveFrames(1L, 10L, balls);

        assertThat(bowlerGame.getTotalScore()).isEqualTo(300);
    }

    @Test
    void strikeFrameEarnsTwoFramePoints() {
        List<int[]> balls = allOpenFrames();
        balls.set(0, new int[]{10}); // frame 1 is a strike
        balls.set(1, new int[]{0, 0}); // avoid lookahead score confusion

        scoreService.saveFrames(1L, 10L, balls);

        Frame strikeFrame = capturedFrames().get(0);
        assertThat(strikeFrame.isStrike()).isTrue();
        assertThat(strikeFrame.getFramePoints()).isEqualTo(2.0);
    }

    @Test
    void spareFrameEarnsOneFramePoint() {
        List<int[]> balls = allOpenFrames();
        balls.set(0, new int[]{6, 4}); // spare in frame 1

        scoreService.saveFrames(1L, 10L, balls);

        Frame spareFrame = capturedFrames().get(0);
        assertThat(spareFrame.isSpare()).isTrue();
        assertThat(spareFrame.getFramePoints()).isEqualTo(1.0);
    }

    @Test
    void openFrameEarnsNoFramePoints() {
        scoreService.saveFrames(1L, 10L, allOpenFrames());

        for (Frame frame : capturedFrames()) {
            assertThat(frame.getFramePoints()).isEqualTo(0.0);
        }
    }

    @Test
    void soleWinnerGetsFixedThreePointBonusPlusFramePoints() {
        // Alice bowls a spare in frame 1 (76 total); Bob's recorded score is lower, so Alice wins outright.
        // Both have all 10 frames in (game complete), so the win bonus is actually decided.
        Bowler bob = Bowler.builder().id(2L).name("Bob").build();
        BowlerGame bobGame = BowlerGame.builder().id(101L).bowler(bob).game(game)
                .totalScore(50).build();
        when(bowlerGameRepository.findByGame(game)).thenReturn(Arrays.asList(bowlerGame, bobGame));
        when(frameRepository.findByBowlerGameOrderByFrameNumberAsc(bowlerGame))
                .thenReturn(completeFrames(1.0));
        when(frameRepository.findByBowlerGameOrderByFrameNumberAsc(bobGame))
                .thenReturn(completeFrames(0.0));

        List<int[]> balls = allOpenFrames();
        balls.set(0, new int[]{6, 4});
        scoreService.saveFrames(1L, 10L, balls);

        assertThat(bowlerGame.getResult()).isEqualTo(GameResult.WIN);
        assertThat(bowlerGame.getGamePoints()).isEqualTo(1.0 + 3.0);
        assertThat(bobGame.getResult()).isEqualTo(GameResult.LOSS);
        assertThat(bobGame.getGamePoints()).isEqualTo(0.0);
    }

    @Test
    void tiedWinnersSplitTheThreePointBonusEvenly() {
        Bowler bob = Bowler.builder().id(2L).name("Bob").build();
        BowlerGame bobGame = BowlerGame.builder().id(101L).bowler(bob).game(game).build();
        when(bowlerGameRepository.findByGame(game)).thenReturn(Arrays.asList(bowlerGame, bobGame));
        when(frameRepository.findByBowlerGameOrderByFrameNumberAsc(any(BowlerGame.class)))
                .thenReturn(completeFrames(0.0));

        // Both bowl the same open frames -> same total score -> tie
        scoreService.saveFrames(1L, 10L, allOpenFrames());

        when(bowlerRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(bowlerGameRepository.findByBowlerAndGame(bob, game)).thenReturn(Optional.of(bobGame));
        scoreService.saveFrames(2L, 10L, allOpenFrames());

        assertThat(bowlerGame.getResult()).isEqualTo(GameResult.DRAW);
        assertThat(bowlerGame.getGamePoints()).isEqualTo(1.5);
        assertThat(bobGame.getResult()).isEqualTo(GameResult.DRAW);
        assertThat(bobGame.getGamePoints()).isEqualTo(1.5);
    }

    @Test
    void getParticipantsReturnsEveryBowlerInTheGameWithTheirFrames() {
        Bowler bob = Bowler.builder().id(2L).name("Bob").build();
        BowlerGame bobGame = BowlerGame.builder().id(101L).bowler(bob).game(game)
                .totalScore(70).build();
        when(bowlerGameRepository.findByGame(game)).thenReturn(Arrays.asList(bowlerGame, bobGame));
        when(frameRepository.findByBowlerGameOrderByFrameNumberAsc(bowlerGame))
                .thenReturn(List.of(frameWithPoints(2.0)));
        when(frameRepository.findByBowlerGameOrderByFrameNumberAsc(bobGame))
                .thenReturn(List.of(frameWithPoints(0.0)));

        List<sg.sports.bowling.dto.response.ParticipantResponse> participants = scoreService.getParticipants(10L);

        assertThat(participants).hasSize(2);
        assertThat(participants.get(0).getBowlerId()).isEqualTo(1L);
        assertThat(participants.get(0).getBowlerName()).isEqualTo("Alice");
        assertThat(participants.get(0).getFrames()).hasSize(1);
        assertThat(participants.get(1).getBowlerId()).isEqualTo(2L);
        assertThat(participants.get(1).getTotalScore()).isEqualTo(70);
    }

    private Frame frameWithPoints(double points) {
        Frame f = Frame.builder().frameNumber(1).build();
        f.setFramePoints(points);
        return f;
    }

    /** A full 10-frame, all-open-frames game (so isGameComplete sees it as finished), carrying framePoints on the 10th frame. */
    private List<Frame> completeFrames(double framePoints) {
        List<Frame> frames = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            frames.add(Frame.builder().frameNumber(i).ball1(3).ball2(4).build());
        }
        Frame last = Frame.builder().frameNumber(10).ball1(3).ball2(4).build();
        last.setFramePoints(framePoints);
        frames.add(last);
        return frames;
    }

    private void verifyFramesSaved() {
        org.mockito.Mockito.verify(frameRepository).saveAll(framesCaptor.capture());
    }

    private List<Frame> capturedFrames() {
        verifyFramesSaved();
        return framesCaptor.getValue();
    }
}
