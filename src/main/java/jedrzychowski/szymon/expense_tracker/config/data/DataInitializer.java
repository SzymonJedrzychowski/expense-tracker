package jedrzychowski.szymon.expense_tracker.config.data;
import jedrzychowski.szymon.expense_tracker.entity.Role;
import jedrzychowski.szymon.expense_tracker.entity.AppUser;
import jedrzychowski.szymon.expense_tracker.repository.RoleRepository;
import jedrzychowski.szymon.expense_tracker.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(AppUserRepository appUserRepository,
                                       RoleRepository roleRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            if (roleRepository.count() == 0) {
                Role userRole = new Role("ROLE_USER");
                Role adminRole = new Role("ROLE_ADMIN");
                roleRepository.saveAll(List.of(userRole, adminRole));
            }

            if (appUserRepository.count() == 0) {
                Role userRole = roleRepository.findByName("ROLE_USER");
                AppUser appUser = new AppUser(
                        "user",
                        passwordEncoder.encode("password"),
                        List.of(userRole)
                );
                appUserRepository.save(appUser);

                Role adminRole = roleRepository.findByName("ROLE_ADMIN");
                AppUser admin = new AppUser(
                        "admin",
                        passwordEncoder.encode("adminpassword"),
                        List.of(adminRole)
                );
                appUserRepository.save(admin);
            }
        };
    }
}