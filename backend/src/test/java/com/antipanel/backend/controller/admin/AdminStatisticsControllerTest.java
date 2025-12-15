package com.antipanel.backend.controller.admin;

import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.repository.InvoiceRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminStatisticsController Tests")
class AdminStatisticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminStatisticsController adminStatisticsController;

    private CustomUserDetails adminDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminStatisticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        User adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .balance(BigDecimal.ZERO)
                .isBanned(false)
                .build();

        adminDetails = new CustomUserDetails(adminUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/statistics/orders")
    class GetOrderStatistics {

        @Test
        @DisplayName("Should return order statistics")
        void shouldReturnOrderStatistics() throws Exception {
            when(orderRepository.count()).thenReturn(100L);
            when(orderRepository.countOrdersByStatus()).thenReturn(List.of(
                    new Object[]{OrderStatus.PENDING, 10L},
                    new Object[]{OrderStatus.PROCESSING, 20L},
                    new Object[]{OrderStatus.COMPLETED, 70L}
            ));
            when(orderRepository.getTotalRevenue()).thenReturn(new BigDecimal("5000.00"));
            when(orderRepository.getTotalCost()).thenReturn(new BigDecimal("2000.00"));
            when(orderRepository.getTotalProfit()).thenReturn(new BigDecimal("3000.00"));

            mockMvc.perform(get("/api/v1/admin/statistics/orders")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(100))
                    .andExpect(jsonPath("$.totalRevenue").value(5000.00))
                    .andExpect(jsonPath("$.totalCost").value(2000.00))
                    .andExpect(jsonPath("$.totalProfit").value(3000.00));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/statistics/orders/range")
    class GetOrderStatisticsByDateRange {

        @Test
        @DisplayName("Should return order statistics for date range")
        void shouldReturnOrderStatisticsForDateRange() throws Exception {
            when(orderRepository.findOrdersBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(orderRepository.getRevenueBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("1000.00"));
            when(orderRepository.getProfitBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("500.00"));

            mockMvc.perform(get("/api/v1/admin/statistics/orders/range")
                            .param("startDate", "2024-01-01")
                            .param("endDate", "2024-12-31")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ordersInRange").value(0))
                    .andExpect(jsonPath("$.revenueInRange").value(1000.00))
                    .andExpect(jsonPath("$.profitInRange").value(500.00));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/statistics/invoices")
    class GetInvoiceStatistics {

        @Test
        @DisplayName("Should return invoice statistics")
        void shouldReturnInvoiceStatistics() throws Exception {
            when(invoiceRepository.count()).thenReturn(50L);
            when(invoiceRepository.countByStatus(any(InvoiceStatus.class))).thenReturn(10L);
            when(invoiceRepository.getTotalRevenue()).thenReturn(new BigDecimal("2500.00"));

            mockMvc.perform(get("/api/v1/admin/statistics/invoices")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalInvoices").value(50))
                    .andExpect(jsonPath("$.totalDeposits").value(2500.00))
                    .andExpect(jsonPath("$.pendingInvoices").value(10));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/statistics/users")
    class GetUserStatistics {

        @Test
        @DisplayName("Should return user statistics")
        void shouldReturnUserStatistics() throws Exception {
            when(userRepository.count()).thenReturn(1000L);
            when(userRepository.findByIsBannedTrue()).thenReturn(Collections.emptyList());
            when(userRepository.getTotalUserBalance()).thenReturn(new BigDecimal("50000.00"));

            mockMvc.perform(get("/api/v1/admin/statistics/users")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalUsers").value(1000))
                    .andExpect(jsonPath("$.bannedUsers").value(0))
                    .andExpect(jsonPath("$.totalUserBalance").value(50000.00));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/statistics/dashboard")
    class GetDashboardSummary {

        @Test
        @DisplayName("Should return dashboard summary")
        void shouldReturnDashboardSummary() throws Exception {
            when(userRepository.count()).thenReturn(1000L);
            when(userRepository.findByIsBannedTrue()).thenReturn(Collections.emptyList());
            when(orderRepository.count()).thenReturn(500L);
            when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(20L);
            when(orderRepository.getTotalRevenue()).thenReturn(new BigDecimal("10000.00"));
            when(orderRepository.getTotalProfit()).thenReturn(new BigDecimal("5000.00"));
            when(invoiceRepository.getTotalRevenue()).thenReturn(new BigDecimal("15000.00"));
            when(invoiceRepository.countByStatus(InvoiceStatus.PENDING)).thenReturn(5L);
            when(orderRepository.findOrdersBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(orderRepository.getRevenueBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("100.00"));

            mockMvc.perform(get("/api/v1/admin/statistics/dashboard")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalUsers").value(1000))
                    .andExpect(jsonPath("$.bannedUsers").value(0))
                    .andExpect(jsonPath("$.totalOrders").value(500))
                    .andExpect(jsonPath("$.pendingOrders").value(20))
                    .andExpect(jsonPath("$.totalRevenue").value(10000.00))
                    .andExpect(jsonPath("$.totalProfit").value(5000.00))
                    .andExpect(jsonPath("$.totalDeposits").value(15000.00))
                    .andExpect(jsonPath("$.pendingInvoices").value(5))
                    .andExpect(jsonPath("$.ordersToday").value(0))
                    .andExpect(jsonPath("$.revenueToday").value(100.00));
        }
    }
}
