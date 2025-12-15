package com.antipanel.backend.service;

import com.antipanel.backend.dto.auth.LoginRequest;
import com.antipanel.backend.dto.auth.LoginResponse;
import com.antipanel.backend.dto.auth.RefreshTokenRequest;
import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.UnauthorizedException;
import com.antipanel.backend.mapper.UserMapper;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.security.jwt.JwtTokenProvider;
import com.antipanel.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private UserCreateRequest registerRequest;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.USER)
                .balance(new BigDecimal("100.00"))
                .isBanned(false)
                .loginCount(0)
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        registerRequest = UserCreateRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.USER)
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() {
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);

            LoginResponse result = authService.login(loginRequest);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should throw exception for bad credentials")
        void shouldThrowExceptionForBadCredentials() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw exception for disabled account")
        void shouldThrowExceptionForDisabledAccount() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new DisabledException("Account disabled"));

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("disabled");
        }

        @Test
        @DisplayName("Should update login count on successful login")
        void shouldUpdateLoginCountOnSuccessfulLogin() {
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);

            authService.login(loginRequest);

            verify(userRepository, times(2)).findById(1L); // Once for update, once for response
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Register")
    class Register {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUserSuccessfully() {
            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            UserResponse result = authService.register(registerRequest);

            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Email already registered");
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class RefreshToken {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("valid-refresh-token")
                    .build();

            when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
            when(jwtTokenProvider.isRefreshToken("valid-refresh-token")).thenReturn(true);
            when(jwtTokenProvider.getUsernameFromToken("valid-refresh-token")).thenReturn("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("new-access-token");
            when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("new-refresh-token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);

            LoginResponse result = authService.refreshToken(request);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("new-access-token");
            assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("Should throw exception for invalid refresh token")
        void shouldThrowExceptionForInvalidRefreshToken() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalid-token")
                    .build();

            when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        @DisplayName("Should throw exception when user is banned")
        void shouldThrowExceptionWhenUserIsBanned() {
            testUser.setIsBanned(true);
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("valid-refresh-token")
                    .build();

            when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
            when(jwtTokenProvider.isRefreshToken("valid-refresh-token")).thenReturn(true);
            when(jwtTokenProvider.getUsernameFromToken("valid-refresh-token")).thenReturn("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("disabled");
        }
    }

    @Nested
    @DisplayName("Logout")
    class Logout {

        @Test
        @DisplayName("Should logout without error")
        void shouldLogoutWithoutError() {
            // Logout is a no-op for stateless JWT, just verify it doesn't throw
            authService.logout(1L);
            // No exception means success
        }
    }
}
