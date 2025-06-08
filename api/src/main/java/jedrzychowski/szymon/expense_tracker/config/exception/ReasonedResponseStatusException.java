package jedrzychowski.szymon.expense_tracker.config.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

abstract public class ReasonedResponseStatusException extends ResponseStatusException {

    private final List<String> reasons;

    public ReasonedResponseStatusException(@NotNull HttpStatusCode status,
                                           @NotBlank String reason) {
        super(status, reason);
        this.reasons = List.of(reason);
    }

    protected ReasonedResponseStatusException(@NotNull HttpStatus status,
                                           @NotEmpty List<String> reasons) {
        super(status, String.join("\n", reasons));
        this.reasons = reasons;
    }

    public List<String> getReasons() {
        return reasons;
    }
}
