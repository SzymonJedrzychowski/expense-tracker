package jedrzychowski.szymon.expense_tracker.controller;

import jedrzychowski.szymon.expense_tracker.config.exception.DataNotFoundException;
import jedrzychowski.szymon.expense_tracker.config.exception.ParamValidationException;
import jedrzychowski.szymon.expense_tracker.config.exception.UnauthorizedUserAccessException;
import jedrzychowski.szymon.expense_tracker.entity.AccountState;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.service.AccountStateService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("expense-tracker/v1/account-states")
public class AccountStateController {

    private final AccountStateService accountStateService;

    public AccountStateController(AccountStateService accountStateService) {
        this.accountStateService = accountStateService;
    }

    /**
     * Retrieves AccountStates, optionally filtered by Account ID and date range.
     *
     * @param appUser   the currently authorized AppUser
     * @param accountId optional ID of the Account to filter by
     * @param startDate optional start date for filtering AccountStates
     * @param endDate   optional end date for filtering AccountStates
     * @return a list of AccountStates matching the filters
     * @throws ParamValidationException        if startDate is later than endDate
     * @throws DataNotFoundException           if no account with the specified accountId is found
     * @throws UnauthorizedUserAccessException if the account does not belong to the authorized user
     */
    @GetMapping
    public List<AccountState> getAllAccountStates(@AuthenticationPrincipal AppUser appUser,
                                                  @RequestParam(required = false) Long accountId,
                                                  @RequestParam(required = false) LocalDate startDate,
                                                  @RequestParam(required = false) LocalDate endDate) {
        return accountStateService.getAllAccountStates(appUser, accountId, startDate, endDate);
    }

    /**
     * Retrieves the AccountState with the specified ID.
     *
     * @param appUser the currently authorized AppUser
     * @param id      the ID of the AccountState to retrieve
     * @return the AccountState with the specified ID
     * @throws DataNotFoundException           if no AccountState with the specified ID is found
     * @throws UnauthorizedUserAccessException if the account does not belong to the authorized user
     */
    @GetMapping("/{id}")
    public AccountState getAccountStateById(@AuthenticationPrincipal AppUser appUser,
                                            @PathVariable Long id) {
        return accountStateService.getAccountStateById(appUser, id);
    }
}
