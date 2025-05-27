package jedrzychowski.szymon.expense_tracker.controller;

import jedrzychowski.szymon.expense_tracker.config.exception.ReasonedResponseStatusException;
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
     * Get AccountStates. These can be filtered by: Account ID.
     *
     * @param appUser   Currently Authorized AppUser.
     * @param accountId ID of Account used to filter.
     * @param startDate Starting LocalDate used for the filter.
     * @param endDate   Ending LocalDate used for the filter.
     * @return List of AccountStates.
     * @throws ReasonedResponseStatusException when no Account with accountId ID is found.
     */
    @GetMapping
    public List<AccountState> getAllAccountStates(@AuthenticationPrincipal AppUser appUser,
                                                  @RequestParam(required = false) Long accountId,
                                                  @RequestParam(required = false) LocalDate startDate,
                                                  @RequestParam(required = false) LocalDate endDate) {
        return accountStateService.getAllAccountStates(appUser, accountId, startDate, endDate);
    }

    /**
     * Gets the AccountState by specified ID.
     *
     * @param appUser Currently Authorized AppUser.
     * @param id      of specific AccountState.
     * @return AccountState with specified ID.
     * @throws ReasonedResponseStatusException if no AccountState with specific ID is found.
     */
    @GetMapping("/{id}")
    public AccountState getAccountStateById(@AuthenticationPrincipal AppUser appUser,
                                            @PathVariable Long id) {
        return accountStateService.getAccountStateById(appUser, id);
    }
}
