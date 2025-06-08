package jedrzychowski.szymon.expense_tracker.config.exception;

import org.springframework.http.HttpStatus;

public class ParamValidationException extends ReasonedResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public ParamValidationException(String validationResult) {
        super(STATUS, validationResult);
    }
}
