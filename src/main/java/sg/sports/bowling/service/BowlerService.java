package sg.sports.bowling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sg.sports.bowling.entity.Bowler;
import sg.sports.bowling.repository.BowlerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BowlerService {

    private final BowlerRepository bowlerRepository;

    public List<Bowler> getAllBowlers() {
        return bowlerRepository.findAllByOrderByNameAsc();
    }

    public Bowler getBowler(Long id) {
        return bowlerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bowler not found: " + id));
    }

    public Bowler createBowler(String name) {
        if (bowlerRepository.existsByName(name)) {
            throw new IllegalArgumentException("Bowler already exists: " + name);
        }
        return bowlerRepository.save(Bowler.builder().name(name).build());
    }

    public Bowler updateBowler(Long id, String name) {
        Bowler bowler = getBowler(id);
        bowler.setName(name);
        return bowlerRepository.save(bowler);
    }

    public void deleteBowler(Long id) {
        bowlerRepository.deleteById(id);
    }
}
