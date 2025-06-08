package jedrzychowski.szymon.expense_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jedrzychowski.szymon.expense_tracker.entity.dto.expense.CreateExpenseRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.expense.UpdateExpenseRequestDTO;

import java.time.LocalDate;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Float movementAmount;

    private Float refundAmount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_type_id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private ExpenseType expenseType;

    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Account account;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_state_id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private AccountState accountState;

    public Expense() {
    }

    public Expense(Float movementAmount,
                   Float refundAmount,
                   ExpenseType expenseType,
                   String description,
                   AccountState accountState,
                   Account account) {
        this.movementAmount = movementAmount;
        setRefundAmount(refundAmount);
        this.expenseType = expenseType;
        this.description = description;
        this.accountState = accountState;
        this.account = account;
    }

    public Expense(CreateExpenseRequestDTO createExpenseRequestDTO,
                   ExpenseType expenseType,
                   AccountState accountState,
                   Account account) {
        this.movementAmount = createExpenseRequestDTO.movementAmount();
        setRefundAmount(createExpenseRequestDTO.getRefundAmount());
        this.expenseType = expenseType;
        this.description = createExpenseRequestDTO.description();
        this.accountState = accountState;
        this.account = account;
    }

    public void updateExpense(UpdateExpenseRequestDTO updateExpenseRequestDTO,
                              ExpenseType expenseType,
                              AccountState accountState,
                              Account account) {
        this.movementAmount = updateExpenseRequestDTO.movementAmount();
        this.refundAmount = updateExpenseRequestDTO.refundAmount();
        this.expenseType = expenseType;
        this.description = updateExpenseRequestDTO.description();
        this.accountState = accountState;
        this.account = account;

        this.accountState.recalculateAmountsWithExpense(this);
    }

    public Long getId() {
        return id;
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
        if (refundAmount != null && refundAmount == 0) refundAmount = null;
        this.refundAmount = refundAmount;
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(ExpenseType expenseType) {
        this.expenseType = expenseType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AccountState getAccountState() {
        return accountState;
    }

    public void setAccountState(AccountState accountState) {
        this.accountState = accountState;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public LocalDate getDate() {
        return this.accountState.getDate();
    }
}
