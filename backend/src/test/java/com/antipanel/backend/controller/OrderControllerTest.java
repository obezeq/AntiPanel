package com.antipanel.backend.controller;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.order.OrderCreateRequest;
import com.antipanel.backend.dto.order.OrderDetailResponse;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper;
    private CustomUserDetails userDetails;
    private OrderResponse orderResponse;
    private OrderDetailResponse orderDetailResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();

        objectMapper = new ObjectMapper();

        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hash")
                .role(UserRole.USER)
                .isBanned(false)
                .build();

        userDetails = new CustomUserDetails(testUser);

        UserSummary userSummaryForResponse = UserSummary.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .user(userSummaryForResponse)
                .serviceId(1)
                .target("https://example.com/post/123")
                .quantity(1000)
                .totalCharge(new BigDecimal("5.00"))
                .status(OrderStatus.PENDING)
                .build();

        UserSummary userSummary = UserSummary.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        orderDetailResponse = OrderDetailResponse.builder()
                .id(1L)
                .user(userSummary)
                .target("https://example.com/post/123")
                .quantity(1000)
                .totalCharge(new BigDecimal("5.00"))
                .status(OrderStatus.PENDING)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Nested
    @DisplayName("POST /api/v1/orders")
    class CreateOrder {

        @Test
        @DisplayName("Should create order successfully and submit to provider")
        void shouldCreateOrderSuccessfully() throws Exception {
            OrderCreateRequest request = OrderCreateRequest.builder()
                    .serviceId(1)
                    .target("https://example.com/post/123")
                    .quantity(1000)
                    .build();

            // Mock the two-step order creation process
            when(orderService.create(eq(1L), any(OrderCreateRequest.class))).thenReturn(orderResponse);
            when(orderService.submitOrderToProvider(1L)).thenReturn(orderResponse);

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.quantity").value(1000));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders")
    class GetUserOrders {

        @Test
        @DisplayName("Should return user's orders with pagination")
        void shouldReturnUserOrdersWithPagination() throws Exception {
            PageResponse<OrderResponse> pageResponse = PageResponse.<OrderResponse>builder()
                    .content(List.of(orderResponse))
                    .totalElements(1L)
                    .totalPages(1)
                    .pageNumber(0)
                    .pageSize(20)
                    .first(true)
                    .last(true)
                    .build();

            when(orderService.getByUserPaginated(eq(1L), any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{id}")
    class GetOrder {

        @Test
        @DisplayName("Should return order detail for owner")
        void shouldReturnOrderDetailForOwner() throws Exception {
            when(orderService.getDetailById(1L)).thenReturn(orderDetailResponse);

            mockMvc.perform(get("/api/v1/orders/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("Should return 403 for non-owner")
        void shouldReturn403ForNonOwner() throws Exception {
            UserSummary otherUser = UserSummary.builder()
                    .id(999L)  // Different user
                    .email("other@example.com")
                    .build();

            OrderDetailResponse otherUserOrder = OrderDetailResponse.builder()
                    .id(1L)
                    .user(otherUser)
                    .build();

            when(orderService.getDetailById(1L)).thenReturn(otherUserOrder);

            mockMvc.perform(get("/api/v1/orders/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/active")
    class GetActiveOrders {

        @Test
        @DisplayName("Should return user's active orders")
        void shouldReturnUserActiveOrders() throws Exception {
            when(orderService.getActiveByUser(1L)).thenReturn(List.of(orderResponse));

            mockMvc.perform(get("/api/v1/orders/active")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/refillable")
    class GetRefillableOrders {

        @Test
        @DisplayName("Should return user's refillable orders")
        void shouldReturnUserRefillableOrders() throws Exception {
            when(orderService.getRefillableByUser(1L)).thenReturn(List.of(orderResponse));

            mockMvc.perform(get("/api/v1/orders/refillable")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));
        }
    }
}
