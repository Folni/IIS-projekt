package hr.algebra.iis.config;

import hr.algebra.iis.entity.AppUser;
import hr.algebra.iis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicijalizira testne korisnike pri pokretanju aplikacije.
 * Kreira:
 *   - admin / admin123  → FULL_ACCESS
 *   - viewer / viewer123 → READ_ONLY
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(AppUser.UserRole.FULL_ACCESS)
                    .build());

            userRepository.save(AppUser.builder()
                    .username("viewer")
                    .password(passwordEncoder.encode("viewer123"))
                    .role(AppUser.UserRole.READ_ONLY)
                    .build());

            log.info("Kreirani testni korisnici: admin (FULL_ACCESS) i viewer (READ_ONLY)");
        }
    }
}
