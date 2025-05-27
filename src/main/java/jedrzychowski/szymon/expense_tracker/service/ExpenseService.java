package jedrzychowski.szymon.expense_tracker.service;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.entity.*;
import jedrzychowski.szymon.expense_tracker.entity.dto.expense.CreateExpenseRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.dto.expense.UpdateExpenseRequestDTO;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.AccountStateRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseRepository;
import jedrzychowski.szymon.expense_tracker.repository.ExpenseTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ExpenseService {

    private final AccountRepository accountRepository;
    private final AccountStateRepository accountStateRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseTypeRepository expenseTypeRepository;

    public ExpenseService(AccountRepository accountRepository,
                          AccountStateRepository accountStateRepository,
                          ExpenseRepository expenseRepository,
                          ExpenseTypeRepository expenseTypeRepository) {
        this.accountRepository = accountRepository;
        this.accountStateRepository = accountStateRepository;
        this.expenseRepository = expenseRepository;
        this.expenseTypeRepository = expenseTypeRepository;
    }


    public List<Expense> getAllExpenses(AppUser appUser,
                                        Long accountId,
                                        LocalDate startDate,
                                        LocalDate endDate) {
        //Update startDate and endDate in case of null values
        startDate = startDate == null ? LocalDate.of(0, 1, 1) : startDate;
        endDate = endDate == null ? LocalDate.of(9999, 12, 31) : endDate;

        if (startDate.isAfter(endDate)) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("startDate (%s) cannot be after endDate (%s)", startDate, endDate)
            );
        }

        //Return all without filtering
        if (accountId == null) {
            return applyDateFilter(
                    expenseRepository.findAllByAccount_AppUserOrderByAccountState_DateAsc(appUser), startDate, endDate
            );
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with ID: %d", accountId)
                ));
        account.validateIfAccountIsOwnedByCurrentUser(appUser);

        return applyDateFilter(
                expenseRepository.findAllByAccountIdOrderByAccountState_DateAsc(accountId), startDate, endDate
        );
    }

    public Expense getExpenseById(AppUser appUser,
                                  Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Expense with ID: %d", id)
                ));
        expense.getAccount().validateIfAccountIsOwnedByCurrentUser(appUser);
        return expense;
    }


    public Expense createExpense(AppUser appUser,
                                 @Valid CreateExpenseRequestDTO createExpenseRequestDTO) {
        List<String> validationResults = createExpenseRequestDTO.validateDTO();
        if (!validationResults.isEmpty()) {
            throw new ReasonedResponseStatusException(HttpStatus.BAD_REQUEST, validationResults);
        }

        //Find if Account with given ID exists
        Account account = accountRepository.findById(createExpenseRequestDTO.accountId())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account with name: %s.", createExpenseRequestDTO.accountId())
                ));
        account.validateIfAccountIsOwnedByCurrentUser(appUser);

        //Find if Expense Type with given ID exists
        ExpenseType expenseType = expenseTypeRepository.findById(createExpenseRequestDTO.expenseTypeId())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Expense Type with ID: %d", createExpenseRequestDTO.expenseTypeId())
                ));

        //Find if Expense Type can be used for the Account
        if (expenseType.getAccount() != account) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    String.format("Expense Type with ID: %d and name: %s cannot be used for Account with name: %s " +
                                    "and can be used only for Account with name: %s",
                            expenseType.getId(), expenseType.getName(), account.getName(),
                            expenseType.getAccount().getName())
            );
        }

        LocalDate date = createExpenseRequestDTO.date();
        float negativeMovement = -Math.min(createExpenseRequestDTO.movementAmount(), 0);
        float positiveMovement = Math.max(createExpenseRequestDTO.movementAmount(), 0);

        //Find or create new AccountState based on the date
        AccountState accountState = accountStateRepository.findByDate(date)
                .orElseGet(() -> {
                    Optional<AccountState> previousAccountState =
                            accountStateRepository.findFirstByAccountAndDateBeforeOrderByDateDesc(account, date);
                    float previousValue = previousAccountState.isPresent()
                            ? previousAccountState.get().getCurrentAmount()
                            : 0f;
                    return new AccountState(date, previousValue, 0f, 0f, 0f, account);
                });

        //Update the values
        accountState.setCurrentAmount(accountState.getCurrentAmount() + createExpenseRequestDTO.movementAmount());
        accountState.setNegativeMovement(accountState.getNegativeMovement() + negativeMovement);
        accountState.setPositiveMovement(accountState.getPositiveMovement() + positiveMovement);
        accountState.setRefundAmount(accountState.getRefundAmount() + createExpenseRequestDTO.refundAmount());

        //Create the expense
        Expense expenseToAdd = new Expense(createExpenseRequestDTO, expenseType, accountState, account);
        accountStateRepository.save(accountState);

        updateFutureAccountStates(accountState);
        return expenseRepository.save(expenseToAdd);
    }

    public Expense updateExpense(AppUser appUser,
                                 @Valid UpdateExpenseRequestDTO updateExpenseRequestDTO) {
        List<String> validationResults = updateExpenseRequestDTO.validateDTO();
        if (!validationResults.isEmpty()) {
            throw new ReasonedResponseStatusException(HttpStatus.BAD_REQUEST, validationResults);
        }

        //Find the Expense to update
        Expense expenseToUpdate = expenseRepository.findById(updateExpenseRequestDTO.id())
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Expense with ID %d.", updateExpenseRequestDTO.id())
                ));

        //Find if Account gets updated on Expense
        Account account = expenseToUpdate.getAccount();
        account.validateIfAccountIsOwnedByCurrentUser(appUser);
        if (!Objects.equals(updateExpenseRequestDTO.accountId(), account.getId())) {
            account = accountRepository.findById(updateExpenseRequestDTO.accountId())
                    .orElseThrow(() -> new ReasonedResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            String.format("Cannot find Account with ID: %d.", updateExpenseRequestDTO.accountId())
                    ));
            account.validateIfAccountIsOwnedByCurrentUser(appUser);
        }

        //Find if ExpenseType gets updated on Expense
        ExpenseType expenseType = expenseToUpdate.getExpenseType();
        if (Objects.equals(updateExpenseRequestDTO.expenseTypeId(), expenseType.getId())) {
            expenseType = expenseTypeRepository.findById(updateExpenseRequestDTO.expenseTypeId())
                    .orElseThrow(() -> new ReasonedResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            String.format("Cannot find Expense Type with ID: %d",
                                    updateExpenseRequestDTO.expenseTypeId())
                    ));
        }

        //Find if Expense Type can be used for the Account
        if (expenseType.getAccount() != account) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    String.format("Expense Type with ID: %d and name: %s cannot be used for Account with name: %s " +
                                    "and can be used only for Account with name: %s",
                            expenseType.getId(), expenseType.getName(), account.getName(),
                            expenseType.getAccount().getName())
            );
        }

        //Find if Date gets updated on Expense (AccountState)
        LocalDate date = updateExpenseRequestDTO.date();
        final Account effectiveAccount = account;
        AccountState updatedAccountState = expenseToUpdate.getAccountState();
        if (date != expenseToUpdate.getDate()) {
            updatedAccountState = accountStateRepository.findByDate(date)
                    .orElseGet(() -> {
                        Optional<AccountState> previousAccountState =
                                accountStateRepository.findFirstByAccountAndDateBeforeOrderByDateDesc(effectiveAccount,
                                        date);
                        float previousValue = previousAccountState.isPresent()
                                ? previousAccountState.get().getCurrentAmount()
                                : 0f;
                        return new AccountState(date, previousValue, 0f, 0f, 0f, effectiveAccount);
                    });
        }

        //Recalculate the amounts from AccountState
        AccountState previousAccountState;
        previousAccountState = expenseToUpdate.getAccountState();

        if (previousAccountState != updatedAccountState) {
            previousAccountState.recalculateAmountsWithoutExpense(expenseToUpdate);
            accountStateRepository.save(previousAccountState);
        } else {
            updatedAccountState.recalculateAmountsWithoutExpense(expenseToUpdate);
            accountStateRepository.save(updatedAccountState);
        }

        //Update the expense
        expenseToUpdate.updateExpense(updateExpenseRequestDTO, expenseType, updatedAccountState, account);

        //Update all AccountStates that were after the oldest updated AccountState
        updateFutureAccountStates(
                previousAccountState.getDate().isBefore(updatedAccountState.getDate())
                        ? previousAccountState
                        : updatedAccountState
        );

        return expenseRepository.save(expenseToUpdate);
    }

    public void deleteExpense(AppUser appUser,
                              Long id) {
        //Find Expense to delete
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Expense with ID: %d.", id)
                ));
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
}
