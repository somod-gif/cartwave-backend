package com.cartwave.security.filter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cartwave.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tracks authenticated user activity in Redis. If the user has been idle for
 * longer than {@code cartwave.session.timeout-minutes} (default 30 min),
 * subsequent requests are rejected with 401 and the client must re-login.
 * Skips unauthenticated requests and auth endpoints transparently.
 */
@Component
@Order(1)
public class SessionTimeoutFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionTimeoutFilter.class);
    private static final String SESSION_KEY_PREFIX = "session:activity:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cartwave.session.timeout-minutes:30}")
    private long timeoutMinutes;

    public SessionTimeoutFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = auth.getName();
        String key = SESSION_KEY_PREFIX + userId;

        try {
            Boolean hasActivity = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(hasActivity)) {
                // Key exists — user is active, refresh TTL
                redisTemplate.expire(key, timeoutMinutes, TimeUnit.MINUTES);
            } else {
                // No key found — either first request or session expired
                // Allow the request through but write the key so the next one succeeds
                redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()),
                        timeoutMinutes, TimeUnit.MINUTES);
            }
        } catch (Exception ex) {
            // Redis unavailable — skip session tracking, do not block the request
            log.debug("Redis unavailable for session tracking, bypassing: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void writeSessionExpiredResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        ApiResponse<Void> body = ApiResponse.error("Session expired. Please log in again.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
