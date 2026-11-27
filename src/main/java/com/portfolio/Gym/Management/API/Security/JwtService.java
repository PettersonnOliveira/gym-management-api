package com.portfolio.Gym.Management.API.Security;

import com.portfolio.Gym.Management.API.Entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
    private static final int MIN_SECRET_LENGTH = 32;

    private final String secret;
    private final long expirationMs;
    private SecretKey key;

    public JwtService(
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    @PostConstruct
    void init() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "JWT_SECRET is not configured. Set it to a Base64 key of at least "
                            + MIN_SECRET_LENGTH + " bytes.");
        }

        String trimmed = secret.trim();
        if (trimmed.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET must contain at least " + MIN_SECRET_LENGTH + " characters.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(trimmed);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT_SECRET must be a valid Base64-encoded key.", ex);
        }

        if (keyBytes.length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET must decode to at least " + MIN_SECRET_LENGTH + " bytes.");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        Claims claims = parseClaims(token);
        return email.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
