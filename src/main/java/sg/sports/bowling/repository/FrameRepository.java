package sg.sports.bowling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sg.sports.bowling.entity.BowlerGame;
import sg.sports.bowling.entity.BowlingSession;
import sg.sports.bowling.entity.Frame;

import java.util.List;
import java.util.Optional;

public interface FrameRepository extends JpaRepository<Frame, Long> {
    List<Frame> findByBowlerGameOrderByFrameNumberAsc(BowlerGame bowlerGame);
    Optional<Frame> findByBowlerGameAndFrameNumber(BowlerGame bowlerGame, int frameNumber);
    int countByBowlerGame(BowlerGame bowlerGame);
    void deleteAllByBowlerGame(BowlerGame bowlerGame);

    @Modifying
    @Query("DELETE FROM Frame f WHERE f.bowlerGame.game.session = :session")
    void deleteAllBySession(@Param("session") BowlingSession session);
}
