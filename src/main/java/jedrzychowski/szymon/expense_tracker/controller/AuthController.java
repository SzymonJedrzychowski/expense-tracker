package jedrzychowski.szymon.expense_tracker.controller;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.auth.JwtUtil;
import jedrzychowski.szymon.expense_tracker.dto.auth.AuthRequestDTO;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.entity.Role;
import jedrzychowski.szymon.expense_tracker.exception.ReasonedResponseStatusException;
import jedrzychowski.szymon.expense_tracker.repository.AppUserRepository;
import jedrzychowski.szymon.expense_tracker.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("expense-tracker/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public String login(@RequestBody @Valid AuthRequestDTO authRequestDTO) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
            );
            return jwtUtil.generateToken(authRequestDTO.getUsername());
        } catch (AuthenticationException e) {
            throw new ReasonedResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/password.");
        }
    }

    @PostMapping("/register")
    public void register(@RequestBody AuthRequestDTO authRequestDTO) {
        if (appUserRepository.findByUsername(authRequestDTO.getUsername()).isPresent()) {
            throw new ReasonedResponseStatusException(HttpStatus.CONFLICT, "Username already taken.");
        }

        List<String> validationResults = authRequestDTO.validateEntity();
        if(!validationResults.isEmpty()){
            throw new ReasonedResponseStatusException(HttpStatus.BAD_REQUEST, validationResults);
        }

        Role userRole = roleRepository.findByName("ROLE_USER");
        AppUser appUser = new AppUser(
                authRequestDTO.getUsername(),
                passwordEncoder.encode(authRequestDTO.getPassword()),
                List.of(userRole)
        );

        appUserRepository.save(appUser);
    }
}
