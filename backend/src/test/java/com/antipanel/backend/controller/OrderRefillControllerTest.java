package com.antipanel.backend.controller;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillCreateRequest;
import com.antipanel.backend.dto.orderrefill.OrderRefillResponse;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.RefillStatus;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.OrderRefillService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderRefillController Tests")
class OrderRefillControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderRefillService orderRefillService;

    @InjectMocks
    private OrderRefillController orderRefillController;

    private ObjectMapper objectMapper;
    private CustomUserDetails userDetails;
    private OrderRefillResponse refillResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderRefillController)
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

        refillResponse = OrderRefillResponse.builder()
                .id(1L)
                .orderId(100L)
                .status(RefillStatus.PENDING)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Nested
    @DisplayName("POST /api/v1/refills")
    class CreateRefill {

        @Test
        @DisplayName("Should create refill request successfully")
        void shouldCreateRefillSuccessfully() throws Exception {
            OrderRefillCreateRequest request = OrderRefillCreateRequest.builder()
                    .orderId(100L)
                    .build();

            when(orderRefillService.create(eq(1L), any(OrderRefillCreateRequest.class))).thenReturn(refillResponse);

            mockMvc.perform(post("/api/v1/refills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.orderId").value(100));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/refills")
    class GetUserRefills {

        @Test
        @DisplayName("Should return user's refill requests with pagination")
        void shouldReturnUserRefillsWithPagination() throws Exception {
            PageResponse<OrderRefillResponse> pageResponse = PageResponse.<OrderRefillResponse>builder()
                    .content(List.of(refillResponse))
                    .totalElements(1L)
                    .totalPages(1)
                    .pageNumber(0)
                    .pageSize(20)
                    .first(true)
                    .last(true)
                    .build();

            when(orderRefillService.getByUserPaginated(eq(1L), any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/refills")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/refills/{id}")
    class GetRefill {

        @Test
        @DisplayName("Should return refill for owner")
        void shouldReturnRefillForOwner() throws Exception {
            when(orderRefillService.getById(1L)).thenReturn(refillResponse);
            when(orderRefillService.getByUser(1L)).thenReturn(List.of(refillResponse));

            mockMvc.perform(get("/api/v1/refills/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("Should return 403 for non-owner")
        void shouldReturn403ForNonOwner() throws Exception {
            OrderRefillResponse otherUserRefill = OrderRefillResponse.builder()
                    .id(999L)
                    .orderId(200L)
                    .status(RefillStatus.PENDING)
                    .build();

            when(orderRefillService.getById(999L)).thenReturn(otherUserRefill);
            when(orderRefillService.getByUser(1L)).thenReturn(List.of(refillResponse)); // Current user's refills don't include 999

            mockMvc.perform(get("/api/v1/refills/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/refills/order/{orderId}")
    class GetRefillsByOrder {

        @Test
        @DisplayName("Should return refills for order")
        void shouldReturnRefillsForOrder() throws Exception {
            when(orderRefillService.getByOrder(100L)).thenReturn(List.of(refillResponse));

            mockMvc.perform(get("/api/v1/refills/order/100")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].orderId").value(100));
        }
    }
}
