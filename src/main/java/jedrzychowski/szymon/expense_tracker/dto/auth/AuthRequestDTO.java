package jedrzychowski.szymon.expense_tracker.dto.auth;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class AuthRequestDTO {

    @NotBlank(message = "Username cannot be null.")
    private String username;

    @NotBlank(message = "Password cannot be null.")
    private String password;

    static final String USERNAME_PATTERN = "^[a-zA-Z0-9._]{6,20}$";
    static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-\\[\\]{}|;':\",.<>/?]).{8,32}$";

    public AuthRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> validateEntity() {
        List<String> validationErrors = new ArrayList<>();

        if (!username.matches(USERNAME_PATTERN)) {
            validationErrors.add("Username must be an 8 to 32 characters long, including only letters and numbers.");
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            validationErrors.add("Password must be an 8 to 32 characters long, including letters, numbers" +
                    "and special characters. At least on number and one special character must be used.");
        }

        return validationErrors;
    }
}
