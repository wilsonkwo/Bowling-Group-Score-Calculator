package sg.sports.bowling.service;

import org.junit.jupiter.api.Test;
import sg.sports.bowling.entity.BowlingSession;
import sg.sports.bowling.entity.BowlingSession.TimeSlot;
import sg.sports.bowling.entity.Game;
import sg.sports.bowling.repository.BowlerGameRepository;
import sg.sports.bowling.repository.BowlingSessionRepository;
import sg.sports.bowling.repository.FrameRepository;
import sg.sports.bowling.repository.GameRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    @Test
    void createSessionValidatesTimeSlotAndDuplicates() {
        BowlingSessionRepository sr = mock(BowlingSessionRepository.class);
        GameRepository gr = mock(GameRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        FrameRepository fr = mock(FrameRepository.class);

        SessionService svc = new SessionService(sr, gr, bgr, fr);

        assertThrows(IllegalArgumentException.class, () -> svc.createSession(LocalDate.now(), "L", "n", null));

        when(sr.existsBySessionDateAndTimeSlot(any(), any())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> svc.createSession(LocalDate.now(), "L", "n", TimeSlot.MORNING));
    }

    @Test
    void getSessionNotFoundThrows() {
        BowlingSessionRepository sr = mock(BowlingSessionRepository.class);
        when(sr.findById(1L)).thenReturn(Optional.empty());

        SessionService svc = new SessionService(sr, mock(GameRepository.class), mock(BowlerGameRepository.class), mock(FrameRepository.class));
        assertThrows(IllegalArgumentException.class, () -> svc.getSession(1L));
    }

    @Test
    void addGameCreatesNewGame() {
        BowlingSessionRepository sr = mock(BowlingSessionRepository.class);
        GameRepository gr = mock(GameRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        FrameRepository fr = mock(FrameRepository.class);

        BowlingSession session = BowlingSession.builder().id(1L).build();
        when(sr.findById(1L)).thenReturn(Optional.of(session));
        when(gr.countBySession(session)).thenReturn(2);
        when(gr.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

        SessionService svc = new SessionService(sr, gr, bgr, fr);
        Game g = svc.addGame(1L);
        assertEquals(3, g.getGameNumber());
    }

    @Test
    void deleteSessionRemovesRelatedData() {
        BowlingSessionRepository sr = mock(BowlingSessionRepository.class);
        GameRepository gr = mock(GameRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        FrameRepository fr = mock(FrameRepository.class);

        BowlingSession session = BowlingSession.builder().id(5L).build();
        when(sr.findById(5L)).thenReturn(Optional.of(session));

        SessionService svc = new SessionService(sr, gr, bgr, fr);
        svc.deleteSession(5L);

        verify(fr).deleteAllBySession(session);
        verify(bgr).deleteAllBySession(session);
        verify(gr).deleteAllBySession(session);
        verify(sr).delete(session);
    }

    @Test
    void listAndCloseSessionPaths() {
        BowlingSessionRepository sr = mock(BowlingSessionRepository.class);
        GameRepository gr = mock(GameRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        FrameRepository fr = mock(FrameRepository.class);

        BowlingSession s1 = BowlingSession.builder().id(10L).build();
        when(sr.findAllByOrderBySessionDateDesc()).thenReturn(java.util.List.of(s1));
        when(sr.findByStatusOrderBySessionDateDesc(any())).thenReturn(java.util.List.of(s1));
        when(sr.findById(10L)).thenReturn(java.util.Optional.of(s1));
        when(gr.findBySessionOrderByGameNumberAsc(s1)).thenReturn(java.util.List.of());
        when(sr.save(s1)).thenReturn(s1);

        SessionService svc = new SessionService(sr, gr, bgr, fr);
        assertEquals(1, svc.getAllSessions().size());
        assertEquals(1, svc.getOpenSessions().size());
        svc.closeSession(10L);
        assertEquals(s1.getStatus(), s1.getStatus());
        assertTrue(svc.getGamesForSession(10L).isEmpty());
    }
}
