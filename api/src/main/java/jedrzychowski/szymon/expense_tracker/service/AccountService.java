package jedrzychowski.szymon.expense_tracker.service;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.DataConflictException;
import jedrzychowski.szymon.expense_tracker.config.exception.DataNotFoundException;
import jedrzychowski.szymon.expense_tracker.config.exception.UnauthorizedUserAccessException;
import jedrzychowski.szymon.expense_tracker.entity.dto.account.CreateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.account.UpdateAccountRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.Account;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.finder.AccountFinder;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;
    private final AccountFinder accountFinder;

    public AccountService(AccountRepository accountRepository,
                          ExpenseRepository expenseRepository) {
        this.accountRepository = accountRepository;
        this.expenseRepository = expenseRepository;
        this.accountFinder = new AccountFinder(accountRepository);
    }

    public List<Account> getAllAccounts(AppUser appUser) {
        return accountRepository.findAllByAppUser(appUser);
    }

    public Account getAccountById(AppUser appUser,
                                  Long id) throws
                                           DataNotFoundException,
                                           UnauthorizedUserAccessException {
        Account account = accountFinder.findById(id);
        account.validateIfAccountIsOwnedByCurrentUser(appUser);
        return account;
    }

    public Account createAccount(AppUser appUser,
                                 @Valid CreateAccountRequestDTO createAccountRequestDTO) throws
                                                                                         DataConflictException {
        if (accountRepository.existsByNameAndAppUser(createAccountRequestDTO.name(), appUser)) {
            throw new DataConflictException(
                    String.format("Account with name %s exists for current user.", createAccountRequestDTO.name())
            );
        }
        Account newAccount = new Account(createAccountRequestDTO, appUser);
        return accountRepository.save(newAccount);
    }


    public Account updateAccount(AppUser appUser,
                                 @Valid UpdateAccountRequestDTO updateAccountRequestDTO) throws
                                                                                         DataNotFoundException,
                                                                                         UnauthorizedUserAccessException {
        Account accountToUpdate = accountFinder.findById(updateAccountRequestDTO.id());
        accountToUpdate.validateIfAccountIsOwnedByCurrentUser(appUser);

        accountToUpdate.update(updateAccountRequestDTO);
        return accountRepository.save(accountToUpdate);
    }

    public void deleteAccount(AppUser appUser,
                              Long id,
                              boolean deleteExpenses) throws
                                                      DataNotFoundException,
                                                      DataConflictException,
                                                      UnauthorizedUserAccessException {
        Account accountToDelete = accountFinder.findById(id);
        accountToDelete.validateIfAccountIsOwnedByCurrentUser(appUser);

        if (!deleteExpenses && expenseRepository.countByAccount(accountToDelete) > 0) {
            throw new DataConflictException(
                    String.format("Account with ID: %d and Name: %s is used by existing Expense records.",
                            accountToDelete.getId(), accountToDelete.getName())
            );
        }
        accountRepository.delete(accountToDelete);
    }
}
