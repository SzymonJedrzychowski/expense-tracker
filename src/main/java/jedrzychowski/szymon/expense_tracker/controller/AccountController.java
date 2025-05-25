package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.dto.account.CreateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.dto.account.UpdateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("expense-tracker/v1/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    /**
     * Gets all Accounts.
     *
     * @param appUser Currently Authorized AppUser.
     * @return List of Accounts.
     */
    @GetMapping
    public List<Account> getAll(@AuthenticationPrincipal AppUser appUser) {
        return accountRepository.findAllByAppUser(appUser);
    }

    /**
     * Gets the Account by specified ID.
     *
     * @param appUser Currently Authorized AppUser.
     * @param id of specific Account.
     * @return Account with specified ID.
     * @throws ReasonedResponseStatusException if no Account with specific ID is found.
     */
    @GetMapping("/{id}")
    public Account getAccountById(@AuthenticationPrincipal AppUser appUser,
                                  @PathVariable Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID: %d", id)
                ));
        account.validateIfAccountIsOwnedByCurrentUser(appUser);
        return account;
    }

    /**
     * Creates new Account.
     *
     * @param appUser Currently Authorized AppUser.
     * @param createAccountRequestDTO CreateAccountRequestDTO of the Account that is being created.
     * @return Account that was created.
     * @throws ReasonedResponseStatusException if validation of Account fails or Account with the same name already exists.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid CreateAccountRequestDTO createAccountRequestDTO) {
        //Create Account based on the DTO
        Account newAccount = new Account(createAccountRequestDTO, appUser);
        return accountRepository.save(newAccount);
    }

    /**
     * Updates Account.
     *
     * @param appUser Currently Authorized AppUser.
     * @param updateAccountRequestDTO UpdateAccountRequestDTO with information about updated Account.
     * @return Updated Account.
     */
    @PutMapping
    public Account updateAccount(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid UpdateAccountRequestDTO updateAccountRequestDTO) {
        //Find account to update
        Account accountToUpdate = accountRepository.findById(updateAccountRequestDTO.getId())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID: %d.", updateAccountRequestDTO.getId())
                ));
        accountToUpdate.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Update account based on the DTO
        accountToUpdate.update(updateAccountRequestDTO);
        return accountRepository.save(accountToUpdate);
    }

    /**
     * Deletes the Account.
     *
     * @param appUser Currently Authorized AppUser.
     * @param id of Account to remove.
     * @throws ReasonedResponseStatusException if Account cannot be found or cannot be removed.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(
            @AuthenticationPrincipal AppUser appUser,
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean deleteExpenses
    ) {
        //Find Account to delete
        Account accountToDelete = accountRepository.findById(id)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID: %d.", id)
                ));

        accountToDelete.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Validate that no expenses exist with this Account
        int expensesWithAccount = expenseRepository.countByAccount(accountToDelete);
        if (expensesWithAccount > 0 && !deleteExpenses) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.CONFLICT,
                    String.format("Account with ID: %d and Name: %s is used by existing Expense records.",
                            accountToDelete.getId(), accountToDelete.getName())
            );
        }

        accountRepository.delete(accountToDelete);
    }

}
