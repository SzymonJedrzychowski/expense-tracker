package jedrzychowski.szymon.expense_tracker.config.handler;

import jakarta.servlet.http.HttpServletRequest;
import jedrzychowski.szymon.expense_tracker.config.exception.ReasonedResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException exception,
                                                            HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("path", request.getRequestURI());

        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getDefaultMessage());
        }
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ReasonedResponseStatusException.class)
    public ResponseEntity<Object> handleReasonedResponseStatusException(ReasonedResponseStatusException exception,
                                                                        HttpServletRequest request) {
        int statusCode = exception.getStatusCode().value();
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        String errorValue = httpStatus != null ? httpStatus.getReasonPhrase() : "";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", statusCode);
        body.put("error", errorValue);
        body.put("path", request.getRequestURI());
        body.put("errors", exception.getReasons());

        return new ResponseEntity<>(body, exception.getStatusCode());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundExceptionException(UsernameNotFoundException exception,
                                                                           HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        int statusCode = httpStatus.value();
        String errorValue = httpStatus.getReasonPhrase();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", statusCode);
        body.put("error", errorValue);
        body.put("path", request.getRequestURI());
        body.put("errors", List.of(exception.getMessage()));

        return new ResponseEntity<>(body, httpStatus);
    }
}
