package com.cartwave.security.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.cartwave.user.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenProvider {

    private final String jwtSecret;
    private final long jwtExpirationDate;
    private final long refreshExpirationDate;

    public JwtTokenProvider(
            @Value("${app.jwt-secret}") String jwtSecret,
            @Value("${app.jwt-expiration-milliseconds}") long jwtExpirationDate,
            @Value("${jwt.refresh-token-expiration:604800000}") long refreshExpirationDate
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationDate = jwtExpirationDate;
        this.refreshExpirationDate = refreshExpirationDate;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user, UUID storeId) {
        return createToken(buildClaims(user, storeId, "ACCESS"), user.getEmail(), jwtExpirationDate);
    }

    public String generateRefreshToken(User user, UUID storeId) {
        return createToken(buildClaims(user, storeId, "REFRESH"), user.getEmail(), refreshExpirationDate);
    }

    private Map<String, Object> buildClaims(User user, UUID storeId, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("role", user.getRole().name());
        claims.put("tokenType", tokenType);
        if (storeId != null) {
            claims.put("storeId", storeId.toString());
            claims.put("tenantId", storeId.toString());
        }
        return claims;
    }

    private String createToken(Map<String, Object> claims, String subject, long expiryMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractStoreId(String token) {
        String storeId = extractClaim(token, claims -> claims.get("storeId", String.class));
        return storeId == null ? null : UUID.fromString(storeId);
    }

    public UUID extractTenantId(String token) {
        String tenantId = extractClaim(token, claims -> claims.get("tenantId", String.class));
        if (tenantId == null) {
            return extractStoreId(token);
        }
        return UUID.fromString(tenantId);
    }

    public UUID extractUserId(String token) {
        String userId = extractClaim(token, claims -> claims.get("userId", String.class));
        return userId == null ? null : UUID.fromString(userId);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
