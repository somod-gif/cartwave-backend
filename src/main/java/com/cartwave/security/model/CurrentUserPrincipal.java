package com.cartwave.security.model;

import com.cartwave.user.entity.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Getter
public class CurrentUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String password;
    private final UserRole role;
    private final UUID storeId;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CurrentUserPrincipal(
            UUID userId,
            String email,
            String password,
            UserRole role,
            UUID storeId,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.storeId = storeId;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
