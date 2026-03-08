package com.cartwave.security.service;

import com.cartwave.exception.UnauthorizedException;
import com.cartwave.security.model.CurrentUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    public CurrentUserPrincipal requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserPrincipal principal)) {
            throw new UnauthorizedException("Authenticated user context is missing.");
        }
        return principal;
    }

    public UUID requireCurrentUserId() {
        return requireCurrentUser().getUserId();
    }
}
