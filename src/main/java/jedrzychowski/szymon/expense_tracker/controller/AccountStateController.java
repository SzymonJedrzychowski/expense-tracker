package jedrzychowski.szymon.expense_tracker.controller;

import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AccountState;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.AccountStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("expense-tracker/v1/account-states")
public class AccountStateController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountStateRepository accountStateRepository;

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
    public List<AccountState> getAll(
            @AuthenticationPrincipal AppUser appUser,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        //Update startDate and endDate in case of null values
        startDate = startDate == null ? LocalDate.of(0, 1, 1) : startDate;
        endDate = endDate == null ? LocalDate.of(9999, 12, 31) : endDate;

        if (startDate.isAfter(endDate)) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("startDate (%s) cannot be after endDate (%s)", startDate, endDate)
            );
        }

        //Return all without filtering
        if (accountId == null) {
            return applyDateFilter(
                    accountStateRepository.findAllByAccount_AppUserOrderByDateAsc(appUser), startDate, endDate
            );
        }

        //Check if Account with specific ID exists.
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID: %d", accountId)
                ));
        account.validateIfAccountIsOwnedByCurrentUser(appUser);
        return applyDateFilter(accountStateRepository.findAllByAccountIdOrderByDateAsc(accountId), startDate, endDate);
    }

    /**
     * Gets the AccountState by specified ID.
     *
     * @param appUser   Currently Authorized AppUser.
     * @param id of specific AccountState.
     * @return AccountState with specified ID.
     * @throws ReasonedResponseStatusException if no AccountState with specific ID is found.
     */
    @GetMapping("/{id}")
    public AccountState getAccountStateById(@AuthenticationPrincipal AppUser appUser,
                                            @PathVariable Long id) {
        AccountState accountState = accountStateRepository.findById(id)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account State with ID: %d", id)
                ));
        accountState.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);
        return accountState;
    }

    /**
     * Filters the List of Accountstates with dates.
     *
     * @param accountStates List of AccountStates to filter.
     * @param startDate     First date that will be included in the List of AccountStates.
     * @param endDate       Last date that will be included in the List of AccountStates.
     * @return Filtered List of AccountStates.
     */
    private List<AccountState> applyDateFilter(List<AccountState> accountStates, LocalDate startDate, LocalDate endDate) {
        return accountStates.stream().filter(expense -> {
            LocalDate expenseDate = expense.getDate();
            return (!startDate.isAfter(expenseDate) && !endDate.isBefore(expenseDate));
        }).toList();
    }

}
