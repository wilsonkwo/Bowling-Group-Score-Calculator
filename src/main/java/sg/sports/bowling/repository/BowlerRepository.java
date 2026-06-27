package sg.sports.bowling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.sports.bowling.entity.Bowler;

import java.util.List;

public interface BowlerRepository extends JpaRepository<Bowler, Long> {
    List<Bowler> findAllByOrderByNameAsc();
    boolean existsByName(String name);
}
