package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
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

    @PostMapping("/login")
    public String login(@RequestBody @Valid AuthRequestDTO authRequestDTO) {
        return authService.login(authRequestDTO);
    }

    @PostMapping("/register")
    public void register(@RequestBody AuthRequestDTO authRequestDTO) {
        authService.register(authRequestDTO);
    }
}
