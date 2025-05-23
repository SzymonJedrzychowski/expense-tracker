package jedrzychowski.szymon.expense_tracker.controller;

import jedrzychowski.szymon.expense_tracker.config.auth.JwtUtil;
import jedrzychowski.szymon.expense_tracker.dto.auth.AuthRequestDTO;
import jedrzychowski.szymon.expense_tracker.exception.ReasonedResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("expense-tracker/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody AuthRequestDTO authRequestDTO) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
            );
            return jwtUtil.generateToken(authRequestDTO.getUsername());
        } catch (AuthenticationException e) {
            throw new ReasonedResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/password");
        }
    }
}
