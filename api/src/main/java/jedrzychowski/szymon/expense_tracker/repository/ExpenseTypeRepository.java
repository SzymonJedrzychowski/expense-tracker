package jedrzychowski.szymon.expense_tracker.repository;

import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.entity.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseTypeRepository extends JpaRepository<ExpenseType, Long> {

    List<ExpenseType> findAllByAccountId(Long accountId);

    List<ExpenseType> findAllByAccount_AppUser(AppUser appUser);

    Boolean existsByNameAndAccountId(String name,
                                     Long accountId);
}
