package jedrzychowski.szymon.expense_tracker.service;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.DataConflictException;
import jedrzychowski.szymon.expense_tracker.config.exception.DataNotFoundException;
import jedrzychowski.szymon.expense_tracker.config.exception.UnauthorizedUserAccessException;
import jedrzychowski.szymon.expense_tracker.entity.dto.account.CreateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.account.UpdateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;

    public AccountService(AccountRepository accountRepository,
                          ExpenseRepository expenseRepository) {
        this.accountRepository = accountRepository;
        this.expenseRepository = expenseRepository;
    }

    public List<Account> getAllAccounts(AppUser appUser) {
        return accountRepository.findAllByAppUser(appUser);
    }

    public Account getAccountById(AppUser appUser,
                                  Long id) throws DataNotFoundException, UnauthorizedUserAccessException {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Cannot find Account with ID: %d.", id)
                ));
        account.validateIfAccountIsOwnedByCurrentUser(appUser);
        return account;
    }

    public Account createAccount(AppUser appUser,
                                 @Valid CreateAccountRequestDTO createAccountRequestDTO) {
        //Create Account based on the DTO
        Account newAccount = new Account(createAccountRequestDTO, appUser);
        return accountRepository.save(newAccount);
    }


    public Account updateAccount(AppUser appUser,
                                 @Valid UpdateAccountRequestDTO updateAccountRequestDTO) throws DataNotFoundException, UnauthorizedUserAccessException {
        //Find account to update
        Account accountToUpdate = accountRepository.findById(updateAccountRequestDTO.id())
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Cannot find Account with ID: %d.", updateAccountRequestDTO.id())
                ));
        accountToUpdate.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Update account based on the DTO
        accountToUpdate.update(updateAccountRequestDTO);
        return accountRepository.save(accountToUpdate);
    }

    public void deleteAccount(AppUser appUser,
                              Long id,
                              boolean deleteExpenses) throws DataNotFoundException, DataConflictException, UnauthorizedUserAccessException {
        //Find Account to delete
        Account accountToDelete = accountRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Cannot find Account with ID: %d.", id)
                ));

        accountToDelete.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Validate that no expenses exist with this Account
        int expensesWithAccount = expenseRepository.countByAccount(accountToDelete);
        if (expensesWithAccount > 0 && !deleteExpenses) {
            throw new DataConflictException(
                    String.format("Account with ID: %d and Name: %s is used by existing Expense records.",
                            accountToDelete.getId(), accountToDelete.getName())
            );
        }

        accountRepository.delete(accountToDelete);
    }
}
