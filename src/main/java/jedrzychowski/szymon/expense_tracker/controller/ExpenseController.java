package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.entity.dto.expense.CreateExpenseRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.expense.UpdateExpenseRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.*;
import jedrzychowski.szymon.expense_tracker.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("expense-tracker/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Get Expenses. These can be filtered by: Account ID, Start Date and End Date.
     *
     * @param appUser   Currently Authorized AppUser.
     * @param accountId ID of Account used to filter.
     * @param startDate Starting LocalDate of AccountState used to filter.
     * @param endDate   Ending LocalDate of AccountState used to filter.
     * @return List of Expense.
     * @throws ReasonedResponseStatusException when no Account with accountId ID is found.
     */
    @GetMapping
    public List<Expense> getAllExpenses(@AuthenticationPrincipal AppUser appUser,
                                @RequestParam(required = false) Long accountId,
                                @RequestParam(required = false) LocalDate startDate,
                                @RequestParam(required = false) LocalDate endDate) {
        return expenseService.getAllExpenses(appUser, accountId, startDate, endDate);
    }

    /**
     * Gets the Expense by specified ID.
     *
     * @param appUser Currently Authorized AppUser.
     * @param id      of specific Expense.
     * @return Expense with specified ID.
     * @throws ReasonedResponseStatusException if no Expense with specific ID is found.
     */
    @GetMapping("/{id}")
    public Expense getExpenseById(@AuthenticationPrincipal AppUser appUser,
                                  @PathVariable Long id) {
        return expenseService.getExpenseById(appUser, id);
    }

    /**
     * Creates new Expense.
     *
     * @param appUser                 Currently Authorized AppUser.
     * @param createExpenseRequestDTO CreateExpenseRequestDTO of the Account that is being created.
     * @return Account that was created.
     * @throws ReasonedResponseStatusException if validation fails or data is not found.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense createExpense(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid CreateExpenseRequestDTO createExpenseRequestDTO) {
        return expenseService.createExpense(appUser, createExpenseRequestDTO);
    }

    /**
     * Updates Expense.
     *
     * @param appUser                 Currently Authorized AppUser.
     * @param updateExpenseRequestDTO UpdateAccountRequestDTO with information about updated Expense.
     * @return Updated Expense.
     * @throws ReasonedResponseStatusException if validation fails or Expense cannot be edited.
     */
    @PutMapping
    public Expense updateExpense(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid UpdateExpenseRequestDTO updateExpenseRequestDTO) {
        return expenseService.updateExpense(appUser, updateExpenseRequestDTO);
    }

    /**
     * Deletes the Expense.
     *
     * @param appUser Currently Authorized AppUser.
     * @param id      of Expense to remove.
     * @throws ReasonedResponseStatusException if Expense cannot be found.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@AuthenticationPrincipal AppUser appUser,
                              @PathVariable Long id) {
        expenseService.deleteExpense(appUser, id);
    }
}
