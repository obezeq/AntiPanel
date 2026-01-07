package com.antipanel.backend.controller;

import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserStatisticsController Tests")
class UserStatisticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserStatisticsController userStatisticsController;

    private CustomUserDetails userDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userStatisticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver()
                )
                .build();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hash")
                .role(UserRole.USER)
                .balance(new BigDecimal("150.50"))
                .isBanned(false)
                .build();

        userDetails = new CustomUserDetails(testUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/users/me/statistics")
    class GetUserStatistics {

        @Test
        @DisplayName("Should return user statistics successfully")
        void shouldReturnUserStatisticsSuccessfully() throws Exception {
            // Setup mocks
            when(orderRepository.countByUserId(1L)).thenReturn(25L);
            when(orderRepository.countByUserIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(2L);
            when(orderRepository.countByUserIdAndStatus(1L, OrderStatus.PROCESSING)).thenReturn(1L);
            when(orderRepository.countByUserIdAndStatus(1L, OrderStatus.COMPLETED)).thenReturn(20L);
            when(orderRepository.countByUserIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(5L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/v1/users/me/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(25))
                    .andExpect(jsonPath("$.pendingOrders").value(3))  // 2 PENDING + 1 PROCESSING
                    .andExpect(jsonPath("$.completedOrders").value(20))
                    .andExpect(jsonPath("$.ordersThisMonth").value(5))
                    .andExpect(jsonPath("$.balance").value(150.50));
        }

        @Test
        @DisplayName("Should return zero statistics for new user")
        void shouldReturnZeroStatisticsForNewUser() throws Exception {
            User newUser = User.builder()
                    .id(2L)
                    .email("new@example.com")
                    .passwordHash("hash")
                    .role(UserRole.USER)
                    .balance(BigDecimal.ZERO)
                    .isBanned(false)
                    .build();

            CustomUserDetails newUserDetails = new CustomUserDetails(newUser);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(newUserDetails, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")))
            );

            when(orderRepository.countByUserId(2L)).thenReturn(0L);
            when(orderRepository.countByUserIdAndStatus(2L, OrderStatus.PENDING)).thenReturn(0L);
            when(orderRepository.countByUserIdAndStatus(2L, OrderStatus.PROCESSING)).thenReturn(0L);
            when(orderRepository.countByUserIdAndStatus(2L, OrderStatus.COMPLETED)).thenReturn(0L);
            when(orderRepository.countByUserIdAndCreatedAtAfter(eq(2L), any(LocalDateTime.class))).thenReturn(0L);
            when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));

            mockMvc.perform(get("/api/v1/users/me/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(0))
                    .andExpect(jsonPath("$.pendingOrders").value(0))
                    .andExpect(jsonPath("$.completedOrders").value(0))
                    .andExpect(jsonPath("$.ordersThisMonth").value(0))
                    .andExpect(jsonPath("$.balance").value(0));
        }

        @Test
        @DisplayName("Should return statistics with high balance")
        void shouldReturnStatisticsWithHighBalance() throws Exception {
            User richUser = User.builder()
                    .id(1L)
                    .email("test@example.com")
                    .passwordHash("hash")
                    .role(UserRole.USER)
                    .balance(new BigDecimal("9999.9999"))
                    .isBanned(false)
                    .build();

            when(orderRepository.countByUserId(1L)).thenReturn(100L);
            when(orderRepository.countByUserIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(0L);
            when(orderRepository.countByUserIdAndStatus(1L, OrderStatus.PROCESSING)).thenReturn(0L);
            when(orderRepository.countByUserIdAndStatus(1L, OrderStatus.COMPLETED)).thenReturn(100L);
            when(orderRepository.countByUserIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(15L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(richUser));

            mockMvc.perform(get("/api/v1/users/me/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(100))
                    .andExpect(jsonPath("$.completedOrders").value(100))
                    .andExpect(jsonPath("$.ordersThisMonth").value(15))
                    .andExpect(jsonPath("$.balance").value(9999.9999));
        }
    }
}
