package com.cartwave.security.service;

import com.cartwave.exception.UnauthorizedException;
import com.cartwave.security.dto.JwtClaims;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${jwt.secret:ChangeThisToASecureRandomSecretKeyOfSufficientLength_ReplaceInProduction}") String secret,
            @Value("${jwt.access-token-expiration:900000}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpiration) {

        byte[] decodedKey = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(JwtClaims claims) {
        return buildToken(claims, accessTokenExpiration, "ACCESS");
    }

    public String generateRefreshToken(JwtClaims claims) {
        return buildToken(claims, refreshTokenExpiration, "REFRESH");
    }

    private String buildToken(JwtClaims claims, long expirationMs, String tokenType) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(expirationMs, ChronoUnit.MILLIS);

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("userId", claims.getUserId().toString());
        claimsMap.put("email", claims.getEmail());
        claimsMap.put("role", claims.getRole());
        claimsMap.put("storeId", claims.getStoreId().toString());
        claimsMap.put("tokenType", tokenType);
        if (claims.getPermissions() != null) {
            claimsMap.put("permissions", claims.getPermissions());
        }

        return Jwts.builder()
                .claims(claimsMap)
                .subject(claims.getUserId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(secretKey)
                .compact();
    }

    public JwtClaims extractClaims(String token) {
        try {
            Claims claims = parseToken(token);

            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) claims.get("permissions");
            return JwtClaims.builder()
                    .userId(UUID.fromString((String) claims.get("userId")))
                    .email(claims.get("email", String.class))
                    .role(claims.get("role", String.class))
                    .storeId(UUID.fromString((String) claims.get("storeId")))
                    .tokenType(claims.get("tokenType", String.class))
                    .permissions(permissions)
                    .build();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting claims from token", e);
            throw new UnauthorizedException("Invalid token");
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseToken(String token) {
        @SuppressWarnings("deprecation")
        JwtParser parser = Jwts.parser()
                .setSigningKey(secretKey)
                .build();
        return parser.parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public String getTokenTypeFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("tokenType", String.class);
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid token");
        }
    }

}
