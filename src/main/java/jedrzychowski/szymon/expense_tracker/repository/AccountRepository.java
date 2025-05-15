package jedrzychowski.szymon.expense_tracker.repository;

import jedrzychowski.szymon.expense_tracker.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Boolean existsByName(String name);

}
