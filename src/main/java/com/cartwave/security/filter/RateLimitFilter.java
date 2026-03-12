package com.cartwave.security.filter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cartwave.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Request-rate limiter using Bucket4j with per-IP in-memory buckets.
 * Rules:
 *   - /api/v1/auth/login         → 10 requests / minute  (brute-force protection)
 *   - /api/v1/auth/forgot-password → 5 requests / 10 minutes
 *   - All other /api/** endpoints → 200 requests / minute per IP
 */
@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LOGIN_CAPACITY = 10;
    private static final int FORGOT_CAPACITY = 5;
    private static final int DEFAULT_CAPACITY = 200;

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> forgotBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> defaultBuckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = resolveClientIp(request);
        String path = request.getServletPath();

        Bucket bucket;
        if (path.startsWith("/api/v1/auth/login")) {
            bucket = loginBuckets.computeIfAbsent(ip, k -> buildBucket(LOGIN_CAPACITY, Duration.ofMinutes(1)));
        } else if (path.startsWith("/api/v1/auth/forgot-password")) {
            bucket = forgotBuckets.computeIfAbsent(ip, k -> buildBucket(FORGOT_CAPACITY, Duration.ofMinutes(10)));
        } else {
            bucket = defaultBuckets.computeIfAbsent(ip, k -> buildBucket(DEFAULT_CAPACITY, Duration.ofMinutes(1)));
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            ApiResponse<Void> body = ApiResponse.error("Too many requests. Please slow down.");
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }

    private static Bucket buildBucket(int capacity, Duration period) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, period));
        return Bucket.builder().addLimit(limit).build();
    }

    /** Prefer X-Forwarded-For (set by reverse proxies/load-balancers) over remote address. */
    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
