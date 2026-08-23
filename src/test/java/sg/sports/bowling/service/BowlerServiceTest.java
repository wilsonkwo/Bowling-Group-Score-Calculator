package sg.sports.bowling.service;

import org.junit.jupiter.api.Test;
import sg.sports.bowling.entity.Bowler;
import sg.sports.bowling.repository.BowlerGameRepository;
import sg.sports.bowling.repository.BowlerRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BowlerServiceTest {

    @Test
    void createBowlerDuplicateThrows() {
        BowlerRepository br = mock(BowlerRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        when(br.existsByName("a")).thenReturn(true);
        BowlerService svc = new BowlerService(br, bgr);
        assertThrows(IllegalArgumentException.class, () -> svc.createBowler("a"));
    }

    @Test
    void deleteBowlerWithHistoryThrows() {
        BowlerRepository br = mock(BowlerRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        Bowler b = Bowler.builder().id(1L).name("x").build();
        when(br.findById(1L)).thenReturn(Optional.of(b));
        when(bgr.existsByBowler(b)).thenReturn(true);
        BowlerService svc = new BowlerService(br, bgr);
        assertThrows(IllegalArgumentException.class, () -> svc.deleteBowler(1L));
    }

    @Test
    void updateAndGetBowler() {
        BowlerRepository br = mock(BowlerRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        Bowler b = Bowler.builder().id(2L).name("x").build();
        when(br.findById(2L)).thenReturn(Optional.of(b));
        when(br.save(any())).thenAnswer(i -> i.getArgument(0));
        BowlerService svc = new BowlerService(br, bgr);
        Bowler updated = svc.updateBowler(2L, "y");
        assertEquals("y", updated.getName());
    }

    @Test
    void getBowlerNotFoundThrows() {
        BowlerRepository br = mock(BowlerRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        when(br.findById(5L)).thenReturn(Optional.empty());
        BowlerService svc = new BowlerService(br, bgr);
        assertThrows(IllegalArgumentException.class, () -> svc.getBowler(5L));
    }

    @Test
    void deleteBowlerSuccessDeletes() {
        BowlerRepository br = mock(BowlerRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        Bowler b = Bowler.builder().id(9L).name("z").build();
        when(br.findById(9L)).thenReturn(Optional.of(b));
        when(bgr.existsByBowler(b)).thenReturn(false);
        doNothing().when(br).deleteById(9L);
        BowlerService svc = new BowlerService(br, bgr);
        svc.deleteBowler(9L);
        verify(br).deleteById(9L);
    }

    @Test
    void createBowlerSuccessCreates() {
        BowlerRepository br = mock(BowlerRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        when(br.existsByName("new")).thenReturn(false);
        when(br.save(any())).thenAnswer(i -> {
            Bowler b = i.getArgument(0);
            b.setId(42L);
            return b;
        });
        BowlerService svc = new BowlerService(br, bgr);
        Bowler created = svc.createBowler("new");
        assertEquals(42L, created.getId());
        assertEquals("new", created.getName());
        verify(br).save(any());
    }

    @Test
    void getAllBowlersReturnsList() {
        BowlerRepository br = mock(BowlerRepository.class);
        BowlerGameRepository bgr = mock(BowlerGameRepository.class);
        Bowler b1 = Bowler.builder().id(1L).name("A").build();
        Bowler b2 = Bowler.builder().id(2L).name("B").build();
        when(br.findAllByOrderByNameAsc()).thenReturn(java.util.List.of(b1, b2));
        BowlerService svc = new BowlerService(br, bgr);
        java.util.List<Bowler> list = svc.getAllBowlers();
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getName());
    }
}
