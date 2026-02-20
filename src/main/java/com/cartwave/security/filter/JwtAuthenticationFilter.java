package com.cartwave.security.filter;

import com.cartwave.security.service.CustomUserDetailsService;
import com.cartwave.security.service.JwtTokenProvider;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final StoreRepository storeRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService, StoreRepository storeRepository) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.storeRepository = storeRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = getJwtFromRequest(request);
        UUID previousTenant = null;

        try {
            if (jwt != null) {
                String username = tokenProvider.extractUsername(jwt);
                UUID storeId = tokenProvider.extractStoreId(jwt);

                if (storeId == null) {
                    // fallback: if only one store exists, use it for local/testing convenience
                    Optional<com.cartwave.store.entity.Store> single = storeRepository.findAll().stream().findFirst();
                    if (single.isPresent()) {
                        storeId = single.get().getId();
                    }
                }

                if (storeId != null) {
                    // preserve previous tenant if any (defensive)
                    if (TenantContext.isSet()) {
                        previousTenant = TenantContext.getTenantId();
                    }
                    TenantContext.setTenantId(storeId);
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    if (tokenProvider.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // Clear tenant context to avoid leaking between requests/threads
            try {
                TenantContext.clear();
                if (previousTenant != null) {
                    TenantContext.setTenantId(previousTenant);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
