package sg.sports.bowling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Scores", description = "Submit and retrieve frame-by-frame scores and leaderboards")
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping("/frames")
    @Operation(summary = "Submit frames for a bowler in a game",
            description = "Re-submitting replaces all previously saved frames for that bowler/game pair. Triggers score and win/loss recalculation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Frames saved; returns updated frame list with scores"),
                    @ApiResponse(responseCode = "400", description = "Validation error, invalid bowler/game ID, or illegal frame data"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
            })
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

    @GetMapping("/frames")
    @Operation(summary = "Get a bowler's frames for a game", responses = {
            @ApiResponse(responseCode = "200", description = "List of frames ordered by frame number"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<List<FrameResponse>> getFrames(
            @Parameter(description = "Bowler ID", required = true) @RequestParam Long bowlerId,
            @Parameter(description = "Game ID", required = true) @RequestParam Long gameId) {

        List<Frame> frames = scoreService.getFrames(bowlerId, gameId);
        return ResponseEntity.ok(frames.stream().map(FrameResponse::from).toList());
    }

    @GetMapping("/games/{gameId}/participants")
    @Operation(summary = "List participants in a game with their scores",
            description = "Returns every bowler entered in the game so far, with total score, game points, and result. Used to resume frame entry.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of participants"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
            })
    public ResponseEntity<List<ParticipantResponse>> getParticipants(
            @Parameter(description = "Game ID", required = true) @PathVariable Long gameId) {
        return ResponseEntity.ok(scoreService.getParticipants(gameId));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get the leaderboard for a session",
            description = "Returns bowlers ranked by total game points accumulated across all games in the session.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ranked list of bowlers with total points"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
            })
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(
            @Parameter(description = "Session ID", required = true) @RequestParam Long sessionId) {
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
