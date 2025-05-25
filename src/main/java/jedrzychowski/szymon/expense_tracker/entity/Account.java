package jedrzychowski.szymon.expense_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jedrzychowski.szymon.expense_tracker.dto.account.CreateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.dto.account.UpdateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.exception.ReasonedResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Entity
@JsonIgnoreProperties("expenseTypes")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private AppUser appUser;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseType> expenseTypes;

    public Account() {
    }

    public Account(String name) {
        this.name = name;
    }

    public Account(CreateAccountRequestDTO createAccountRequestDto, AppUser appUser) {
        this.name = createAccountRequestDto.getName();
        this.appUser = appUser;
    }

    public void update(UpdateAccountRequestDTO updateAccountRequestDTO) {
        this.name = updateAccountRequestDTO.getName();
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

    public void validateIfAccountIsOwnedByCurrentUser(AppUser appUser) {
        if (this.appUser.equals(appUser)) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Account is not owned by authorised user.");
        }
    }
}
