package jedrzychowski.szymon.expense_tracker.repository;

import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.Expense;
import jedrzychowski.szymon.expense_tracker.entity.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByAccountIdOrderByAccountState_DateAsc(Long accountId);

    Integer countByAccount(Account account);

    Boolean existsByExpenseType(ExpenseType expenseType);
}