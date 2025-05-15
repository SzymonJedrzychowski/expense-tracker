package jedrzychowski.szymon.expense_tracker.dto.expenseType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateExpenseTypeRequestDTO {

    @NotNull(message = "ID cannot be null.")
    private Long id;

    @NotBlank(message = "Name cannot be blank.")
    private String name;

    @NotNull(message = "AccountId cannot be null.")
    private Long accountId;

    public UpdateExpenseTypeRequestDTO(Long id, String name, Long accountId) {
        this.id = id;
        this.name = name;
        this.accountId = accountId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
