package sg.sports.bowling.controller;

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
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    /**
     * POST /api/auth/login
     * Body: { "username": "...", "password": "..." }
     * Returns: { "token": "<jwt>" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(buildTokenResponse(token, userDetails));
    }

    /**
     * POST /api/auth/register
     * Body: { "username": "...", "email": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    /**
     * POST /api/auth/refresh
     * Header: Authorization: Bearer <current-valid-token>
     * Returns a fresh token with a reset expiry — no need to re-enter credentials.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@AuthenticationPrincipal UserDetails userDetails) {
        String newToken = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(buildTokenResponse(newToken, userDetails));
    }

    /**
     * GET /api/auth/me
     * Header: Authorization: Bearer <token>
     * Returns the current user's username and roles.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        ));
    }

    /**
     * POST /api/auth/change-password
     * Header: Authorization: Bearer <token>
     * Body: { "currentPassword": "...", "newPassword": "..." }
     */
    @PostMapping("/change-password")
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
