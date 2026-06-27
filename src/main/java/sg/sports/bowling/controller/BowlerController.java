package sg.sports.bowling.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sg.sports.bowling.entity.Bowler;
import sg.sports.bowling.service.BowlerService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bowlers")
@RequiredArgsConstructor
public class BowlerController {

    private final BowlerService bowlerService;

    @GetMapping
    public ResponseEntity<List<Bowler>> getAllBowlers() {
        return ResponseEntity.ok(bowlerService.getAllBowlers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bowler> getBowler(@PathVariable Long id) {
        return ResponseEntity.ok(bowlerService.getBowler(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Bowler> createBowler(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bowlerService.createBowler(name));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Bowler> updateBowler(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(bowlerService.updateBowler(id, body.get("name")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBowler(@PathVariable Long id) {
        bowlerService.deleteBowler(id);
        return ResponseEntity.noContent().build();
    }
}
