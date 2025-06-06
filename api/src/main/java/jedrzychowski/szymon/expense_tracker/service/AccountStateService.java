package jedrzychowski.szymon.expense_tracker.service;

import jedrzychowski.szymon.expense_tracker.config.exception.*;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AccountState;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.finder.AccountFinder;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.AccountStateRepository;
import jedrzychowski.szymon.expense_tracker.util.DateUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountStateService {

    private final AccountStateRepository accountStateRepository;
    private final AccountFinder accountFinder;

    public AccountStateService(AccountRepository accountRepository,
                               AccountStateRepository accountStateRepository) {
        this.accountStateRepository = accountStateRepository;
        this.accountFinder = new AccountFinder(accountRepository);
    }


    public List<AccountState> getAllAccountStates(AppUser appUser,
                                                  Long accountId,
                                                  LocalDate startDate,
                                                  LocalDate endDate) throws
                                                                     DataNotFoundException,
                                                                     ParamValidationException,
                                                                     UnauthorizedUserAccessException {

        Pair<LocalDate, LocalDate> dates = DateUtil.validateDateParams(startDate, endDate);
        startDate = dates.getLeft();
        endDate = dates.getRight();

        if (accountId == null) {
            return accountStateRepository.findAllByAccount_AppUserAndDateBetweenOrderByDateAsc(appUser, startDate, endDate);
        }

        Account account = accountFinder.findById(accountId);
        account.validateIfAccountIsOwnedByCurrentUser(appUser);
        return accountStateRepository.findAllByAccountAndDateBetweenOrderByDateAsc(account, startDate, endDate);
    }

    public AccountState getAccountStateById(AppUser appUser,
                                            Long id) throws
                                                     DataNotFoundException,
                                                     UnauthorizedUserAccessException {
        AccountState accountState = accountStateRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Cannot find Account State with ID: %d.", id)
                ));
        accountState.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);
        return accountState;
    }
}
