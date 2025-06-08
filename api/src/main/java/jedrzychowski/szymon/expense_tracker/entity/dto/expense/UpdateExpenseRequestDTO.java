package jedrzychowski.szymon.expense_tracker.entity.dto.expense;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record UpdateExpenseRequestDTO(@NotNull(message = "ID cannot be null.") Long id,
                                      @NotNull(message = "Date cannot be null.") LocalDate date,
                                      @NotNull(message = "MovementAmount cannot be null.") Float movementAmount,
                                      Float refundAmount,
                                      @NotNull(message = "ExpenseTypeId cannot be null.") Long expenseTypeId,
                                      String description,
                                      @NotNull(message = "AccountId cannot be null.") Long accountId) {

    public UpdateExpenseRequestDTO(Long id,
                                   LocalDate date,
                                   Float movementAmount,
                                   Float refundAmount,
                                   Long expenseTypeId,
                                   String description,
                                   Long accountId) {
        this.id = id;
        this.date = date;
        this.movementAmount = movementAmount;
        this.refundAmount = refundAmount;
        this.expenseTypeId = expenseTypeId;
        this.description = description;
        this.accountId = accountId;
    }

    public List<String> validateDTO() {
        List<String> validationErrors = new ArrayList<>();
        if (movementAmount == null || movementAmount == 0) {
            validationErrors.add("Movement Amount must be non-null and non-zero.");
        }

        if (refundAmount != null && refundAmount < 0) {
            validationErrors.add("Refund Amount must be positive.");
        }

        if (refundAmount != null && refundAmount > 0 && movementAmount != null && movementAmount > 0) {
            validationErrors.add("Movement Amount must be negative to have a non-zero Refund Amount.");
        }
        return validationErrors;
    }
}
