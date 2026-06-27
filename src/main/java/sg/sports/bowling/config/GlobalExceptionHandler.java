package sg.sports.bowling.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Translates unguarded service-layer exceptions into proper JSON error responses.
 * Without this, an uncaught exception falls through to Spring Boot's default error
 * forwarding to "/error", which is itself behind the security filter chain and has
 * no Authorization header on the internal forward — masking the real error as a
 * misleading 401 "Full authentication is required".
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }
}
