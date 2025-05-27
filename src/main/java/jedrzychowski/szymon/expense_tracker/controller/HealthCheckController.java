package jedrzychowski.szymon.expense_tracker.controller;

import jedrzychowski.szymon.expense_tracker.service.HealthCheckService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("expense-tracker/v1/health-check")
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    public String getHealthCheck() {
        return healthCheckService.getHealthCheck();
    }
}
