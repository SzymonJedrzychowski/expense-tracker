package jedrzychowski.szymon.expense_tracker.entity.dto.account;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequestDTO(@NotBlank(message = "Name cannot be blank.") String name) {

    public CreateAccountRequestDTO(String name) {
        this.name = name;
    }
}
