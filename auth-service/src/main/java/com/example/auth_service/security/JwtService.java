package com.example.auth_service.security;

import com.example.auth_service.config.JwtProperties;
import com.example.auth_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getAccessTokenExpiry());

        // Convert roles to list of role names
        List<String> roles = user.getRoleNames().stream().toList();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getRefreshTokenExpiry());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public UUID extractUserId(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Claims claims = parseToken(token);
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? new HashSet<>(roles) : Set.of();
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    public Long getAccessTokenExpiry() {
        return jwtProperties.getAccessTokenExpiry();
    }

    public Long getRefreshTokenExpiry() {
        return jwtProperties.getRefreshTokenExpiry();
    }

    public Instant getAccessTokenExpiryInstant() {
        return Instant.now().plusMillis(jwtProperties.getAccessTokenExpiry());
    }

    public Instant getRefreshTokenExpiryInstant() {
        return Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiry());
    }
}
