package jedrzychowski.szymon.expense_tracker.config.exception;

import org.springframework.http.HttpStatus;

public class DataConflictException extends ReasonedResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;

    public DataConflictException(String message) {
        super(STATUS, message);
    }
}
