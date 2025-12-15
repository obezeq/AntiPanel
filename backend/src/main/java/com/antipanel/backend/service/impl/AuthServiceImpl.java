package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.auth.LoginRequest;
import com.antipanel.backend.dto.auth.LoginResponse;
import com.antipanel.backend.dto.auth.RefreshTokenRequest;
import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.BadRequestException;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.UnauthorizedException;
import com.antipanel.backend.mapper.UserMapper;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.security.jwt.JwtTokenProvider;
import com.antipanel.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Implementation of AuthService.
 * Handles authentication, registration, and token management.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.debug("Login attempt for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Update last login
            userRepository.findById(userDetails.getUserId())
                    .ifPresent(user -> {
                        user.setLastLoginAt(LocalDateTime.now());
                        user.setLoginCount(user.getLoginCount() + 1);
                        userRepository.save(user);
                    });

            String authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            String accessToken = jwtTokenProvider.generateAccessToken(userDetails.getUsername(), authorities);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUsername());

            User user = userRepository.findById(userDetails.getUserId())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            UserSummary userSummary = UserSummary.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .balance(user.getBalance())
                    .build();

            log.info("User logged in: {}", request.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000) // Convert to seconds
                    .user(userSummary)
                    .build();

        } catch (DisabledException e) {
            log.debug("Login failed - account disabled: {}", request.getEmail());
            throw new UnauthorizedException("Account is disabled. Please contact support.");
        } catch (BadCredentialsException e) {
            log.debug("Login failed - bad credentials: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public UserResponse register(UserCreateRequest request) {
        log.debug("Registration attempt for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.debug("Registration failed - email already exists: {}", request.getEmail());
            throw new ConflictException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER) // Default role for self-registration
                .balance(BigDecimal.ZERO)
                .isBanned(false)
                .loginCount(0)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getEmail());

        return userMapper.toResponse(saved);
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Token refresh attempt");

        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        // Ensure it's a refresh token, not an access token
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid token type. Please provide a refresh token.");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Load user to verify they still exist and are not banned
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getIsBanned()) {
            throw new UnauthorizedException("Account is disabled");
        }

        String authorities = "ROLE_" + user.getRole().name();
        String newAccessToken = jwtTokenProvider.generateAccessToken(username, authorities);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        UserSummary userSummary = UserSummary.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .balance(user.getBalance())
                .build();

        log.debug("Token refreshed for user: {}", username);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
                .user(userSummary)
                .build();
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        log.debug("Logout for user ID: {}", userId);
        // For stateless JWT authentication, logout is handled client-side
        // by removing tokens from storage.
        // If token blacklisting is needed, implement it here with Redis/DB storage.
        log.info("User logged out: {}", userId);
    }
}
