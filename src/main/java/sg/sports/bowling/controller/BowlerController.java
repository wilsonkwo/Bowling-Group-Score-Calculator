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
import sg.sports.bowling.dto.request.CreateBowlerRequest;
import sg.sports.bowling.dto.request.UpdateBowlerRequest;
import sg.sports.bowling.entity.Bowler;
import sg.sports.bowling.service.BowlerService;

import java.util.List;

@RestController
@RequestMapping("/api/bowlers")
@RequiredArgsConstructor
@Tag(name = "Bowlers", description = "Manage bowler profiles")
public class BowlerController {

    private final BowlerService bowlerService;

    @GetMapping
    @Operation(summary = "List all bowlers", responses = {
            @ApiResponse(responseCode = "200", description = "List of all bowlers"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<List<Bowler>> getAllBowlers() {
        return ResponseEntity.ok(bowlerService.getAllBowlers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a bowler by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Bowler found"),
            @ApiResponse(responseCode = "400", description = "Bowler not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<Bowler> getBowler(
            @Parameter(description = "Bowler ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(bowlerService.getBowler(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a bowler (admin only)", responses = {
            @ApiResponse(responseCode = "200", description = "Bowler created"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate name"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    public ResponseEntity<Bowler> createBowler(@Valid @RequestBody CreateBowlerRequest request) {
        return ResponseEntity.ok(bowlerService.createBowler(request.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a bowler's name (admin only)", responses = {
            @ApiResponse(responseCode = "200", description = "Bowler updated"),
            @ApiResponse(responseCode = "400", description = "Validation error or bowler not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    public ResponseEntity<Bowler> updateBowler(
            @Parameter(description = "Bowler ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateBowlerRequest request) {
        return ResponseEntity.ok(bowlerService.updateBowler(id, request.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a bowler (admin only)", description = "Rejected with 400 if the bowler has existing game history.", responses = {
            @ApiResponse(responseCode = "204", description = "Bowler deleted"),
            @ApiResponse(responseCode = "400", description = "Bowler has game history and cannot be deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    public ResponseEntity<Void> deleteBowler(
            @Parameter(description = "Bowler ID", required = true) @PathVariable Long id) {
        bowlerService.deleteBowler(id);
        return ResponseEntity.noContent().build();
    }
}
