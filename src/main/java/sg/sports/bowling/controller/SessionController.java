package sg.sports.bowling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sg.sports.bowling.entity.BowlingSession;
import sg.sports.bowling.entity.Game;
import sg.sports.bowling.service.SessionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<BowlingSession>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @GetMapping("/open")
    public ResponseEntity<List<BowlingSession>> getOpenSessions() {
        return ResponseEntity.ok(sessionService.getOpenSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BowlingSession> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSession(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BowlingSession> createSession(@RequestBody Map<String, String> body) {
        LocalDate date = LocalDate.parse(body.get("sessionDate"));
        String location = body.get("location");
        String notes = body.get("notes");
        return ResponseEntity.ok(sessionService.createSession(date, location, notes));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BowlingSession> closeSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.closeSession(id));
    }

    @GetMapping("/{id}/games")
    public ResponseEntity<List<Game>> getGames(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getGamesForSession(id));
    }

    @PostMapping("/{id}/games")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Game> addGame(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.addGame(id));
    }
}
