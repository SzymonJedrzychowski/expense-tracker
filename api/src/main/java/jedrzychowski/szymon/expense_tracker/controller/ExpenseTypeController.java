package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.DataConflictException;
import jedrzychowski.szymon.expense_tracker.config.exception.DataNotFoundException;
import jedrzychowski.szymon.expense_tracker.config.exception.UnauthorizedUserAccessException;
import jedrzychowski.szymon.expense_tracker.entity.dto.expenseType.CreateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.expenseType.UpdateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.entity.ExpenseType;
import jedrzychowski.szymon.expense_tracker.service.ExpenseTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("expense-tracker/v1/expense-types")
public class ExpenseTypeController {

    private final ExpenseTypeService expenseTypeService;

    public ExpenseTypeController(ExpenseTypeService expenseTypeService) {
        this.expenseTypeService = expenseTypeService;
    }

    /**
     * Retrieves ExpenseTypes, optionally filtered by Account ID.
     *
     * @param appUser   the currently authorized AppUser
     * @param accountId optional ID of the Account to filter ExpenseTypes by
     * @return a list of ExpenseTypes matching the filter
     * @throws DataNotFoundException           if no account with the specified accountId is found
     * @throws UnauthorizedUserAccessException if the account does not belong to the authorized user
     */
    @GetMapping
    public List<ExpenseType> getAllExpenseTypes(@AuthenticationPrincipal AppUser appUser,
                                                @RequestParam(required = false) Long accountId) {
        return expenseTypeService.getAllExpenseTypes(appUser, accountId);
    }

    /**
     * Retrieves the ExpenseType with the specified ID.
     *
     * @param appUser the currently authorized AppUser
     * @param id      the ID of the ExpenseType to retrieve
     * @return the ExpenseType with the specified ID
     * @throws DataNotFoundException           if no ExpenseType with the specified ID is found
     * @throws UnauthorizedUserAccessException if the ExpenseType does not belong to the authorized user
     */
    @GetMapping("/{id}")
    public ExpenseType getExpenseTypeById(@AuthenticationPrincipal AppUser appUser,
                                          @PathVariable Long id) {
        return expenseTypeService.getExpenseTypeById(appUser, id);
    }

    /**
     * Creates a new ExpenseType.
     *
     * @param appUser                     the currently authorized AppUser
     * @param createExpenseTypeRequestDTO the details of the ExpenseType to be created
     * @return the created ExpenseType
     * @throws DataNotFoundException           if no Account with the specified ID is found
     * @throws UnauthorizedUserAccessException if the Account does not belong to the authorized user
     * @throws DataConflictException           if Expense Type with duplicate name exists on the Account
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseType createExpenseType(@AuthenticationPrincipal AppUser appUser,
                                         @RequestBody @Valid CreateExpenseTypeRequestDTO createExpenseTypeRequestDTO) {
        return expenseTypeService.createExpenseType(appUser, createExpenseTypeRequestDTO);
    }

    /**
     * Updates an existing ExpenseType.
     *
     * @param appUser                     the currently authorized AppUser
     * @param updateExpenseTypeRequestDTO DTO containing updated ExpenseType information
     * @return the updated ExpenseType
     * @throws DataNotFoundException           if no ExpenseType or Account with the specified IDs are found
     * @throws UnauthorizedUserAccessException if the ExpenseType or Account do not belong to the authorized user
     * @throws DataConflictException           if Expense Type with duplicate name exists on the Account
     *                                         or Expenses exists on the Account and Account is changed
     */
    @PutMapping
    public ExpenseType updateExpenseType(@AuthenticationPrincipal AppUser appUser,
                                         @RequestBody @Valid UpdateExpenseTypeRequestDTO updateExpenseTypeRequestDTO) {
        return expenseTypeService.updateExpenseType(appUser, updateExpenseTypeRequestDTO);
    }

    /**
     * Deletes the ExpenseType with the specified ID.
     *
     * @param appUser the currently authorized AppUser
     * @param id      the ID of the ExpenseType to delete
     * @throws DataNotFoundException           if no ExpenseType with the specified ID is found
     * @throws UnauthorizedUserAccessException if the ExpenseType does not belong to the authorized user
     * @throws DataConflictException           if Expenses exists with specified ExpenseType
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpenseType(@AuthenticationPrincipal AppUser appUser,
                                  @PathVariable Long id) {
        expenseTypeService.deleteExpenseType(appUser, id);
    }
}
