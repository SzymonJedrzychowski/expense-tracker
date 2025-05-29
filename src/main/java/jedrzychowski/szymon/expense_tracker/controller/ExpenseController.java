package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.*;
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
     * Retrieves expenses, optionally filtered by Account ID and date range.
     *
     * @param appUser   the currently authorized AppUser
     * @param accountId optional ID of the Account to filter expenses by
     * @param startDate optional start date to filter expenses
     * @param endDate   optional end date to filter expenses
     * @return a list of expenses matching the filters
     * @throws ParamValidationException        if startDate is later than endDate
     * @throws DataNotFoundException           if no Account with the specified accountId is found
     * @throws UnauthorizedUserAccessException if the Account does not belong to the authorized user
     */
    @GetMapping
    public List<Expense> getAllExpenses(@AuthenticationPrincipal AppUser appUser,
                                        @RequestParam(required = false) Long accountId,
                                        @RequestParam(required = false) LocalDate startDate,
                                        @RequestParam(required = false) LocalDate endDate) {
        return expenseService.getAllExpenses(appUser, accountId, startDate, endDate);
    }

    /**
     * Retrieves the Expense with the specified ID.
     *
     * @param appUser the currently authorized AppUser
     * @param id      the ID of the Expense to retrieve
     * @return the Expense with the specified ID
     * @throws DataNotFoundException           if no Expense with the specified ID is found
     * @throws UnauthorizedUserAccessException if the Expense does not belong to the authorized user
     */
    @GetMapping("/{id}")
    public Expense getExpenseById(@AuthenticationPrincipal AppUser appUser,
                                  @PathVariable Long id) {
        return expenseService.getExpenseById(appUser, id);
    }

    /**
     * Creates a new Expense.
     *
     * @param appUser                 the currently authorized AppUser
     * @param createExpenseRequestDTO the details of the Expense to be created
     * @return the created Expense
     * @throws DataValidationException         if validation of createExpenseRequestDTO fails
     * @throws DataNotFoundException           if no Expense Type or Account with specified IDs is found
     * @throws UnauthorizedUserAccessException if the Account does not belong to the authorized user
     * @throws ForbiddenActionException        if Expense Type with specified ID cannot be used for specified Account
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense createExpense(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid CreateExpenseRequestDTO createExpenseRequestDTO) {
        return expenseService.createExpense(appUser, createExpenseRequestDTO);
    }

    /**
     * Updates an existing Expense.
     *
     * @param appUser                 the currently authorized AppUser
     * @param updateExpenseRequestDTO DTO containing updated Expense information
     * @return the updated Expense
     * @throws DataValidationException         if validation of updateExpenseRequestDTO fails
     * @throws DataNotFoundException           if no Expense, Expense Type or Account with specified IDs is found
     * @throws UnauthorizedUserAccessException if the Account or Expense do not belong to the authorized user
     * @throws ForbiddenActionException        if Expense Type with specified ID cannot be used for specified Account
     */
    @PutMapping
    public Expense updateExpense(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody @Valid UpdateExpenseRequestDTO updateExpenseRequestDTO) {
        return expenseService.updateExpense(appUser, updateExpenseRequestDTO);
    }

    /**
     * Deletes the Expense with the specified ID.
     *
     * @param appUser the currently authorized AppUser
     * @param id      the ID of the Expense to remove
     * @throws ReasonedResponseStatusException if the Expense cannot be found
     * @throws UnauthorizedUserAccessException if the Expense does not belong to the authorized user
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@AuthenticationPrincipal AppUser appUser,
                              @PathVariable Long id) {
        expenseService.deleteExpense(appUser, id);
    }
}
