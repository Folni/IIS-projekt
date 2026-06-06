package hr.algebra.iis.controller;

import hr.algebra.iis.entity.AppUser;
import hr.algebra.iis.repository.UserRepository;
import hr.algebra.iis.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * KORAK 5: Autentifikacijski kontroler - login i refresh tokena.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * POST /api/auth/login
     * Vraća access i refresh JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        log.info("Login pokušaj za korisnika: {}", username);

        AppUser user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Neispravan username ili lozinka"));
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Uspješna prijava korisnika: {} ({})", username, user.getRole());

        return ResponseEntity.ok(Map.of(
            "accessToken",  accessToken,
            "refreshToken", refreshToken,
            "tokenType",    "Bearer",
            "expiresIn",    86400,
            "user", Map.of(
                "id",       user.getId(),
                "username", user.getUsername(),
                "role",     user.getRole().name()
            )
        ));
    }

    /**
     * POST /api/auth/refresh
     * Prima refresh token, vraća novi access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (refreshToken == null || !jwtService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nevažeći ili istekli refresh token"));
        }

        String username = jwtService.extractUsername(refreshToken);
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        String newAccessToken = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(Map.of(
            "accessToken", newAccessToken,
            "tokenType",   "Bearer",
            "expiresIn",   86400
        ));
    }
}
