package jedrzychowski.szymon.expense_tracker.config.exception;

import org.springframework.http.HttpStatus;

public class DataNotFoundException extends ReasonedResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public DataNotFoundException(String message) {
        super(STATUS, message);
    }
}
