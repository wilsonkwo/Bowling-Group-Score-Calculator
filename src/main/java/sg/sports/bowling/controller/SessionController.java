package sg.sports.bowling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sg.sports.bowling.dto.request.CreateSessionRequest;
import sg.sports.bowling.entity.BowlingSession;
import sg.sports.bowling.entity.Game;
import sg.sports.bowling.service.SessionService;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Manage bowling sessions and their games")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @Operation(summary = "List all sessions (newest first)", responses = {
            @ApiResponse(responseCode = "200", description = "List of sessions"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<List<BowlingSession>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @GetMapping("/open")
    @Operation(summary = "List open sessions (newest first)", responses = {
            @ApiResponse(responseCode = "200", description = "List of open sessions"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<List<BowlingSession>> getOpenSessions() {
        return ResponseEntity.ok(sessionService.getOpenSessions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a session by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Session found"),
            @ApiResponse(responseCode = "400", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<BowlingSession> getSession(
            @Parameter(description = "Session ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSession(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a session (admin only)",
            description = "The combination of sessionDate + timeSlot must be unique.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Session created with status OPEN"),
                    @ApiResponse(responseCode = "400", description = "Validation error or duplicate date/slot"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
                    @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
            })
    public ResponseEntity<BowlingSession> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.ok(sessionService.createSession(
                request.getSessionDate(),
                request.getLocation(),
                request.getNotes(),
                request.getTimeSlot()));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close a session (admin only)", responses = {
            @ApiResponse(responseCode = "200", description = "Session status set to CLOSED"),
            @ApiResponse(responseCode = "400", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    public ResponseEntity<BowlingSession> closeSession(
            @Parameter(description = "Session ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.closeSession(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a session and all its games (admin only)",
            description = "Cascades: removes all games, bowler_game records, and frames for this session.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Session and all related data deleted"),
                    @ApiResponse(responseCode = "400", description = "Session not found"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
                    @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
            })
    public ResponseEntity<Void> deleteSession(
            @Parameter(description = "Session ID", required = true) @PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/games")
    @Operation(summary = "List games in a session", responses = {
            @ApiResponse(responseCode = "200", description = "List of games ordered by game number"),
            @ApiResponse(responseCode = "400", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<List<Game>> getGames(
            @Parameter(description = "Session ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getGamesForSession(id));
    }

    @PostMapping("/{id}/games")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a game to a session (admin only)", responses = {
            @ApiResponse(responseCode = "200", description = "Game created with auto-incremented game number"),
            @ApiResponse(responseCode = "400", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    public ResponseEntity<Game> addGame(
            @Parameter(description = "Session ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.addGame(id));
    }
}
