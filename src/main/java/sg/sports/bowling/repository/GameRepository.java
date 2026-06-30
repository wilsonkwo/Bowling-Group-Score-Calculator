package sg.sports.bowling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sg.sports.bowling.entity.BowlingSession;
import sg.sports.bowling.entity.Game;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findBySessionOrderByGameNumberAsc(BowlingSession session);
    Optional<Game> findBySessionAndGameNumber(BowlingSession session, int gameNumber);
    int countBySession(BowlingSession session);

    @Modifying
    @Query("DELETE FROM Game g WHERE g.session = :session")
    void deleteAllBySession(@Param("session") BowlingSession session);
}
