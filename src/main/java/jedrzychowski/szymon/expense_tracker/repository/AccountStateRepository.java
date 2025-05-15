package jedrzychowski.szymon.expense_tracker.repository;

import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AccountState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountStateRepository extends JpaRepository<AccountState, Long> {

    Optional<AccountState> findByDate(LocalDate date);

    List<AccountState> findAllByAccountIdOrderByDateAsc(Long accountId);

    List<AccountState> findByAccountAndDateGreaterThanOrderByDateAsc(Account account, LocalDate date);

    Optional<AccountState> findFirstByAccountAndDateBeforeOrderByDateDesc(Account account, LocalDate date);
}
