package sg.sports.bowling.security;

import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void generateAndValidateToken() {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "jwtSecret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(util, "jwtExpirationMs", 3600000L);

        UserDetails user = User.withUsername("alice").password("pw").roles("USER").build();
        String token = util.generateToken(user);
        assertNotNull(token);

        String username = util.extractUsername(token);
        assertEquals("alice", username);
        assertTrue(util.validateToken(token, user));

        // malformed token should return false from validateToken
        assertFalse(util.validateToken(token + "x", user));
    }
}
