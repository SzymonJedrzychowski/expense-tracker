package jedrzychowski.szymon.expense_tracker.config.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenActionException extends ReasonedResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;

    public ForbiddenActionException(String message) {
        super(STATUS, message);
    }
}
