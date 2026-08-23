package sg.sports.bowling;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class BowlingApplicationTest {

    @Test
    void mainInvokesSpringRun() {
        try (MockedStatic<SpringApplication> ms = Mockito.mockStatic(SpringApplication.class)) {
            ms.when(() -> SpringApplication.run(BowlingApplication.class, new String[0])).thenReturn(null);
            BowlingApplication.main(new String[]{});
            ms.verify(() -> SpringApplication.run(BowlingApplication.class, new String[0]));
        }
    }
}
