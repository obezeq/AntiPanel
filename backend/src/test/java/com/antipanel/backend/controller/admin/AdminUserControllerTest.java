package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserUpdateRequest;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserController Tests")
class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    private ObjectMapper objectMapper;
    private CustomUserDetails adminDetails;
    private UserResponse userResponse;
    private PageResponse<UserResponse> pageResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();

        User adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .balance(BigDecimal.ZERO)
                .isBanned(false)
                .build();

        adminDetails = new CustomUserDetails(adminUser);

        userResponse = UserResponse.builder()
                .id(2L)
                .email("user@example.com")
                .role(UserRole.USER)
                .balance(new BigDecimal("100.00"))
                .isBanned(false)
                .build();

        pageResponse = PageResponse.<UserResponse>builder()
                .content(List.of(userResponse))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users")
    class GetAllUsers {

        @Test
        @DisplayName("Should return paginated list of users")
        void shouldReturnPaginatedUsers() throws Exception {
            when(userService.getAll(any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].email").value("user@example.com"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("Should return user by ID")
        void shouldReturnUserById() throws Exception {
            when(userService.getById(2L)).thenReturn(userResponse);

            mockMvc.perform(get("/api/v1/admin/users/2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.email").value("user@example.com"));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(userService.getById(999L))
                    .thenThrow(new ResourceNotFoundException("User", "id", 999L));

            mockMvc.perform(get("/api/v1/admin/users/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/search")
    class SearchUsers {

        @Test
        @DisplayName("Should search users by email")
        void shouldSearchUsersByEmail() throws Exception {
            when(userService.search(eq("user"), any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/admin/users/search")
                            .param("email", "user")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].email").value("user@example.com"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/role/{role}")
    class GetUsersByRole {

        @Test
        @DisplayName("Should return users by role")
        void shouldReturnUsersByRole() throws Exception {
            when(userService.getByRole(eq(UserRole.USER), any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/admin/users/role/USER")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].role").value("USER"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/users")
    class CreateUser {

        @Test
        @DisplayName("Should create new user")
        void shouldCreateNewUser() throws Exception {
            UserCreateRequest request = UserCreateRequest.builder()
                    .email("newuser@example.com")
                    .password("password123")
                    .role(UserRole.USER)
                    .build();

            UserResponse createdUser = UserResponse.builder()
                    .id(3L)
                    .email("newuser@example.com")
                    .role(UserRole.USER)
                    .balance(BigDecimal.ZERO)
                    .isBanned(false)
                    .build();

            when(userService.create(any(UserCreateRequest.class))).thenReturn(createdUser);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.email").value("newuser@example.com"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("Should update user")
        void shouldUpdateUser() throws Exception {
            UserUpdateRequest request = UserUpdateRequest.builder()
                    .email("updated@example.com")
                    .build();

            UserResponse updatedUser = UserResponse.builder()
                    .id(2L)
                    .email("updated@example.com")
                    .role(UserRole.USER)
                    .build();

            when(userService.update(eq(2L), any(UserUpdateRequest.class))).thenReturn(updatedUser);

            mockMvc.perform(put("/api/v1/admin/users/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("updated@example.com"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("Should delete user")
        void shouldDeleteUser() throws Exception {
            doNothing().when(userService).delete(2L);

            mockMvc.perform(delete("/api/v1/admin/users/2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).delete(2L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/ban")
    class BanUser {

        @Test
        @DisplayName("Should ban user")
        void shouldBanUser() throws Exception {
            UserResponse bannedUser = UserResponse.builder()
                    .id(2L)
                    .email("user@example.com")
                    .role(UserRole.USER)
                    .isBanned(true)
                    .bannedReason("Violation of terms")
                    .build();

            when(userService.ban(eq(2L), eq("Violation of terms"))).thenReturn(bannedUser);

            mockMvc.perform(patch("/api/v1/admin/users/2/ban")
                            .param("reason", "Violation of terms")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isBanned").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/unban")
    class UnbanUser {

        @Test
        @DisplayName("Should unban user")
        void shouldUnbanUser() throws Exception {
            UserResponse unbannedUser = UserResponse.builder()
                    .id(2L)
                    .email("user@example.com")
                    .role(UserRole.USER)
                    .isBanned(false)
                    .build();

            when(userService.unban(2L)).thenReturn(unbannedUser);

            mockMvc.perform(patch("/api/v1/admin/users/2/unban")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isBanned").value(false));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/balance")
    class AdjustBalance {

        @Test
        @DisplayName("Should adjust user balance")
        void shouldAdjustUserBalance() throws Exception {
            UserResponse adjustedUser = UserResponse.builder()
                    .id(2L)
                    .email("user@example.com")
                    .role(UserRole.USER)
                    .balance(new BigDecimal("150.00"))
                    .build();

            when(userService.adjustBalance(eq(2L), eq(new BigDecimal("50.00")))).thenReturn(adjustedUser);

            mockMvc.perform(patch("/api/v1/admin/users/2/balance")
                            .param("amount", "50.00")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(150.00));
        }
    }
}
