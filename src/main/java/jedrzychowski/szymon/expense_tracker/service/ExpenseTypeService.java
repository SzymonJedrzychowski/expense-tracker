package jedrzychowski.szymon.expense_tracker.service;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.entity.ExpenseType;
import jedrzychowski.szymon.expense_tracker.entity.dto.expenseType.CreateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.expenseType.UpdateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ExpenseTypeService {

    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseTypeRepository expenseTypeRepository;

    public ExpenseTypeService(AccountRepository accountRepository,
                              ExpenseRepository expenseRepository,
                              ExpenseTypeRepository expenseTypeRepository) {
        this.accountRepository = accountRepository;
        this.expenseRepository = expenseRepository;
        this.expenseTypeRepository = expenseTypeRepository;
    }

    public List<ExpenseType> getAllExpenseTypes(AppUser appUser,
                                                Long accountId) {
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

    public ExpenseType getExpenseTypeById(AppUser appUser,
                                          Long id) {
        ExpenseType expenseType = expenseTypeRepository.findById(id)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find ExpenseType with ID: %d", id)
                ));

        expenseType.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);
        return expenseType;
    }

    public ExpenseType createExpenseType(AppUser appUser,
                                         @Valid CreateExpenseTypeRequestDTO createExpenseTypeRequestDTO) {
        //Find Account for the ExpenseType
        Account account = accountRepository.findById(createExpenseTypeRequestDTO.accountId())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID: %d", createExpenseTypeRequestDTO.accountId())
                ));

        account.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Find if Expense Type would be a duplicate
        if (expenseTypeRepository.existsByNameAndAccountId(
                createExpenseTypeRequestDTO.name(), createExpenseTypeRequestDTO.accountId())) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("ExpenseType with name: %s already exists for Account with ID: %d",
                            createExpenseTypeRequestDTO.name(), createExpenseTypeRequestDTO.accountId())
            );
        }

        //Create the ExpenseType
        ExpenseType expenseType = new ExpenseType(createExpenseTypeRequestDTO, account);

        return expenseTypeRepository.save(expenseType);
    }

    public ExpenseType updateExpenseType(AppUser appUser,
                                         @Valid UpdateExpenseTypeRequestDTO updateExpenseTypeRequestDTO) {
        //Find the ExpenseType to update
        ExpenseType expenseTypeToUpdate = expenseTypeRepository.findById(updateExpenseTypeRequestDTO.id())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find ExpenseType with ID %d.", updateExpenseTypeRequestDTO.id())
                ));

        expenseTypeToUpdate.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);

        //Find the account to be updated for ExpenseType
        Account newAccount = accountRepository.findById(updateExpenseTypeRequestDTO.accountId())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID %d.", updateExpenseTypeRequestDTO.accountId())
                ));

        newAccount.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Check if the ExpenseType is used by previous Account (if Account changes)
        if (!Objects.equals(updateExpenseTypeRequestDTO.accountId(), expenseTypeToUpdate.getAccount().getId())
                && expenseRepository.existsByExpenseType(expenseTypeToUpdate)) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Account with Name: %s has existing Expenses with ExpenseType with Name: %s.",
                            expenseTypeToUpdate.getAccount().getName(), expenseTypeToUpdate.getName()));
        }

        //Check for duplicates
        if (expenseTypeRepository.existsByNameAndAccountId(
                updateExpenseTypeRequestDTO.name(), updateExpenseTypeRequestDTO.accountId()
        )) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("ExpenseType with name: %s already exists for Account with ID: %d",
                            updateExpenseTypeRequestDTO.name(), updateExpenseTypeRequestDTO.accountId()
                    ));
        }

        //Update the ExpenseType
        expenseTypeToUpdate.updateExpenseType(updateExpenseTypeRequestDTO, newAccount);

        return expenseTypeRepository.save(expenseTypeToUpdate);
    }

    public void deleteExpenseType(AppUser appUser,
                                  Long id) {
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
