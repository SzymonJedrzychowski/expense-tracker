package jedrzychowski.szymon.expense_tracker.service;

import jakarta.validation.Valid;
import jedrzychowski.szymon.expense_tracker.config.auth.JwtUtil;
import jedrzychowski.szymon.expense_tracker.config.exception.DataConflictException;
import jedrzychowski.szymon.expense_tracker.config.exception.DataValidationException;
import jedrzychowski.szymon.expense_tracker.config.exception.UnauthorizedUserAccessException;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.entity.dto.auth.AuthRequestDTO;
import jedrzychowski.szymon.expense_tracker.repository.AppUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;

    public AuthService(AuthenticationManager authManager,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder,
                       AppUserRepository appUserRepository) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.appUserRepository = appUserRepository;
    }


    public String login(@Valid AuthRequestDTO authRequestDTO) throws
                                                              UnauthorizedUserAccessException {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDTO.username(), authRequestDTO.password())
            );
            return jwtUtil.generateToken(authRequestDTO.username());
        } catch (AuthenticationException e) {
            throw new UnauthorizedUserAccessException("Invalid username/password.");
        }
    }


    public void register(AuthRequestDTO authRequestDTO) throws
                                                        DataConflictException,
                                                        DataValidationException {
        if (appUserRepository.findByUsername(authRequestDTO.username()).isPresent()) {
            throw new DataConflictException("Username already taken.");
        }

        List<String> validationResults = authRequestDTO.validateDTO();
        if (!validationResults.isEmpty()) {
            throw new DataValidationException(validationResults);
        }

        AppUser appUser = new AppUser(
                authRequestDTO.username(),
                passwordEncoder.encode(authRequestDTO.password())
        );

        appUserRepository.save(appUser);
    }
}
