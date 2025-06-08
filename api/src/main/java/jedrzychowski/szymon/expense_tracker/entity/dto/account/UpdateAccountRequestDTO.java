package jedrzychowski.szymon.expense_tracker.entity.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountRequestDTO(@NotNull(message = "ID cannot be null.") Long id,
                                      @NotBlank(message = "Name cannot be blank.") String name) {

    public UpdateAccountRequestDTO(Long id,
                                   String name) {
        this.id = id;
        this.name = name;
    }
}
