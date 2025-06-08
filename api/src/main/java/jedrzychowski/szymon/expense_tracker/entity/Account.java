package jedrzychowski.szymon.expense_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jedrzychowski.szymon.expense_tracker.config.exception.UnauthorizedUserAccessException;
import jedrzychowski.szymon.expense_tracker.entity.dto.account.CreateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.account.UpdateAccountRequestDTO;

import java.util.List;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "app_user_id"})
        }
)
@JsonIgnoreProperties("expenseTypes")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "app_user_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private AppUser appUser;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseType> expenseTypes;

    public Account() {
    }

    public Account(String name) {
        this.name = name;
    }

    public Account(CreateAccountRequestDTO createAccountRequestDto,
                   AppUser appUser) {
        this.name = createAccountRequestDto.name();
        this.appUser = appUser;
    }

    public void update(UpdateAccountRequestDTO updateAccountRequestDTO) {
        this.name = updateAccountRequestDTO.name();
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

    public List<ExpenseType> getExpenseTypes() {
        return expenseTypes;
    }

    public void setExpenseTypes(List<ExpenseType> expenseTypes) {
        this.expenseTypes = expenseTypes;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public void validateIfAccountIsOwnedByCurrentUser(AppUser appUser) throws
                                                                       UnauthorizedUserAccessException {
        if (!this.appUser.equals(appUser)) {
            throw new UnauthorizedUserAccessException("Account is not owned by authorised user.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;
        return id.equals(account.id) && appUser.equals(account.appUser) && name.equals(account.name);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + appUser.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
