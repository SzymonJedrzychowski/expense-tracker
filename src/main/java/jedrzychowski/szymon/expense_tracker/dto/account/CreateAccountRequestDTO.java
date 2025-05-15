package jedrzychowski.szymon.expense_tracker.dto.account;

import jakarta.validation.constraints.NotBlank;

public class CreateAccountRequestDTO {

    @NotBlank(message = "Name cannot be blank.")
    private String name;

    public CreateAccountRequestDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
