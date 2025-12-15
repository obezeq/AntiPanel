package com.antipanel.backend.controller;

import com.antipanel.backend.dto.auth.LoginRequest;
import com.antipanel.backend.dto.auth.LoginResponse;
import com.antipanel.backend.dto.auth.RefreshTokenRequest;
import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.exception.UnauthorizedException;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private UserCreateRequest registerRequest;
    private UserResponse userResponse;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        UserSummary userSummary = UserSummary.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.USER)
                .balance(new BigDecimal("100.00"))
                .build();

        loginResponse = LoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userSummary)
                .build();

        registerRequest = UserCreateRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.USER)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .email("newuser@example.com")
                .role(UserRole.USER)
                .build();

        refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.user.email").value("test@example.com"));
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new UnauthorizedException("Invalid email or password"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterSuccessfully() throws Exception {
            when(authService.register(any(UserCreateRequest.class))).thenReturn(userResponse);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("newuser@example.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn409WhenEmailExists() throws Exception {
            when(authService.register(any(UserCreateRequest.class)))
                    .thenThrow(new ConflictException("Email already registered"));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class RefreshToken {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() throws Exception {
            when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(loginResponse);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
        }

        @Test
        @DisplayName("Should return 401 for invalid refresh token")
        void shouldReturn401ForInvalidRefreshToken() throws Exception {
            when(authService.refreshToken(any(RefreshTokenRequest.class)))
                    .thenThrow(new UnauthorizedException("Invalid refresh token"));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class Logout {

        @Test
        @DisplayName("Should logout successfully when authenticated")
        void shouldLogoutSuccessfully() throws Exception {
            // Set up authenticated user in security context
            CustomUserDetails userDetails = new CustomUserDetails(
                    com.antipanel.backend.entity.User.builder()
                            .id(1L)
                            .email("test@example.com")
                            .passwordHash("hash")
                            .role(UserRole.USER)
                            .isBanned(false)
                            .build()
            );
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")))
            );

            doNothing().when(authService).logout(1L);

            // Use standalone setup with resolved current user
            MockMvc mockMvcWithUser = MockMvcBuilders.standaloneSetup(authController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                    .build();

            mockMvcWithUser.perform(post("/api/v1/auth/logout"))
                    .andExpect(status().isNoContent());
        }
    }
}
