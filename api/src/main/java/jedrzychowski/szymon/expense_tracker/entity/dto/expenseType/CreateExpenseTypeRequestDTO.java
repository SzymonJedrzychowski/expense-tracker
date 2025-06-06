package jedrzychowski.szymon.expense_tracker.entity.dto.expenseType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateExpenseTypeRequestDTO(@NotBlank(message = "Name cannot be blank.") String name,
                                          @NotNull(message = "AccountId cannot be null.") Long accountId) {

    public CreateExpenseTypeRequestDTO(String name,
                                       Long accountId) {
        this.name = name;
        this.accountId = accountId;
    }
}
