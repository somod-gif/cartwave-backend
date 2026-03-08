package com.cartwave;

import com.cartwave.auth.dto.RegisterRequest;
import com.cartwave.auth.service.AuthService;
import com.cartwave.auth.service.LoginAttemptService;
import com.cartwave.customer.repository.CustomerRepository;
import com.cartwave.exception.BusinessException;
import com.cartwave.security.service.CustomUserDetailsService;
import com.cartwave.security.service.JwtTokenProvider;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    LoginAttemptService loginAttemptService;
    @Mock
    CustomUserDetailsService customUserDetailsService;
    @Mock
    StoreRepository storeRepository;
    @Mock
    CustomerRepository customerRepository;

    @InjectMocks
    AuthService authService;

    @Test
    void registerRejectsAdminRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@example.com");
        request.setPassword("Password123!");
        request.setRole("ADMIN");

        assertThrows(BusinessException.class, () -> authService.register(request));
    }

    @Test
    void customerRegistrationRequiresStoreId() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("customer@example.com");
        request.setPassword("Password123!");
        request.setRole("CUSTOMER");

        assertThrows(BusinessException.class, () -> authService.register(request));
    }
}
