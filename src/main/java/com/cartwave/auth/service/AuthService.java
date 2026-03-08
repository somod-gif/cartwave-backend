package com.cartwave.auth.service;

import com.cartwave.auth.dto.JwtAuthResponse;
import com.cartwave.auth.dto.LoginRequest;
import com.cartwave.auth.dto.RefreshTokenRequest;
import com.cartwave.auth.dto.RegisterRequest;
import com.cartwave.auth.dto.UserDTO;
import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.repository.CustomerRepository;
import com.cartwave.exception.BusinessException;
import com.cartwave.exception.UnauthorizedException;
import com.cartwave.security.service.CustomUserDetailsService;
import com.cartwave.security.service.JwtTokenProvider;
import com.cartwave.store.entity.Store;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.entity.UserStatus;
import com.cartwave.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final CustomUserDetailsService customUserDetailsService;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       LoginAttemptService loginAttemptService,
                       CustomUserDetailsService customUserDetailsService,
                       StoreRepository storeRepository,
                       CustomerRepository customerRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.customUserDetailsService = customUserDetailsService;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
    }

    public JwtAuthResponse login(LoginRequest loginRequest) {
        if (loginAttemptService.isLocked(loginRequest.getEmail())) {
            throw new UnauthorizedException("Too many login attempts. Account temporarily locked.");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));
            UUID storeId = resolveStoreContext(user, loginRequest.getStoreId(), true);
            ensureCustomerProfileIfNeeded(user, storeId);
            user.setLastLoginAt(Instant.now().toEpochMilli());
            userRepository.save(user);
            loginAttemptService.onSuccess(loginRequest.getEmail());
            return JwtAuthResponse.builder()
                    .accessToken(tokenProvider.generateToken(user, storeId))
                    .refreshToken(tokenProvider.generateRefreshToken(user, storeId))
                    .build();
        } catch (Exception ex) {
            loginAttemptService.onFailure(loginRequest.getEmail());
            if (ex instanceof UnauthorizedException || ex instanceof BusinessException) {
                throw ex;
            }
            throw new UnauthorizedException("Invalid email or password.");
        }
    }

    public JwtAuthResponse refreshToken(RefreshTokenRequest request) {
        if (!"REFRESH".equals(tokenProvider.extractTokenType(request.getRefreshToken()))) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String username = tokenProvider.extractUsername(request.getRefreshToken());
        UUID storeId = tokenProvider.extractStoreId(request.getRefreshToken());
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        var userDetails = customUserDetailsService.loadUserByUsernameAndStoreId(username, storeId);
        if (!tokenProvider.validateToken(request.getRefreshToken(), userDetails)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        resolveStoreContext(user, storeId, false);
        ensureCustomerProfileIfNeeded(user, storeId);
        return JwtAuthResponse.builder()
                .accessToken(tokenProvider.generateToken(user, storeId))
                .refreshToken(tokenProvider.generateRefreshToken(user, storeId))
                .build();
    }

    public UserDTO register(RegisterRequest registerRequest) {
        UserRole role = parsePublicRole(registerRequest.getRole());

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email address already in use.");
        }

        if (role == UserRole.CUSTOMER && registerRequest.getStoreId() == null) {
            throw new BusinessException("STORE_REQUIRED", "Customer registration requires a storeId.");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setDeleted(false);
        User saved = userRepository.save(user);

        if (role == UserRole.CUSTOMER) {
            Store store = getStore(registerRequest.getStoreId());
            createCustomerProfileIfMissing(saved, store.getId(), registerRequest.getPhoneNumber());
        }

        return UserDTO.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .phoneNumber(saved.getPhoneNumber())
                .role(saved.getRole().name())
                .status(saved.getStatus().name())
                .emailVerified(saved.getEmailVerified())
                .build();
    }

    private UserRole parsePublicRole(String rawRole) {
        UserRole role;
        try {
            role = UserRole.valueOf(rawRole);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_ROLE", "Unsupported public registration role.");
        }

        if (role != UserRole.CUSTOMER && role != UserRole.BUSINESS_OWNER) {
            throw new BusinessException("INVALID_ROLE", "Public registration is limited to CUSTOMER and BUSINESS_OWNER.");
        }
        return role;
    }

    private UUID resolveStoreContext(User user, UUID requestedStoreId, boolean requireForCustomer) {
        List<UUID> accessibleStores = customUserDetailsService.getAccessibleStoreIds(user);

        if (requestedStoreId != null) {
            if (!accessibleStores.contains(requestedStoreId)) {
                throw new UnauthorizedException("You do not have access to the requested store.");
            }
            return requestedStoreId;
        }

        if (accessibleStores.size() == 1) {
            return accessibleStores.getFirst();
        }

        if (accessibleStores.size() > 1) {
            throw new UnauthorizedException("storeId is required because this account can access multiple stores.");
        }

        if (requireForCustomer && user.getRole() == UserRole.CUSTOMER) {
            throw new UnauthorizedException("Customer login requires a valid storeId.");
        }

        return null;
    }

    private void ensureCustomerProfileIfNeeded(User user, UUID storeId) {
        if (user.getRole() != UserRole.CUSTOMER) {
            return;
        }
        if (storeId == null) {
            throw new UnauthorizedException("Customer login requires a valid storeId.");
        }
        createCustomerProfileIfMissing(user, storeId, user.getPhoneNumber());
    }

    private void createCustomerProfileIfMissing(User user, UUID storeId, String phoneNumber) {
        customerRepository.findByUserIdAndStoreId(user.getId(), storeId).orElseGet(() -> {
            Customer customer = Customer.builder()
                    .userId(user.getId())
                    .storeId(storeId)
                    .phone(phoneNumber)
                    .addressesJson("[]")
                    .wishlistJson("[]")
                    .build();
            return customerRepository.save(customer);
        });
    }

    private Store getStore(UUID storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException("STORE_NOT_FOUND", "Store not found for customer registration."));
    }
}
