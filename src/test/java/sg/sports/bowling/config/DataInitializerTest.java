package sg.sports.bowling.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import sg.sports.bowling.entity.Role;
import sg.sports.bowling.repository.RoleRepository;
import sg.sports.bowling.repository.UserRepository;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    @Test
    void runSkipsWhenAdminExists() {
        RoleRepository rr = mock(RoleRepository.class);
        UserRepository ur = mock(UserRepository.class);
        PasswordEncoder pe = mock(PasswordEncoder.class);
        when(ur.existsByUsername(anyString())).thenReturn(true);

        DataInitializer init = new DataInitializer(rr, ur, pe);
        init.run(null);

        verify(ur).existsByUsername(anyString());
        verify(ur, never()).save(any());
    }

    @Test
    void seedDefaultAdminRoleMissingThrows() throws Exception {
        RoleRepository rr = mock(RoleRepository.class);
        UserRepository ur = mock(UserRepository.class);
        PasswordEncoder pe = mock(PasswordEncoder.class);

        when(ur.existsByUsername(anyString())).thenReturn(false);
        when(rr.findByName("ADMIN")).thenReturn(Optional.empty());

        DataInitializer init = new DataInitializer(rr, ur, pe);

        Method m = DataInitializer.class.getDeclaredMethod("seedDefaultAdmin");
        m.setAccessible(true);
        java.lang.reflect.InvocationTargetException ite = assertThrows(java.lang.reflect.InvocationTargetException.class,
            () -> m.invoke(init));
        assertTrue(ite.getCause() instanceof IllegalStateException);
    }
}
