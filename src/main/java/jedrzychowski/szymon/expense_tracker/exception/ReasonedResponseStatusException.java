package jedrzychowski.szymon.expense_tracker.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class ReasonedResponseStatusException extends ResponseStatusException {

    private final String[] reasons;

    public ReasonedResponseStatusException(@NotNull HttpStatusCode status, @NotBlank String reason) {
        super(status, reason);
        this.reasons = new String[] {reason};
    }

    public ReasonedResponseStatusException(@NotNull HttpStatus status, @NotEmpty String[] reasons){
        super(status, String.join("\n", reasons));
        this.reasons = reasons;
    }

    public String[] getReasons() {
        return reasons;
    }
}
