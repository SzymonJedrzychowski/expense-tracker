package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.entity.dto.account.CreateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.account.UpdateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("expense-tracker/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Gets all Accounts.
     *
     * @param appUser Currently Authorized AppUser.
     * @return List of Accounts.
     */
    @GetMapping
    public List<Account> getAllAccounts(@AuthenticationPrincipal AppUser appUser) {
        return accountService.getAllAccounts(appUser);
    }

    /**
     * Gets the Account by specified ID.
     *
     * @param appUser Currently Authorized AppUser.
     * @param id      of specific Account.
     * @return Account with specified ID.
     * @throws ReasonedResponseStatusException if no Account with specific ID is found.
     */
    @GetMapping("/{id}")
    public Account getAccountById(@AuthenticationPrincipal AppUser appUser,
                                  @PathVariable Long id) {
        return accountService.getAccountById(appUser, id);
    }

    /**
     * Creates new Account.
     *
     * @param appUser                 Currently Authorized AppUser.
     * @param createAccountRequestDTO CreateAccountRequestDTO of the Account that is being created.
     * @return Account that was created.
     * @throws ReasonedResponseStatusException if validation of Account fails or Account with the same name already exists.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid CreateAccountRequestDTO createAccountRequestDTO) {
        return accountService.createAccount(appUser, createAccountRequestDTO);
    }

    /**
     * Updates Account.
     *
     * @param appUser                 Currently Authorized AppUser.
     * @param updateAccountRequestDTO UpdateAccountRequestDTO with information about updated Account.
     * @return Updated Account.
     */
    @PutMapping
    public Account updateAccount(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid UpdateAccountRequestDTO updateAccountRequestDTO) {
        return accountService.updateAccount(appUser, updateAccountRequestDTO);
    }

    /**
     * Deletes the Account.
     *
     * @param appUser Currently Authorized AppUser.
     * @param id      of Account to remove.
     * @throws ReasonedResponseStatusException if Account cannot be found or cannot be removed.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@AuthenticationPrincipal AppUser appUser,
                              @PathVariable Long id,
                              @RequestParam(defaultValue = "false") boolean deleteExpenses) {
        accountService.deleteAccount(appUser, id, deleteExpenses);
    }
}
