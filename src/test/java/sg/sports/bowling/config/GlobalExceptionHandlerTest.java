package sg.sports.bowling.config;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import sg.sports.bowling.dto.request.CreateBowlerRequest;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void handleValidationReturnsStructuredErrors() throws NoSuchMethodException {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        CreateBowlerRequest req = new CreateBowlerRequest();
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(req, "req");
        binding.addError(new FieldError("req", "name", "must not be blank"));

        Method m = TestController.class.getMethod("dummy", CreateBowlerRequest.class);
        MethodParameter mp = new MethodParameter(m, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, binding);

        var resp = handler.handleValidation(ex);
        assertEquals(400, resp.getStatusCodeValue());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals("Validation failed", body.get("message"));
        assertTrue(body.containsKey("errors"));
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals("must not be blank", errors.get("name"));
    }

    @Test
    void handleValidationMergeFunctionKeepsFirstMessage() throws NoSuchMethodException {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        CreateBowlerRequest req = new CreateBowlerRequest();
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(req, "req");
        // two errors for same field to trigger merge (a,b)->a
        binding.addError(new FieldError("req", "name", "first"));
        binding.addError(new FieldError("req", "name", "second"));

        Method m = TestController.class.getMethod("dummy", CreateBowlerRequest.class);
        MethodParameter mp = new MethodParameter(m, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, binding);

        var resp = handler.handleValidation(ex);
        @SuppressWarnings("unchecked")
        var errors = (java.util.Map<String, String>) resp.getBody().get("errors");
        assertEquals("first", errors.get("name"));
    }

    static class TestController {
        public void dummy(CreateBowlerRequest r) {}
    }
}
