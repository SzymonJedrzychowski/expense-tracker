package jedrzychowski.szymon.expense_tracker.controller;

import jedrzychowski.szymon.expense_tracker.entity.AccountState;
import jedrzychowski.szymon.expense_tracker.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.repository.AccountRepository;
import jedrzychowski.szymon.expense_tracker.repository.AccountStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("expense-tracker/v1/account-state")
public class AccountStateController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountStateRepository accountStateRepository;

    /**
     * Get AccountStates. These can be filtered by: Account ID.
     *
     * @param accountId ID of Account used to filter.
     * @return List of AccountStates.
     * @throws ReasonedResponseStatusException when no Account with accountId ID is found.
     */
    @GetMapping
    public List<AccountState> getAll(@RequestParam(required = false) Long accountId) {
        //Return all without filtering
        if (accountId == null) {
            return accountStateRepository.findAll(Sort.by(Sort.Direction.ASC, "date"));
        }

        //Check if Account with specific ID exists.
        if (!accountRepository.existsById(accountId)) {
            throw new ReasonedResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Cannot find Account with ID: %d", accountId)
            );
        }
        return accountStateRepository.findAllByAccountIdOrderByDateAsc(accountId);
    }

    /**
     * Gets the AccountState by specified ID.
     *
     * @param id of specific AccountState.
     * @return AccountState with specified ID.
     * @throws ReasonedResponseStatusException if no AccountState with specific ID is found.
     */
    @GetMapping("/{id}")
    public AccountState getAccountStateById(@PathVariable Long id) {
        return accountStateRepository.findById(id)
                .orElseThrow(() -> new ReasonedResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Cannot find Account State with ID: %d", id)
                ));
    }
}
