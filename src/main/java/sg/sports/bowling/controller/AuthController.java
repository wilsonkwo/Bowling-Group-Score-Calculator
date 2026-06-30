package sg.sports.bowling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import sg.sports.bowling.dto.request.ChangePasswordRequest;
import sg.sports.bowling.dto.request.LoginRequest;
import sg.sports.bowling.dto.request.RegisterRequest;
import sg.sports.bowling.security.JwtUtil;
import sg.sports.bowling.service.UserService;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Register, login, refresh token, and change password")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Obtain a JWT token", description = "No prior authentication required.", responses = {
            @ApiResponse(responseCode = "200", description = "Returns token, type, username, and roles"),
            @ApiResponse(responseCode = "400", description = "Validation error (blank username/password)"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(buildTokenResponse(token, userDetails));
    }

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Register a new user account", description = "No prior authentication required. New users receive ROLE_USER.", responses = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or username/email already taken")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh the JWT token", description = "Supply the current (still-valid) token; receive a new one with reset expiry.", responses = {
            @ApiResponse(responseCode = "200", description = "New token issued"),
            @ApiResponse(responseCode = "401", description = "Missing or expired token")
    })
    public ResponseEntity<?> refresh(@AuthenticationPrincipal UserDetails userDetails) {
        String newToken = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(buildTokenResponse(newToken, userDetails));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile", responses = {
            @ApiResponse(responseCode = "200", description = "Returns username and roles"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        ));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change the authenticated user's password", responses = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or wrong current password"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                             @Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> buildTokenResponse(String token, UserDetails userDetails) {
        return Map.of(
                "token", token,
                "type", "Bearer",
                "username", userDetails.getUsername(),
                "roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
    }
}
