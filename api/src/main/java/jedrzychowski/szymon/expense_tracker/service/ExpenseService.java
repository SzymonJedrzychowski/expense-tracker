package jedrzychowski.szymon.expense_tracker.service;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.*;
import jedrzychowski.szymon.expense_tracker.entity.*;
import jedrzychowski.szymon.expense_tracker.entity.dto.expense.CreateExpenseRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.expense.UpdateExpenseRequestDTO;
import jedrzychowski.szymon.expense_tracker.finder.AccountFinder;
import jedrzychowski.szymon.expense_tracker.finder.ExpenseFinder;
import jedrzychowski.szymon.expense_tracker.finder.ExpenseTypeFinder;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.AccountStateRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseTypeRepository;
import jedrzychowski.szymon.expense_tracker.util.DateUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ExpenseService {

    private final AccountStateRepository accountStateRepository;
    private final ExpenseRepository expenseRepository;
    private final AccountFinder accountFinder;
    private final ExpenseFinder expenseFinder;
    private final ExpenseTypeFinder expenseTypeFinder;

    public ExpenseService(AccountRepository accountRepository,
                          AccountStateRepository accountStateRepository,
                          ExpenseRepository expenseRepository,
                          ExpenseTypeRepository expenseTypeRepository) {
        this.accountStateRepository = accountStateRepository;
        this.expenseRepository = expenseRepository;
        this.accountFinder = new AccountFinder(accountRepository);
        this.expenseFinder = new ExpenseFinder(expenseRepository);
        this.expenseTypeFinder = new ExpenseTypeFinder(expenseTypeRepository);
    }


    public List<Expense> getAllExpenses(AppUser appUser,
                                        Long accountId,
                                        LocalDate startDate,
                                        LocalDate endDate) throws
                                                           ParamValidationException,
                                                           DataNotFoundException,
                                                           UnauthorizedUserAccessException {
        Pair<LocalDate, LocalDate> dates = DateUtil.validateDateParams(startDate, endDate);
        startDate = dates.getLeft();
        endDate = dates.getRight();

        if (accountId == null) {
            return applyDateFilter(
                    expenseRepository.findAllByAccount_AppUserOrderByAccountState_DateAsc(appUser), startDate, endDate
            );
        }

        Account account = accountFinder.findById(accountId);
        account.validateIfAccountIsOwnedByCurrentUser(appUser);

        return applyDateFilter(
                expenseRepository.findAllByAccountIdOrderByAccountState_DateAsc(accountId), startDate, endDate
        );
    }

    public Expense getExpenseById(AppUser appUser,
                                  Long id) throws
                                           DataNotFoundException,
                                           UnauthorizedUserAccessException {
        Expense expense = expenseFinder.findById(id);
        expense.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);
        return expense;
    }


    @Transactional
    public Expense createExpense(AppUser appUser,
                                 @Valid CreateExpenseRequestDTO createExpenseRequestDTO) throws
                                                                                         DataValidationException,
                                                                                         UnauthorizedUserAccessException,
                                                                                         DataNotFoundException,
                                                                                         ForbiddenActionException {
        List<String> validationResults = createExpenseRequestDTO.validateDTO();
        if (!validationResults.isEmpty()) {
            throw new DataValidationException(validationResults);
        }

        Account account = accountFinder.findById(createExpenseRequestDTO.accountId());
        account.validateIfAccountIsOwnedByCurrentUser(appUser);

        ExpenseType expenseType = expenseTypeFinder.findById(createExpenseRequestDTO.expenseTypeId());
        validateExpenseTypeAccount(expenseType, account);

        LocalDate date = createExpenseRequestDTO.date();
        float negativeMovement = -Math.min(createExpenseRequestDTO.movementAmount(), 0);
        float positiveMovement = Math.max(createExpenseRequestDTO.movementAmount(), 0);

        AccountState accountState = getOrCreateAccountStateForDate(date, account);
        accountState.setCurrentAmount(accountState.getCurrentAmount() + createExpenseRequestDTO.movementAmount());
        accountState.setNegativeMovement(accountState.getNegativeMovement() + negativeMovement);
        accountState.setPositiveMovement(accountState.getPositiveMovement() + positiveMovement);
        accountState.setRefundAmount(accountState.getRefundAmount() + createExpenseRequestDTO.getRefundAmount());

        Expense expenseToAdd = new Expense(createExpenseRequestDTO, expenseType, accountState, account);
        accountStateRepository.save(accountState);

        updateFutureAccountStates(accountState);
        return expenseRepository.save(expenseToAdd);
    }

    @Transactional
    public Expense updateExpense(AppUser appUser,
                                 @Valid UpdateExpenseRequestDTO updateExpenseRequestDTO) throws
                                                                                         DataValidationException,
                                                                                         DataNotFoundException,
                                                                                         UnauthorizedUserAccessException,
                                                                                         ForbiddenActionException {
        List<String> validationResults = updateExpenseRequestDTO.validateDTO();
        if (!validationResults.isEmpty()) {
            throw new DataValidationException(validationResults);
        }

        Expense expenseToUpdate = expenseFinder.findById(updateExpenseRequestDTO.id());

        Account account = expenseToUpdate.getAccount();
        account.validateIfAccountIsOwnedByCurrentUser(appUser);
        if (!Objects.equals(updateExpenseRequestDTO.accountId(), account.getId())) {
            account = accountFinder.findById(updateExpenseRequestDTO.accountId());
            account.validateIfAccountIsOwnedByCurrentUser(appUser);
        }

        ExpenseType expenseType = expenseToUpdate.getExpenseType();
        if (Objects.equals(updateExpenseRequestDTO.expenseTypeId(), expenseType.getId())) {
            expenseType = expenseTypeFinder.findById(updateExpenseRequestDTO.expenseTypeId());
        }

        validateExpenseTypeAccount(expenseType, account);

        LocalDate date = updateExpenseRequestDTO.date();
        AccountState updatedAccountState = expenseToUpdate.getAccountState();
        if (date != expenseToUpdate.getDate()) {
            updatedAccountState = getOrCreateAccountStateForDate(date, account);
        }

        AccountState previousAccountState;
        previousAccountState = expenseToUpdate.getAccountState();

        //Remove Expense values from previous AccountState
        if (previousAccountState != updatedAccountState) {
            previousAccountState.recalculateAmountsWithoutExpense(expenseToUpdate);
            accountStateRepository.save(previousAccountState);
        } else {
            updatedAccountState.recalculateAmountsWithoutExpense(expenseToUpdate);
            accountStateRepository.save(updatedAccountState);
        }

        //Update Expense and add values to AccountState
        expenseToUpdate.updateExpense(updateExpenseRequestDTO, expenseType, updatedAccountState, account);

        //Update all AccountStates that were after the oldest updated AccountState
        updateFutureAccountStates(
                previousAccountState.getDate().isBefore(updatedAccountState.getDate())
                        ? previousAccountState
                        : updatedAccountState
        );

        return expenseRepository.save(expenseToUpdate);
    }

    @Transactional
    public void deleteExpense(AppUser appUser,
                              Long id) throws
                                       DataNotFoundException,
                                       UnauthorizedUserAccessException {
        Expense expense = expenseFinder.findById(id);
        expense.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);

        //Recalculate the amounts from AccountState
        AccountState accountState = expense.getAccountState();
        accountState.recalculateAmountsWithoutExpense(expense);
        accountStateRepository.save(accountState);

        //Update all AccountStates that were after the oldest updated AccountState
        updateFutureAccountStates(accountState);

        expenseRepository.delete(expense);
    }

    /**
     * Filters the List of Expenses with dates.
     *
     * @param expenses  List of Expenses to filter.
     * @param startDate First date that will be included in the List of Expenses.
     * @param endDate   Last date that will be included in the List of Expenses.
     * @return Filtered List of Expenses.
     */
    private List<Expense> applyDateFilter(List<Expense> expenses,
                                          LocalDate startDate,
                                          LocalDate endDate) {
        return expenses.stream().filter(expense -> {
            LocalDate expenseDate = expense.getDate();
            return (!startDate.isAfter(expenseDate) && !endDate.isBefore(expenseDate));
        }).toList();
    }

    /**
     * Updates all AccountStates after specified accountState with new data.
     *
     * @param accountState latest non-updated accountState
     */
    private void updateFutureAccountStates(AccountState accountState) {
        //Get the list of AccountStates to update
        List<AccountState> accountStates = accountStateRepository.findAllByAccountAndDateGreaterThanOrderByDateAsc(
                accountState.getAccount(), accountState.getDate()
        );

        //Get starting new currentValue
        float lastCurrentValue = accountState.getCurrentAmount();
        //Update each AccountState currentValue using lastCurrentValue and movement values
        for (AccountState currentAccountState : accountStates) {
            float newValue = lastCurrentValue
                    + currentAccountState.getPositiveMovement()
                    - currentAccountState.getNegativeMovement();
            currentAccountState.setCurrentAmount(newValue);
            lastCurrentValue = newValue;
        }

        accountStateRepository.saveAll(accountStates);
    }

    private AccountState getOrCreateAccountStateForDate(LocalDate date,
                                                        Account account) {
        return accountStateRepository.findByDate(date)
                .orElseGet(() -> {
                    Optional<AccountState> previousAccountState =
                            accountStateRepository.findFirstByAccountAndDateBeforeOrderByDateDesc(account,
                                    date);
                    float previousValue = previousAccountState.isPresent()
                            ? previousAccountState.get().getCurrentAmount()
                            : 0f;
                    return new AccountState(date, previousValue, 0f, 0f, 0f, account);
                });
    }

    private void validateExpenseTypeAccount(ExpenseType expenseType,
                                            Account account) throws
                                                             ForbiddenActionException {
        if (!expenseType.getAccount().equals(account)) {
            throw new ForbiddenActionException(
                    String.format("Expense Type with ID: %d and name: %s cannot be used for Account with name: %s.",
                            expenseType.getId(), expenseType.getName(), account.getName())
            );
        }
    }
}
