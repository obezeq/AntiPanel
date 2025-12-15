package com.antipanel.backend.controller;

import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserUpdateRequest;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.UserService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private CustomUserDetails userDetails;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();

        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hash")
                .role(UserRole.USER)
                .balance(new BigDecimal("100.00"))
                .isBanned(false)
                .build();

        userDetails = new CustomUserDetails(testUser);

        userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.USER)
                .balance(new BigDecimal("100.00"))
                .isBanned(false)
                .build();

        // Set up security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/users/me")
    class GetCurrentUser {

        @Test
        @DisplayName("Should return current user profile")
        void shouldReturnCurrentUserProfile() throws Exception {
            when(userService.getById(1L)).thenReturn(userResponse);

            mockMvc.perform(get("/api/v1/users/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/me")
    class UpdateCurrentUser {

        @Test
        @DisplayName("Should update current user profile")
        void shouldUpdateCurrentUserProfile() throws Exception {
            UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                    .email("newemail@example.com")
                    .build();

            UserResponse updatedResponse = UserResponse.builder()
                    .id(1L)
                    .email("newemail@example.com")
                    .role(UserRole.USER)
                    .build();

            when(userService.update(eq(1L), any(UserUpdateRequest.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/v1/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("newemail@example.com"));
        }
    }
}
