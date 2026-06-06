package hr.algebra.iis.service;

import hr.algebra.iis.entity.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * KORAK 5: JWT servis za generiranje i validaciju access i refresh tokena.
 */
@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    /**
     * Generira access token (kratko trajanje - 15 min)
     */
    public String generateAccessToken(AppUser user) {
        return buildToken(user, accessTokenExpiration, "access");
    }

    /**
     * Generira refresh token (dugo trajanje - 7 dana)
     */
    public String generateRefreshToken(AppUser user) {
        return buildToken(user, refreshTokenExpiration, "refresh");
    }

    /**
     * Provjerava je li token validan
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Nevažeći JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Izvlači username iz tokena
     */
    public String extractUsername(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    /**
     * Izvlači ulogu korisnika iz tokena
     */
    public String extractRole(String token) {
        return parseToken(token).getPayload().get("role", String.class);
    }

    /**
     * Provjeri je li token access tip (ne refresh)
     */
    public boolean isAccessToken(String token) {
        return "access".equals(parseToken(token).getPayload().get("type", String.class));
    }

    // =================== Privatne metode ===================

    private String buildToken(AppUser user, long expiration, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("type", tokenType);
        claims.put("userId", user.getId());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
