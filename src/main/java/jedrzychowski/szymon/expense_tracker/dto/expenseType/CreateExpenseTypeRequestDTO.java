package jedrzychowski.szymon.expense_tracker.dto.expenseType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateExpenseTypeRequestDTO {

    @NotBlank(message = "Name cannot be blank.")
    private String name;

    @NotNull(message = "AccountId cannot be null.")
    private Long accountId;

    public CreateExpenseTypeRequestDTO(String name, Long accountId) {
        this.name = name;
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
