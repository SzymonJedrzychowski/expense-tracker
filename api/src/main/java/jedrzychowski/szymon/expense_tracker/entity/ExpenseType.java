package jedrzychowski.szymon.expense_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jedrzychowski.szymon.expense_tracker.entity.dto.expenseType.CreateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.expenseType.UpdateExpenseTypeRequestDTO;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "account_id"})
        }
)
public class ExpenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Account account;

    public ExpenseType() {
    }

    public ExpenseType(String name,
                       Account account) {
        this.name = name;
        this.account = account;
    }

    public ExpenseType(CreateExpenseTypeRequestDTO createExpenseTypeRequestDTO,
                       Account account) {
        this(createExpenseTypeRequestDTO.name(), account);
    }

    public void updateExpenseType(UpdateExpenseTypeRequestDTO updateExpenseTypeRequestDTO,
                                  Account account) {
        this.name = updateExpenseTypeRequestDTO.name();
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
