package com.cartwave.auth.service;

import com.cartwave.auth.dto.JwtAuthResponse;
import com.cartwave.auth.dto.LoginRequest;
import com.cartwave.auth.dto.RefreshTokenRequest;
import com.cartwave.auth.dto.RegisterRequest;
import com.cartwave.auth.dto.UserDTO;
import com.cartwave.exception.UnauthorizedException;
import com.cartwave.security.service.JwtTokenProvider;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.entity.UserStatus;
import com.cartwave.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final UserDetailsService userDetailsService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       LoginAttemptService loginAttemptService,
                       UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.userDetailsService = userDetailsService;
    }

    public JwtAuthResponse login(LoginRequest loginRequest) {
        if (loginAttemptService.isLocked(loginRequest.getEmail())) {
            throw new UnauthorizedException("Too many login attempts. Account temporarily locked.");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            loginAttemptService.onSuccess(loginRequest.getEmail());
            return JwtAuthResponse.builder()
                    .accessToken(tokenProvider.generateToken(principal))
                    .refreshToken(tokenProvider.generateRefreshToken(principal))
                    .build();
        } catch (Exception ex) {
            loginAttemptService.onFailure(loginRequest.getEmail());
            throw ex;
        }
    }

    public JwtAuthResponse refreshToken(RefreshTokenRequest request) {
        String username = tokenProvider.extractUsername(request.getRefreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!tokenProvider.validateToken(request.getRefreshToken(), userDetails)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        return JwtAuthResponse.builder()
                .accessToken(tokenProvider.generateToken(userDetails))
                .refreshToken(tokenProvider.generateRefreshToken(userDetails))
                .build();
    }

    public UserDTO register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email address already in use.");
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(UserRole.valueOf(registerRequest.getRole()));
        user.setStatus(UserStatus.ACTIVE);
        user.setDeleted(false);
        User saved = userRepository.save(user);

        return UserDTO.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .status(saved.getStatus().name())
                .build();
    }
}
