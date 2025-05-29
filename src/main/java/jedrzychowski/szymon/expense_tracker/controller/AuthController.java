package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.exception.DataConflictException;
import jedrzychowski.szymon.expense_tracker.config.exception.DataValidationException;
import jedrzychowski.szymon.expense_tracker.config.exception.UnauthorizedUserAccessException;
import jedrzychowski.szymon.expense_tracker.entity.dto.auth.AuthRequestDTO;
import jedrzychowski.szymon.expense_tracker.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("expense-tracker/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user with provided credentials.
     *
     * @param authRequestDTO the authentication request data transfer object containing username and password
     * @return a JWT token if authentication is successful
     * @throws UnauthorizedUserAccessException if incorrect credentials are provided
     */
    @PostMapping("/login")
    public String login(@RequestBody @Valid AuthRequestDTO authRequestDTO) {
        return authService.login(authRequestDTO);
    }

    /**
     * Registers a new user with the provided credentials.
     *
     * @param authRequestDTO the registration request data containing username and password
     * @throws DataConflictException   if provided username is already taken
     * @throws DataValidationException if provided credentials fail validation
     */
    @PostMapping("/register")
    public void register(@RequestBody AuthRequestDTO authRequestDTO) {
        authService.register(authRequestDTO);
    }
}
