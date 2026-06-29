package sg.sports.bowling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.sports.bowling.entity.BowlingSession;
import sg.sports.bowling.entity.BowlingSession.SessionStatus;
import sg.sports.bowling.entity.BowlingSession.TimeSlot;
import sg.sports.bowling.entity.Game;
import sg.sports.bowling.repository.BowlingSessionRepository;
import sg.sports.bowling.repository.GameRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final BowlingSessionRepository sessionRepository;
    private final GameRepository gameRepository;

    public List<BowlingSession> getAllSessions() {
        return sessionRepository.findAllByOrderBySessionDateDesc();
    }

    public List<BowlingSession> getOpenSessions() {
        return sessionRepository.findByStatusOrderBySessionDateDesc(SessionStatus.OPEN);
    }

    public BowlingSession getSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));
    }

    @Transactional
    public BowlingSession createSession(LocalDate date, String location, String notes, TimeSlot timeSlot) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("timeSlot is required");
        }
        BowlingSession session = BowlingSession.builder()
                .sessionDate(date)
                .location(location)
                .notes(notes)
                .timeSlot(timeSlot)
                .build();
        return sessionRepository.save(session);
    }

    @Transactional
    public Game addGame(Long sessionId) {
        BowlingSession session = getSession(sessionId);
        int nextGameNumber = gameRepository.countBySession(session) + 1;
        Game game = Game.builder()
                .session(session)
                .gameNumber(nextGameNumber)
                .build();
        return gameRepository.save(game);
    }

    @Transactional
    public BowlingSession closeSession(Long sessionId) {
        BowlingSession session = getSession(sessionId);
        session.setStatus(SessionStatus.CLOSED);
        return sessionRepository.save(session);
    }

    public List<Game> getGamesForSession(Long sessionId) {
        BowlingSession session = getSession(sessionId);
        return gameRepository.findBySessionOrderByGameNumberAsc(session);
    }
}
