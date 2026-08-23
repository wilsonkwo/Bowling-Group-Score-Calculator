package sg.sports.bowling.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RequestLoggingFilterTest {

    @Test
    void logsAndDelegates() throws Exception {
        RequestLoggingFilter f = new RequestLoggingFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getMethod()).thenReturn("GET");
        when(req.getRequestURI()).thenReturn("/api/test");
        when(req.getQueryString()).thenReturn(null);
        when(res.getStatus()).thenReturn(200);

        f.doFilterInternal(req, res, chain);
        verify(chain).doFilter(req, res);
    }

    @Test
    void shouldNotFilterSkipsSwagger() {
        RequestLoggingFilter f = new RequestLoggingFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/swagger-ui/index.html");
        assertTrue(f.shouldNotFilter(req));
    }
}
