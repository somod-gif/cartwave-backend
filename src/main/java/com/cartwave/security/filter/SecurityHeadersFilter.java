package com.cartwave.security.filter;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Adds recommended OWASP security response headers to every API response.
 * Swagger UI paths are excluded from the restrictive CSP so the UI can load.
 */
@Component
@Order(3)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isSwaggerPath = path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs");

        // Prevent MIME-type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Legacy XSS protection header (Chrome/IE)
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Enforce HTTPS for 1 year, include sub-domains
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

        // Restrict referrer information to same origin
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        if (isSwaggerPath) {
            // Swagger UI needs scripts, styles, images, and fonts to render
            response.setHeader("X-Frame-Options", "SAMEORIGIN");
            response.setHeader("Content-Security-Policy",
                    "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:;");
        } else {
            // Strict API-only policy
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        }

        // Disable browser's feature/permission APIs not needed
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        // CartWave platform watermark
        response.setHeader("X-Powered-By", "CartWave");

        filterChain.doFilter(request, response);
    }
}
