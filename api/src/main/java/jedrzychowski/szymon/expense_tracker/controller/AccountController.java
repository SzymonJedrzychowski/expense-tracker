package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.DataConflictException;
import jedrzychowski.szymon.expense_tracker.config.exception.DataNotFoundException;
import jedrzychowski.szymon.expense_tracker.config.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.config.exception.UnauthorizedUserAccessException;
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
     * Retrieves all accounts for the currently authorized user.
     *
     * @param appUser the currently authorized AppUser
     * @return a list of accounts belonging to the user
     */
    @GetMapping
    public List<Account> getAllAccounts(@AuthenticationPrincipal AppUser appUser) {
        return accountService.getAllAccounts(appUser);
    }

    /**
     * Retrieves an account by its ID for the currently authorized user.
     *
     * @param appUser the currently authorized AppUser
     * @param id      the ID of the account to retrieve
     * @return the account with the specified ID
     * @throws ReasonedResponseStatusException if no account with the specified ID is found
     * @throws UnauthorizedUserAccessException if the account does not belong to the authorized user
     */
    @GetMapping("/{id}")
    public Account getAccountById(@AuthenticationPrincipal AppUser appUser,
                                  @PathVariable Long id) {
        return accountService.getAccountById(appUser, id);
    }

    /**
     * Creates a new account for the currently authorized user.
     *
     * @param appUser                 the currently authorized AppUser
     * @param createAccountRequestDTO the data for the account to be created
     * @return the created Account
     * @throws DataConflictException if an account with the same name already exists
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid CreateAccountRequestDTO createAccountRequestDTO) {
        return accountService.createAccount(appUser, createAccountRequestDTO);
    }

    /**
     * Updates an existing account for the currently authorized user.
     *
     * @param appUser                 the currently authorized AppUser
     * @param updateAccountRequestDTO the updated account information
     * @return the updated Account
     * @throws DataNotFoundException           if the account cannot be found
     * @throws UnauthorizedUserAccessException if the account does not belong to the authorized user
     */
    @PutMapping
    public Account updateAccount(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid UpdateAccountRequestDTO updateAccountRequestDTO) {
        return accountService.updateAccount(appUser, updateAccountRequestDTO);
    }

    /**
     * Deletes the account with the specified ID.
     *
     * @param appUser        the currently authorized AppUser
     * @param id             the ID of the account to remove
     * @param deleteExpenses whether to also delete associated expenses (default is false)
     * @throws DataNotFoundException           if the account cannot be found
     * @throws DataConflictException           if the account cannot be removed because of existing expenses
     * @throws UnauthorizedUserAccessException if the account does not belong to the authorized user
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@AuthenticationPrincipal AppUser appUser,
                              @PathVariable Long id,
                              @RequestParam(defaultValue = "false") boolean deleteExpenses) {
        accountService.deleteAccount(appUser, id, deleteExpenses);
    }
}
