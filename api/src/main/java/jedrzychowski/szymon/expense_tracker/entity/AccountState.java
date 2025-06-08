package jedrzychowski.szymon.expense_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@JsonIgnoreProperties("expenses")
public class AccountState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Float currentAmount;

    @Column(nullable = false)
    private Float negativeMovement;

    @Column(nullable = false)
    private Float positiveMovement;

    @Column(nullable = false)
    private Float refundAmount;

    @ManyToOne(optional = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Account account;

    @OneToMany(mappedBy = "accountState", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Expense> expenses;

    public AccountState() {
    }

    public AccountState(LocalDate date,
                        Float currentAmount,
                        Float negativeMovement,
                        Float positiveMovement,
                        Float refundAmount,
                        Account account) {
        this.date = date;
        this.currentAmount = currentAmount;
        this.negativeMovement = negativeMovement;
        this.positiveMovement = positiveMovement;
        this.refundAmount = refundAmount;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Float getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(Float currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Float getNegativeMovement() {
        return negativeMovement;
    }

    public void setNegativeMovement(Float negativeMovement) {
        this.negativeMovement = negativeMovement;
    }

    public Float getPositiveMovement() {
        return positiveMovement;
    }

    public void setPositiveMovement(Float positiveMovement) {
        this.positiveMovement = positiveMovement;
    }

    public Float getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Float refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Set<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Set<Expense> expenses) {
        this.expenses = expenses;
    }

    public void recalculateAmountsWithExpense(Expense expenseToAdd) {
        expenses.add(expenseToAdd);
        float addedNegativeMovement = -Math.min(expenseToAdd.getMovementAmount(), 0);
        float addedPositiveMovement = Math.max(expenseToAdd.getMovementAmount(), 0);
        negativeMovement = negativeMovement + addedNegativeMovement;
        positiveMovement = positiveMovement + addedPositiveMovement;
        refundAmount = refundAmount + expenseToAdd.getRefundAmount();
        currentAmount = currentAmount + addedPositiveMovement - addedNegativeMovement;
    }

    public void recalculateAmountsWithoutExpense(Expense expenseToRemove) {
        float subtractedNegativeMovement = -Math.min(expenseToRemove.getMovementAmount(), 0);
        float subtractedPositiveMovement = Math.max(expenseToRemove.getMovementAmount(), 0);
        negativeMovement = negativeMovement - subtractedNegativeMovement;
        positiveMovement = positiveMovement - subtractedPositiveMovement;
        refundAmount = refundAmount - expenseToRemove.getRefundAmount();
        currentAmount = currentAmount - subtractedPositiveMovement + subtractedNegativeMovement;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
