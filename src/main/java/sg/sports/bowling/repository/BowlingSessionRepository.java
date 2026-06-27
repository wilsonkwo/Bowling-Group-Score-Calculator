package sg.sports.bowling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.sports.bowling.entity.BowlingSession;
import sg.sports.bowling.entity.BowlingSession.SessionStatus;

import java.util.List;

public interface BowlingSessionRepository extends JpaRepository<BowlingSession, Long> {
    List<BowlingSession> findAllByOrderBySessionDateDesc();
    List<BowlingSession> findByStatusOrderBySessionDateDesc(SessionStatus status);
}
