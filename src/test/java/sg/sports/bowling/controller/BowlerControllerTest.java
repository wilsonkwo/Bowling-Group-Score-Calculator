package sg.sports.bowling.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import sg.sports.bowling.entity.Bowler;
import sg.sports.bowling.service.BowlerService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BowlerControllerTest {

    @Test
    void getAllAndCrudPaths() {
        BowlerService svc = mock(BowlerService.class);
        BowlerController c = new BowlerController(svc);

        when(svc.getAllBowlers()).thenReturn(List.of(Bowler.builder().id(1L).name("a").build()));
        ResponseEntity<List<Bowler>> listResp = c.getAllBowlers();
        assertEquals(200, listResp.getStatusCodeValue());

        when(svc.getBowler(2L)).thenReturn(Bowler.builder().id(2L).name("b").build());
        ResponseEntity<Bowler> singleResp = c.getBowler(2L);
        assertEquals(200, singleResp.getStatusCodeValue());

        when(svc.createBowler("n")).thenReturn(Bowler.builder().id(3L).name("n").build());
        sg.sports.bowling.dto.request.CreateBowlerRequest req = new sg.sports.bowling.dto.request.CreateBowlerRequest();
        req.setName("n");
        ResponseEntity<Bowler> createResp = c.createBowler(req);
        assertEquals(200, createResp.getStatusCodeValue());
        // when passing null request it will NPE in service; we assert that method wiring compiles and returns a response when service works
        // the test focuses on method invocation; if service is mocked to accept null, controller will return 200
    }
}
