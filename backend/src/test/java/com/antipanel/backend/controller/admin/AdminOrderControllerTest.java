package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.order.OrderDetailResponse;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.OrderService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminOrderController Tests")
class AdminOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private AdminOrderController adminOrderController;

    private ObjectMapper objectMapper;
    private CustomUserDetails adminDetails;
    private OrderResponse orderResponse;
    private OrderDetailResponse orderDetailResponse;
    private PageResponse<OrderResponse> pageResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminOrderController)
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

        UserSummary userSummary = UserSummary.builder()
                .id(2L)
                .email("user@example.com")
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .user(userSummary)
                .target("https://example.com/post")
                .quantity(1000)
                .totalCharge(new BigDecimal("10.00"))
                .status(OrderStatus.PENDING)
                .build();

        orderDetailResponse = OrderDetailResponse.builder()
                .id(1L)
                .user(userSummary)
                .target("https://example.com/post")
                .quantity(1000)
                .totalCharge(new BigDecimal("10.00"))
                .status(OrderStatus.PENDING)
                .build();

        pageResponse = PageResponse.<OrderResponse>builder()
                .content(List.of(orderResponse))
                .pageNumber(0)
                .pageSize(50)
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
    @DisplayName("GET /api/v1/admin/orders")
    class GetOrdersByStatus {

        @Test
        @DisplayName("Should return paginated orders by status")
        void shouldReturnPaginatedOrdersByStatus() throws Exception {
            when(orderService.getByStatusPaginated(eq(OrderStatus.PENDING), any(Pageable.class)))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/admin/orders")
                            .param("status", "PENDING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/orders/{id}")
    class GetOrderById {

        @Test
        @DisplayName("Should return order detail by ID")
        void shouldReturnOrderDetailById() throws Exception {
            when(orderService.getDetailById(1L)).thenReturn(orderDetailResponse);

            mockMvc.perform(get("/api/v1/admin/orders/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.target").value("https://example.com/post"));
        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            when(orderService.getDetailById(999L))
                    .thenThrow(new ResourceNotFoundException("Order", "id", 999L));

            mockMvc.perform(get("/api/v1/admin/orders/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/orders/status/{status}")
    class GetOrdersByStatusPath {

        @Test
        @DisplayName("Should return orders by status")
        void shouldReturnOrdersByStatus() throws Exception {
            when(orderService.getByStatusPaginated(eq(OrderStatus.PROCESSING), any(Pageable.class)))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/admin/orders/status/PROCESSING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/orders/user/{userId}")
    class GetOrdersByUser {

        @Test
        @DisplayName("Should return orders for user")
        void shouldReturnOrdersForUser() throws Exception {
            when(orderService.getByUserPaginated(eq(2L), any(Pageable.class)))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/admin/orders/user/2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].user.id").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/orders/pending")
    class GetPendingOrders {

        @Test
        @DisplayName("Should return pending orders")
        void shouldReturnPendingOrders() throws Exception {
            when(orderService.getByStatus(OrderStatus.PENDING)).thenReturn(List.of(orderResponse));

            mockMvc.perform(get("/api/v1/admin/orders/pending")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/orders/{id}/status")
    class UpdateOrderStatus {

        @Test
        @DisplayName("Should update order status")
        void shouldUpdateOrderStatus() throws Exception {
            OrderResponse updatedOrder = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.PROCESSING)
                    .build();

            when(orderService.updateStatus(eq(1L), eq(OrderStatus.PROCESSING)))
                    .thenReturn(updatedOrder);

            mockMvc.perform(patch("/api/v1/admin/orders/1/status")
                            .param("status", "PROCESSING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PROCESSING"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/orders/{id}/cancel")
    class CancelOrder {

        @Test
        @DisplayName("Should cancel order")
        void shouldCancelOrder() throws Exception {
            OrderResponse cancelledOrder = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.CANCELLED)
                    .build();

            when(orderService.cancelOrder(1L)).thenReturn(cancelledOrder);

            mockMvc.perform(patch("/api/v1/admin/orders/1/cancel")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/orders/{id}/progress")
    class UpdateProgress {

        @Test
        @DisplayName("Should update order progress")
        void shouldUpdateOrderProgress() throws Exception {
            OrderResponse progressedOrder = OrderResponse.builder()
                    .id(1L)
                    .startCount(100)
                    .remains(500)
                    .status(OrderStatus.PROCESSING)
                    .build();

            when(orderService.updateProgress(eq(1L), eq(100), eq(500)))
                    .thenReturn(progressedOrder);

            mockMvc.perform(patch("/api/v1/admin/orders/1/progress")
                            .param("startCount", "100")
                            .param("remains", "500")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startCount").value(100))
                    .andExpect(jsonPath("$.remains").value(500));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/orders/{id}/refund")
    class RefundOrder {

        @Test
        @DisplayName("Should refund order")
        void shouldRefundOrder() throws Exception {
            OrderResponse refundedOrder = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.REFUNDED)
                    .build();

            when(orderService.refundOrder(1L)).thenReturn(refundedOrder);

            mockMvc.perform(patch("/api/v1/admin/orders/1/refund")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUNDED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/orders/{id}/complete")
    class CompleteOrder {

        @Test
        @DisplayName("Should complete order")
        void shouldCompleteOrder() throws Exception {
            OrderResponse completedOrder = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.COMPLETED)
                    .build();

            when(orderService.completeOrder(1L)).thenReturn(completedOrder);

            mockMvc.perform(patch("/api/v1/admin/orders/1/complete")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }
    }
}
