package jedrzychowski.szymon.expense_tracker.finder;

import jedrzychowski.szymon.expense_tracker.config.exception.DataNotFoundException;
import jedrzychowski.szymon.expense_tracker.entity.Expense;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseRepository;

public class ExpenseFinder {

    private final ExpenseRepository expenseRepository;

    public ExpenseFinder(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense findById(long id) throws
                                     DataNotFoundException {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Cannot find Expense with ID: %d", id)
                ));
    }
}
