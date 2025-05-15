package jedrzychowski.szymon.expense_tracker.entity;

import jakarta.persistence.*;
import jedrzychowski.szymon.expense_tracker.dto.expenseType.CreateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.dto.expenseType.UpdateExpenseTypeRequestDTO;

@Entity
public class ExpenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public ExpenseType() {
    }

    public ExpenseType(String name, Account account) {
        this.name = name;
        this.account = account;
    }

    public ExpenseType(CreateExpenseTypeRequestDTO createExpenseTypeRequestDTO, Account account){
        this(createExpenseTypeRequestDTO.getName(), account);
    }

    public void updateExpenseType(UpdateExpenseTypeRequestDTO updateExpenseTypeRequestDTO, Account account) {
        this.name = updateExpenseTypeRequestDTO.getName();
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
