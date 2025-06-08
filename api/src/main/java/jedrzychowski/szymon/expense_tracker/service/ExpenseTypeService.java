package jedrzychowski.szymon.expense_tracker.service;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.*;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.entity.ExpenseType;
import jedrzychowski.szymon.expense_tracker.entity.dto.expenseType.CreateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.expenseType.UpdateExpenseTypeRequestDTO;
import jedrzychowski.szymon.expense_tracker.finder.AccountFinder;
import jedrzychowski.szymon.expense_tracker.finder.ExpenseTypeFinder;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseTypeService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseTypeRepository expenseTypeRepository;
    private final AccountFinder accountFinder;
    private final ExpenseTypeFinder expenseTypeFinder;

    public ExpenseTypeService(AccountRepository accountRepository,
                              ExpenseRepository expenseRepository,
                              ExpenseTypeRepository expenseTypeRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseTypeRepository = expenseTypeRepository;
        this.accountFinder = new AccountFinder(accountRepository);
        this.expenseTypeFinder = new ExpenseTypeFinder(expenseTypeRepository);
    }

    public List<ExpenseType> getAllExpenseTypes(AppUser appUser,
                                                Long accountId) throws
                                                                DataNotFoundException,
                                                                UnauthorizedUserAccessException {
        if (accountId == null) {
            return expenseTypeRepository.findAllByAccount_AppUser(appUser);
        }

        Account account = accountFinder.findById(accountId);
        account.validateIfAccountIsOwnedByCurrentUser(appUser);

        return expenseTypeRepository.findAllByAccountId(accountId);
    }

    public ExpenseType getExpenseTypeById(AppUser appUser,
                                          Long id) throws
                                                   DataNotFoundException,
                                                   UnauthorizedUserAccessException {
        ExpenseType expenseType = expenseTypeFinder.findById(id);
        expenseType.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);
        return expenseType;
    }

    public ExpenseType createExpenseType(AppUser appUser,
                                         @Valid CreateExpenseTypeRequestDTO createExpenseTypeRequestDTO) throws
                                                                                                         DataNotFoundException,
                                                                                                         UnauthorizedUserAccessException,
                                                                                                         DataConflictException {
        Account account = accountFinder.findById(createExpenseTypeRequestDTO.accountId());
        account.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Find if Expense Type would be a duplicate
        if (expenseTypeRepository.existsByNameAndAccountId(
                createExpenseTypeRequestDTO.name(), createExpenseTypeRequestDTO.accountId())) {
            throw new DataConflictException(
                    String.format("ExpenseType with name: %s already exists for Account with ID: %d.",
                            createExpenseTypeRequestDTO.name(), createExpenseTypeRequestDTO.accountId())
            );
        }

        ExpenseType expenseType = new ExpenseType(createExpenseTypeRequestDTO, account);
        return expenseTypeRepository.save(expenseType);
    }

    public ExpenseType updateExpenseType(AppUser appUser,
                                         @Valid UpdateExpenseTypeRequestDTO updateExpenseTypeRequestDTO) throws
                                                                                                         DataNotFoundException,
                                                                                                         UnauthorizedUserAccessException,
                                                                                                         DataConflictException {
        ExpenseType expenseTypeToUpdate = expenseTypeFinder.findById(updateExpenseTypeRequestDTO.id());
        expenseTypeToUpdate.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);

        Account newAccount = accountFinder.findById(updateExpenseTypeRequestDTO.accountId());
        newAccount.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Check if the ExpenseType is used by previous Account (if Account changes)
        if (!updateExpenseTypeRequestDTO.accountId().equals(expenseTypeToUpdate.getAccount().getId())) {
            validateForExistingExpenseType(expenseTypeToUpdate);
        }

        //Check for duplicates
        if (expenseTypeRepository.existsByNameAndAccountId(
                updateExpenseTypeRequestDTO.name(), updateExpenseTypeRequestDTO.accountId()
        )) {
            throw new DataConflictException(
                    String.format("ExpenseType with name: %s already exists for Account with ID: %d",
                            updateExpenseTypeRequestDTO.name(), updateExpenseTypeRequestDTO.accountId()
                    ));
        }

        expenseTypeToUpdate.updateExpenseType(updateExpenseTypeRequestDTO, newAccount);
        return expenseTypeRepository.save(expenseTypeToUpdate);
    }

    public void deleteExpenseType(AppUser appUser,
                                  Long id) throws
                                           DataNotFoundException,
                                           UnauthorizedUserAccessException,
                                           DataConflictException {
        ExpenseType expenseTypeToDelete = expenseTypeFinder.findById(id);
        expenseTypeToDelete.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);

        validateForExistingExpenseType(expenseTypeToDelete);
        expenseTypeRepository.delete(expenseTypeToDelete);
    }

    private void validateForExistingExpenseType(ExpenseType expenseType) throws
                                                                         DataConflictException {
        if (expenseRepository.existsByExpenseType(expenseType)) {
            throw new DataConflictException(
                    String.format("Account with Name: %s has existing Expenses with ExpenseType with Name: %s.",
                            expenseType.getAccount().getName(), expenseType.getName()));
        }

    }
}
