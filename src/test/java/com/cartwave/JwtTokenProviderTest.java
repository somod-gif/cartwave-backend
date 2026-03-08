package com.cartwave;

import com.cartwave.security.service.JwtTokenProvider;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTokenProviderTest {

    @Test
    void generatedTokenContainsStoreContext() {
        JwtTokenProvider provider = new JwtTokenProvider(
                "dev-secret-dev-secret-dev-secret-dev-secret",
                900000L,
                604800000L
        );
        User user = User.builder()
                .email("owner@example.com")
                .password("encoded")
                .role(UserRole.BUSINESS_OWNER)
                .build();
        user.setId(UUID.randomUUID());
        UUID storeId = UUID.randomUUID();

        String token = provider.generateToken(user, storeId);

        assertEquals(storeId, provider.extractStoreId(token));
        assertEquals(user.getEmail(), provider.extractUsername(token));
        assertEquals("ACCESS", provider.extractTokenType(token));
    }
}
