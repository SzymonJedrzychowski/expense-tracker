package jedrzychowski.szymon.expense_tracker.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class HealthCheckService {
    public String getHealthCheck() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        return String.format("Expense-Tracker-Api is functional: %s.", formattedDateTime);
    }
}
