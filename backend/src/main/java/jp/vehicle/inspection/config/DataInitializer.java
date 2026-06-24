package jp.vehicle.inspection.config;

import jp.vehicle.inspection.domain.entity.Role;
import jp.vehicle.inspection.domain.entity.User;
import jp.vehicle.inspection.domain.repository.RoleRepository;
import jp.vehicle.inspection.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@vehicle-inspection.local").isPresent()) {
            return;
        }
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing — run Flyway migrations"));
        User admin = User.builder()
                .email("admin@vehicle-inspection.local")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("システム管理者")
                .active(true)
                .mfaEnabled(false)
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(admin);
        log.info("Default admin created: admin@vehicle-inspection.local / admin123");
    }
}
