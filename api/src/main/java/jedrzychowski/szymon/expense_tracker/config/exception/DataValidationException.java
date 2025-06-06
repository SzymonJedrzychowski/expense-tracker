package jedrzychowski.szymon.expense_tracker.config.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class DataValidationException extends ReasonedResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public DataValidationException(List<String> validationResults) {
        super(STATUS, validationResults);
    }
}
