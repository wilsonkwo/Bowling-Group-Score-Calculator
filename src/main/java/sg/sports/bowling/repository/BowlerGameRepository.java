package sg.sports.bowling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sg.sports.bowling.entity.Bowler;
import sg.sports.bowling.entity.BowlerGame;
import sg.sports.bowling.entity.Game;

import java.util.List;
import java.util.Optional;

public interface BowlerGameRepository extends JpaRepository<BowlerGame, Long> {
    Optional<BowlerGame> findByBowlerAndGame(Bowler bowler, Game game);
    List<BowlerGame> findByGame(Game game);
    List<BowlerGame> findByBowler(Bowler bowler);

    @Query("""
        SELECT bg FROM BowlerGame bg
        JOIN bg.game g
        WHERE g.session.id = :sessionId
        ORDER BY bg.bowler.name ASC
    """)
    List<BowlerGame> findBySessionId(@Param("sessionId") Long sessionId);

    @Query("""
        SELECT bg.bowler, SUM(bg.gamePoints) as totalPoints
        FROM BowlerGame bg
        JOIN bg.game g
        WHERE g.session.id = :sessionId
        GROUP BY bg.bowler
        ORDER BY totalPoints DESC
    """)
    List<Object[]> findSessionLeaderboard(@Param("sessionId") Long sessionId);
}
