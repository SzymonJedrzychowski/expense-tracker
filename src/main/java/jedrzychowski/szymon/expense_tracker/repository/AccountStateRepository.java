package jedrzychowski.szymon.expense_tracker.repository;

import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AccountState;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountStateRepository extends JpaRepository<AccountState, Long> {

    Optional<AccountState> findByDate(LocalDate date);

    List<AccountState> findAllByAccountIdOrderByDateAsc(Long accountId);

    List<AccountState> findAllByAccountAndDateGreaterThanOrderByDateAsc(Account account,
                                                                        LocalDate date);

    List<AccountState> findAllByAccount_AppUserOrderByDateAsc(AppUser appUser);

    Optional<AccountState> findFirstByAccountAndDateBeforeOrderByDateDesc(Account account,
                                                                          LocalDate date);
}
