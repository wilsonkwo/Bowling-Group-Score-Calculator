package sg.sports.bowling.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtAuthFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void missingHeaderSkipsFilter() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        UserDetailsService uds = mock(UserDetailsService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, uds);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidTokenSkipsAuthentication() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        when(jwt.extractUsername(anyString())).thenThrow(new RuntimeException("bad token"));
        UserDetailsService uds = mock(UserDetailsService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, uds);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer tok");

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void validTokenSetsAuthentication() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        UserDetails user = User.withUsername("bob").password("x").roles("USER").build();
        when(jwt.extractUsername(anyString())).thenReturn("bob");
        when(jwt.validateToken(anyString(), any())).thenReturn(true);
        UserDetailsService uds = mock(UserDetailsService.class);
        when(uds.loadUserByUsername("bob")).thenReturn(user);

        JwtAuthFilter filter = new JwtAuthFilter(jwt, uds);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer tok");

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
