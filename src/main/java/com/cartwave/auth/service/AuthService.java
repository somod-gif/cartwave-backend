package com.cartwave.auth.service;

import com.cartwave.auth.dto.ForgotPasswordRequest;
import com.cartwave.auth.dto.JwtAuthResponse;
import com.cartwave.auth.dto.LoginRequest;
import com.cartwave.auth.dto.RefreshTokenRequest;
import com.cartwave.auth.dto.RegisterRequest;
import com.cartwave.auth.dto.ResetPasswordRequest;
import com.cartwave.auth.dto.UserDTO;
import com.cartwave.auth.dto.VerifyEmailRequest;
import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.repository.CustomerRepository;
import com.cartwave.email.service.EmailQueueService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
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
    private final EmailQueueService emailQueueService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private static final long PASSWORD_RESET_TTL_MS = 15 * 60 * 1000L; // 15 minutes
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       LoginAttemptService loginAttemptService,
                       CustomUserDetailsService customUserDetailsService,
                       StoreRepository storeRepository,
                       CustomerRepository customerRepository,
                       EmailQueueService emailQueueService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.customUserDetailsService = customUserDetailsService;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.emailQueueService = emailQueueService;
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

    @Transactional
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

        // Generate email verification token for new accounts (except admin-created ones)
        String verificationToken = generateSecureToken();
        user.setEmailVerificationToken(verificationToken);
        User saved = userRepository.save(user);

        if (role == UserRole.CUSTOMER) {
            Store store = getStore(registerRequest.getStoreId());
            createCustomerProfileIfMissing(saved, store.getId(), registerRequest.getPhoneNumber());
        }

        // Send verification email asynchronously (best-effort)
        String verifyLink = frontendUrl + "/verify-email?token=" + verificationToken;
        try {
            emailQueueService.enqueueEmailVerification(saved.getEmail(), saved.getFirstName(), verifyLink);
        } catch (Exception ignored) {}

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

    // ── Forgot Password ───────────────────────────────────────────────────────

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return 200 to prevent email enumeration
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = generateSecureToken();
            user.setPasswordResetToken(token);
            user.setPasswordResetExpiresAt(Instant.now().toEpochMilli() + PASSWORD_RESET_TTL_MS);
            userRepository.save(user);

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            try {
                emailQueueService.enqueuePasswordReset(user.getEmail(), resetLink);
            } catch (Exception ignored) {}
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Invalid or expired password reset token."));

        long now = Instant.now().toEpochMilli();
        if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt() < now) {
            throw new BusinessException("TOKEN_EXPIRED", "Password reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
    }

    // ── Email Verification ────────────────────────────────────────────────────

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmailVerificationToken(request.getToken())
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Invalid email verification token."));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "No account found with that email."));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException("ALREADY_VERIFIED", "Email is already verified.");
        }

        String token = generateSecureToken();
        user.setEmailVerificationToken(token);
        userRepository.save(user);

        String verifyLink = frontendUrl + "/verify-email?token=" + token;
        try {
            emailQueueService.enqueueEmailVerification(user.getEmail(), user.getFirstName(), verifyLink);
        } catch (Exception ignored) {}
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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

