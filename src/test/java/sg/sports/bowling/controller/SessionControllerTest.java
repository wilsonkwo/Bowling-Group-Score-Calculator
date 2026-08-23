package sg.sports.bowling.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import sg.sports.bowling.entity.BowlingSession;
import sg.sports.bowling.entity.Game;
import sg.sports.bowling.service.SessionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionControllerTest {

    @Test
    void listAndGameEndpoints() {
        SessionService svc = mock(SessionService.class);
        SessionController c = new SessionController(svc);

        when(svc.getAllSessions()).thenReturn(List.of(BowlingSession.builder().id(1L).build()));
        ResponseEntity<java.util.List<BowlingSession>> resp = c.getAllSessions();
        assertEquals(200, resp.getStatusCodeValue());

        when(svc.getSession(1L)).thenReturn(BowlingSession.builder().id(1L).build());
        ResponseEntity<BowlingSession> single = c.getSession(1L);
        assertEquals(200, single.getStatusCodeValue());

        when(svc.addGame(1L)).thenReturn(Game.builder().gameNumber(1).build());
        ResponseEntity<Game> gResp = c.addGame(1L);
        assertEquals(200, gResp.getStatusCodeValue());
        when(svc.getOpenSessions()).thenReturn(List.of(BowlingSession.builder().id(2L).build()));
        ResponseEntity<java.util.List<BowlingSession>> open = c.getOpenSessions();
        assertEquals(200, open.getStatusCodeValue());

        when(svc.createSession(any(), anyString(), anyString(), any())).thenReturn(BowlingSession.builder().id(7L).build());
        sg.sports.bowling.dto.request.CreateSessionRequest req = new sg.sports.bowling.dto.request.CreateSessionRequest();
        req.setLocation("loc");
        req.setSessionDate(java.time.LocalDate.now());
        req.setTimeSlot(sg.sports.bowling.entity.BowlingSession.TimeSlot.MORNING);
        ResponseEntity<BowlingSession> created = c.createSession(req);
        assertEquals(200, created.getStatusCodeValue());
        doNothing().when(svc).deleteSession(5L);
        ResponseEntity<Void> del = c.deleteSession(5L);
        assertEquals(204, del.getStatusCodeValue());

        when(svc.closeSession(3L)).thenReturn(BowlingSession.builder().id(3L).build());
        ResponseEntity<BowlingSession> closed = c.closeSession(3L);
        assertEquals(200, closed.getStatusCodeValue());

        when(svc.getGamesForSession(4L)).thenReturn(List.of(Game.builder().gameNumber(1).build()));
        ResponseEntity<java.util.List<Game>> games = c.getGames(4L);
        assertEquals(200, games.getStatusCodeValue());
    }
}
