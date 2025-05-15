package jedrzychowski.szymon.expense_tracker.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateAccountRequestDTO {

    @NotNull(message = "ID cannot be null.")
    private Long id;

    @NotBlank(message = "Name cannot be blank.")
    private String name;

    public UpdateAccountRequestDTO(Long id, String name) {
        this.id = id;
        this.name = name;
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
}
