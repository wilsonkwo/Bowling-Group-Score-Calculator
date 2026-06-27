package sg.sports.bowling.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sg.sports.bowling.entity.Role;
import sg.sports.bowling.entity.User;
import sg.sports.bowling.repository.RoleRepository;
import sg.sports.bowling.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * Seeds the database with required reference data on startup.
 * Idempotent — safe to run every time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedDefaultAdmin();
    }

    private void seedRoles() {
        for (String roleName : new String[]{"ADMIN", "USER"}) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Created role: {}", roleName);
            }
        }
    }

    /**
     * Creates a default admin account if no admin exists yet.
     * Change the password immediately after first login.
     * Credentials are read from env vars ADMIN_USERNAME / ADMIN_PASSWORD,
     * falling back to "admin" / "changeme" for local dev only.
     */
    private void seedDefaultAdmin() {
        String adminUsername = System.getenv().getOrDefault("ADMIN_USERNAME", "admin");

        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        String rawPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "changeme");
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found after seed"));

        User admin = User.builder()
                .username(adminUsername)
                .email("admin@bowling.local")
                .password(passwordEncoder.encode(rawPassword))
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        userRepository.save(admin);
        log.warn("Default admin account created (username={}). Change the password!", adminUsername);
    }
}
