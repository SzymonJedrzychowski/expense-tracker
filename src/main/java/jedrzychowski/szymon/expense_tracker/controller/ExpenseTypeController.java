package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.ReasonedResponseStatusException;
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
     * Get ExpenseTypes. These can be filtered by: Account ID.
     *
     * @param appUser   Currently Authorized AppUser.
     * @param accountId ID of Account used to filter.
     * @return List of ExpenseTypes.
     * @throws ReasonedResponseStatusException when no Account with accountId ID is found.
     */
    @GetMapping
    public List<ExpenseType> getAllExpenseTypes(@AuthenticationPrincipal AppUser appUser,
                                    @RequestParam(required = false) Long accountId) {
       return expenseTypeService.getAllExpenseTypes(appUser, accountId);
    }

    /**
     * Gets the ExpenseType by specified ID.
     *
     * @param appUser Currently Authorized AppUser.
     * @param id      of specific ExpenseType.
     * @return ExpenseType with specified ID.
     * @throws ReasonedResponseStatusException if no ExpenseType with specific ID is found.
     */
    @GetMapping("/{id}")
    public ExpenseType getExpenseTypeById(@AuthenticationPrincipal AppUser appUser,
                                          @PathVariable Long id) {
        return expenseTypeService.getExpenseTypeById(appUser, id);
    }

    /**
     * Creates new ExpenseType.
     *
     * @param appUser                     Currently Authorized AppUser.
     * @param createExpenseTypeRequestDTO CreateExpenseTypeRequestDAO of the Account that is being created.
     * @return ExpenseType that was created.
     * @throws ReasonedResponseStatusException if validation fails or data is not found.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseType createExpenseType(@AuthenticationPrincipal AppUser appUser,
                                      @RequestBody @Valid CreateExpenseTypeRequestDTO createExpenseTypeRequestDTO) {
        return expenseTypeService.createExpenseType(appUser, createExpenseTypeRequestDTO);
    }

    /**
     * Updates ExpenseType.
     *
     * @param appUser                     Currently Authorized AppUser.
     * @param updateExpenseTypeRequestDTO UpdateExpenseTypeRequestDAO with information about updated ExpenseType.
     * @return Updated ExpenseType.
     * @throws ReasonedResponseStatusException if validation fails or ExpenseType cannot be updated.
     */
    @PutMapping
    public ExpenseType updateExpenseType(@AuthenticationPrincipal AppUser appUser,
                                         @RequestBody @Valid UpdateExpenseTypeRequestDTO updateExpenseTypeRequestDTO) {
        return expenseTypeService.updateExpenseType(appUser, updateExpenseTypeRequestDTO);
    }

    /**
     * Deletes ExpenseType.
     *
     * @param appUser Currently Authorized AppUser.
     * @param id      of ExpenseType to delete.
     * @throws ReasonedResponseStatusException if ExpenseType cannot be found or deleted.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpenseType(@AuthenticationPrincipal AppUser appUser,
                                  @PathVariable Long id) {
        expenseTypeService.deleteExpenseType(appUser, id);
    }
}
