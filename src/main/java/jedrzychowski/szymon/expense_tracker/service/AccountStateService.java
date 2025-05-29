package jedrzychowski.szymon.expense_tracker.service;

import jedrzychowski.szymon.expense_tracker.config.exception.*;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AccountState;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.AccountStateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountStateService {

    private final AccountRepository accountRepository;
    private final AccountStateRepository accountStateRepository;

    public AccountStateService(AccountRepository accountRepository,
                               AccountStateRepository accountStateRepository) {
        this.accountRepository = accountRepository;
        this.accountStateRepository = accountStateRepository;
    }


    public List<AccountState> getAllAccountStates(AppUser appUser,
                                                  Long accountId,
                                                  LocalDate startDate,
                                                  LocalDate endDate) throws DataNotFoundException, ParamValidationException, UnauthorizedUserAccessException {
        //Update startDate and endDate in case of null values
        startDate = startDate == null ? LocalDate.of(0, 1, 1) : startDate;
        endDate = endDate == null ? LocalDate.of(9999, 12, 31) : endDate;

        if (startDate.isAfter(endDate)) {
            throw new ParamValidationException(
                    String.format("startDate (%s) cannot be after endDate (%s).", startDate, endDate)
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
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Cannot find Account with ID: %d.", accountId)
                ));
        account.validateIfAccountIsOwnedByCurrentUser(appUser);
        return applyDateFilter(accountStateRepository.findAllByAccountIdOrderByDateAsc(accountId), startDate, endDate);
    }

    public AccountState getAccountStateById(AppUser appUser,
                                            Long id) throws DataNotFoundException, UnauthorizedUserAccessException {
        AccountState accountState = accountStateRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Cannot find Account State with ID: %d.", id)
                ));
        accountState.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);
        return accountState;
    }

    /**
     * Filters the List of AccountStates with dates.
     *
     * @param accountStates List of AccountStates to filter.
     * @param startDate     First date that will be included in the List of AccountStates.
     * @param endDate       Last date that will be included in the List of AccountStates.
     * @return Filtered List of AccountStates.
     */
    private List<AccountState> applyDateFilter(List<AccountState> accountStates,
                                               LocalDate startDate,
                                               LocalDate endDate) {
        return accountStates.stream().filter(expense -> {
            LocalDate expenseDate = expense.getDate();
            return (!startDate.isAfter(expenseDate) && !endDate.isBefore(expenseDate));
        }).toList();
    }
}
