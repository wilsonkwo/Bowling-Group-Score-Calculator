package sg.sports.bowling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.sports.bowling.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
