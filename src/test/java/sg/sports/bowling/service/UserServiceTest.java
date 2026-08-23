package sg.sports.bowling.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import sg.sports.bowling.entity.Role;
import sg.sports.bowling.entity.User;
import sg.sports.bowling.repository.RoleRepository;
import sg.sports.bowling.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    void registerUserValidatesDupesAndRole() {
        UserRepository ur = mock(UserRepository.class);
        RoleRepository rr = mock(RoleRepository.class);
        PasswordEncoder pe = mock(PasswordEncoder.class);

        when(ur.existsByUsername("u")).thenReturn(true);
        UserService svc = new UserService(ur, rr, pe);
        assertThrows(IllegalArgumentException.class, () -> svc.registerUser("u", "e", "p"));

        when(ur.existsByUsername("u")).thenReturn(false);
        when(ur.existsByEmail("e")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> svc.registerUser("u", "e", "p"));

        when(ur.existsByEmail("e")).thenReturn(false);
        when(rr.findByName("USER")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> svc.registerUser("u", "e", "p"));
    }

    @Test
    void changePasswordValidatesCurrentPassword() {
        UserRepository ur = mock(UserRepository.class);
        RoleRepository rr = mock(RoleRepository.class);
        PasswordEncoder pe = mock(PasswordEncoder.class);

        User user = User.builder().username("joe").password("encoded").build();
        when(ur.findByUsername("joe")).thenReturn(Optional.of(user));
        when(pe.matches("wrong", "encoded")).thenReturn(false);

        UserService svc = new UserService(ur, rr, pe);
        assertThrows(IllegalArgumentException.class, () -> svc.changePassword("joe", "wrong", "newp"));
    }
}
