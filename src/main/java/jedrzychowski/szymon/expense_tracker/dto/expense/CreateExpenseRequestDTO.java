package jedrzychowski.szymon.expense_tracker.dto.expense;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateExpenseRequestDTO {

    @NotNull(message = "Date cannot be null.")
    private LocalDate date;

    @NotNull(message = "MovementAmount cannot be null.")
    private Float movementAmount;

    private Float refundAmount;

    @NotNull(message = "ExpenseTypeId cannot be null.")
    private Long expenseTypeId;

    private String description;

    @NotNull(message = "AccountId cannot be null.")
    private Long accountId;

    public CreateExpenseRequestDTO() {
    }

    public CreateExpenseRequestDTO(LocalDate date, Float movementAmount, Float refundAmount, Long expenseTypeId,
                                   String description, Long accountId) {
        this.date = date;
        this.movementAmount = movementAmount;
        this.refundAmount = refundAmount;
        this.expenseTypeId = expenseTypeId;
        this.description = description;
        this.accountId = accountId;
    }

    public List<String> validateEntity() {
        List<String> validations = new ArrayList<>();
        if (movementAmount == null || movementAmount == 0) {
            validations.add("Movement Amount must be non-null and non-zero.");
        }

        if (refundAmount != null && refundAmount < 0) {
            validations.add("Refund Amount must be positive.");
        }

        if (refundAmount != null && refundAmount > 0 && movementAmount != null && movementAmount > 0) {
            validations.add("Movement Amount must be negative to have a non-zero Refund Amount.");
        }
        return validations;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Float getMovementAmount() {
        return movementAmount;
    }

    public void setMovementAmount(Float movementAmount) {
        this.movementAmount = movementAmount;
    }

    public Float getRefundAmount() {
        return refundAmount == null ? 0 : refundAmount;
    }

    public void setRefundAmount(Float refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Long getExpenseTypeId() {
        return expenseTypeId;
    }

    public void setExpenseTypeId(Long expenseTypeId) {
        this.expenseTypeId = expenseTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
