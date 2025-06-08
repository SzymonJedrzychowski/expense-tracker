package jedrzychowski.szymon.expense_tracker.entity.dto.expenseType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateExpenseTypeRequestDTO(@NotNull(message = "ID cannot be null.") Long id,
                                          @NotBlank(message = "Name cannot be blank.") String name,
                                          @NotNull(message = "AccountId cannot be null.") Long accountId) {

    public UpdateExpenseTypeRequestDTO(Long id,
                                       String name,
                                       Long accountId) {
        this.id = id;
        this.name = name;
        this.accountId = accountId;
    }
}
