package jedrzychowski.szymon.expense_tracker.finder;

import jedrzychowski.szymon.expense_tracker.config.exception.DataNotFoundException;
import jedrzychowski.szymon.expense_tracker.entity.ExpenseType;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseTypeRepository;

public class ExpenseTypeFinder {

    private final ExpenseTypeRepository expenseTypeRepository;

    public ExpenseTypeFinder(ExpenseTypeRepository expenseTypeRepository) {
        this.expenseTypeRepository = expenseTypeRepository;
    }

    public ExpenseType findById(long id) throws
                                                    DataNotFoundException {
        return expenseTypeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Cannot find Expense Type with ID: %d.", id)
                ));
    }

}
