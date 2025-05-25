package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.dto.expenseType.CreateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.dto.expenseType.UpdateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.entity.ExpenseType;
import jedrzychowski.szymon.expense_tracker.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("expense-tracker/v1/expense-types")
public class ExpenseTypeController {

    @Autowired
    private ExpenseTypeRepository expenseTypeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Get ExpenseTypes. These can be filtered by: Account ID.
     *
     * @param appUser   Currently Authorized AppUser.
     * @param accountId ID of Account used to filter.
     * @return List of ExpenseTypes.
     * @throws ReasonedResponseStatusException when no Account with accountId ID is found.
     */
    @GetMapping
    public List<ExpenseType> getAll(@AuthenticationPrincipal AppUser appUser,
                                    @RequestParam(required = false) Long accountId) {
        //Return all without filtering
        if (accountId == null) {
            return expenseTypeRepository.findAllByAccount_AppUser(appUser);
        }

        //Check if Account with specific ID exists.
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID: %d", accountId)
                ));

        account.validateIfAccountIsOwnedByCurrentUser(appUser);

        return expenseTypeRepository.findAllByAccountId(accountId);
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
        ExpenseType expenseType = expenseTypeRepository.findById(id)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find ExpenseType with ID: %d", id)
                ));

        expenseType.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);

        return expenseType;
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
    public ExpenseType addExpenseType(@AuthenticationPrincipal AppUser appUser,
                                      @RequestBody @Valid CreateExpenseTypeRequestDTO createExpenseTypeRequestDTO) {
        //Find Account for the ExpenseType
        Account account = accountRepository.findById(createExpenseTypeRequestDTO.getAccountId())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID: %d", createExpenseTypeRequestDTO.getAccountId())
                ));

        account.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Find if Expense Type would be a duplicate
        if (expenseTypeRepository.existsByNameAndAccountId(
                createExpenseTypeRequestDTO.getName(), createExpenseTypeRequestDTO.getAccountId())) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("ExpenseType with name: %s already exists for Account with ID: %d",
                            createExpenseTypeRequestDTO.getName(), createExpenseTypeRequestDTO.getAccountId())
            );
        }

        //Create the ExpenseType
        ExpenseType expenseType = new ExpenseType(createExpenseTypeRequestDTO, account);

        return expenseTypeRepository.save(expenseType);
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
        //Find the ExpenseType to update
        ExpenseType expenseTypeToUpdate = expenseTypeRepository.findById(updateExpenseTypeRequestDTO.getId())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find ExpenseType with ID %d.", updateExpenseTypeRequestDTO.getId())
                ));

        expenseTypeToUpdate.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);

        //Find the account to be updated for ExpenseType
        Account newAccount = accountRepository.findById(updateExpenseTypeRequestDTO.getAccountId())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID %d.", updateExpenseTypeRequestDTO.getAccountId())
                ));

        newAccount.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Check if the ExpenseType is used by previous Account (if Account changes)
        if (!Objects.equals(updateExpenseTypeRequestDTO.getAccountId(), expenseTypeToUpdate.getAccount().getId())
                && expenseRepository.existsByExpenseType(expenseTypeToUpdate)) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Account with Name: %s has existing Expenses with ExpenseType with Name: %s.",
                            expenseTypeToUpdate.getAccount().getName(), expenseTypeToUpdate.getName()));
        }

        //Check for duplicates
        if (expenseTypeRepository.existsByNameAndAccountId(
                updateExpenseTypeRequestDTO.getName(), updateExpenseTypeRequestDTO.getAccountId()
        )) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("ExpenseType with name: %s already exists for Account with ID: %d",
                            updateExpenseTypeRequestDTO.getName(), updateExpenseTypeRequestDTO.getAccountId()
                    ));
        }

        //Update the ExpenseType
        expenseTypeToUpdate.updateExpenseType(updateExpenseTypeRequestDTO, newAccount);

        return expenseTypeRepository.save(expenseTypeToUpdate);
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
        //Find the ExpenseType to update
        ExpenseType expenseTypeToDelete = expenseTypeRepository.findById(id)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find ExpenseType with ID %d.", id)
                ));

        expenseTypeToDelete.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);

        //Check if the ExpenseType is used by previous Account (if Account changes)
        if (expenseRepository.existsByExpenseType(expenseTypeToDelete)) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Account with Name: %s has existing Expenses with ExpenseType with Name: %s.",
                            expenseTypeToDelete.getAccount().getName(), expenseTypeToDelete.getName()));
        }

        expenseTypeRepository.delete(expenseTypeToDelete);
    }
}
