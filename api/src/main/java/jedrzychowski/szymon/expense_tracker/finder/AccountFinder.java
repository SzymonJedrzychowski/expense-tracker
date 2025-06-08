package jedrzychowski.szymon.expense_tracker.finder;

import jedrzychowski.szymon.expense_tracker.config.exception.DataNotFoundException;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;

public class AccountFinder {

    private final AccountRepository accountRepository;

    public AccountFinder(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account findById(long id) throws
                                            DataNotFoundException {
        return accountRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Cannot find Account with ID: %d.", id)
                ));
    }

}
