package jedrzychowski.szymon.expense_tracker.config.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedUserAccessException extends ReasonedResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public UnauthorizedUserAccessException(String message) {
        super(STATUS, message);
    }
}
