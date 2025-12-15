package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillCreateRequest;
import com.antipanel.backend.dto.orderrefill.OrderRefillResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillSummary;
import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.OrderRefill;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.RefillStatus;
import com.antipanel.backend.exception.BadRequestException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.OrderRefillMapper;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.repository.OrderRefillRepository;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.service.impl.OrderRefillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderRefillService Tests")
class OrderRefillServiceTest {

    @Mock
    private OrderRefillRepository orderRefillRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderRefillMapper orderRefillMapper;

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private OrderRefillServiceImpl orderRefillService;

    private User testUser;
    private Order testOrder;
    private OrderRefill testRefill;
    private OrderRefillResponse testRefillResponse;
    private OrderRefillCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .quantity(1000)
                .status(OrderStatus.COMPLETED)
                .isRefillable(true)
                .refillDays(30)
                .refillDeadline(LocalDateTime.now().plusDays(25))
                .build();

        testRefill = OrderRefill.builder()
                .id(1L)
                .order(testOrder)
                .quantity(1000)
                .status(RefillStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        testRefillResponse = OrderRefillResponse.builder()
                .id(1L)
                .orderId(1L)
                .quantity(1000)
                .status(RefillStatus.PENDING)
                .build();

        createRequest = OrderRefillCreateRequest.builder()
                .orderId(1L)
                .build();
    }

    @Nested
    @DisplayName("Create Operations")
    class CreateOperations {

        @Test
        @DisplayName("Should create refill successfully")
        void shouldCreateRefillSuccessfully() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRefillRepository.hasPendingRefill(1L)).thenReturn(false);
            when(orderRefillRepository.save(any(OrderRefill.class))).thenReturn(testRefill);
            when(orderRefillMapper.toResponse(any(OrderRefill.class))).thenReturn(testRefillResponse);

            OrderRefillResponse result = orderRefillService.create(1L, createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(orderRefillRepository).save(any(OrderRefill.class));
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());
            createRequest.setOrderId(999L);

            assertThatThrownBy(() -> orderRefillService.create(1L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order");
        }

        @Test
        @DisplayName("Should throw exception when order does not belong to user")
        void shouldThrowExceptionWhenOrderDoesNotBelongToUser() {
            User otherUser = User.builder().id(2L).build();
            testOrder.setUser(otherUser);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderRefillService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not belong");
        }

        @Test
        @DisplayName("Should throw exception when order is not eligible for refill")
        void shouldThrowExceptionWhenOrderNotEligibleForRefill() {
            testOrder.setIsRefillable(false);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderRefillService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not eligible");
        }

        @Test
        @DisplayName("Should throw exception when order already has pending refill")
        void shouldThrowExceptionWhenOrderHasPendingRefill() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRefillRepository.hasPendingRefill(1L)).thenReturn(true);

            assertThatThrownBy(() -> orderRefillService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("pending refill");
        }
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadOperations {

        @Test
        @DisplayName("Should get refill by ID")
        void shouldGetRefillById() {
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));
            when(orderRefillMapper.toResponse(testRefill)).thenReturn(testRefillResponse);

            OrderRefillResponse result = orderRefillService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when refill not found")
        void shouldThrowExceptionWhenRefillNotFound() {
            when(orderRefillRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderRefillService.getById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("OrderRefill");
        }

        @Test
        @DisplayName("Should get refill by provider refill ID")
        void shouldGetRefillByProviderRefillId() {
            testRefill.setProviderRefillId("PROV-REFILL-123");
            when(orderRefillRepository.findByProviderRefillId("PROV-REFILL-123"))
                    .thenReturn(Optional.of(testRefill));
            when(orderRefillMapper.toResponse(testRefill)).thenReturn(testRefillResponse);

            OrderRefillResponse result = orderRefillService.getByProviderRefillId("PROV-REFILL-123");

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("By Order Queries")
    class ByOrderQueries {

        @Test
        @DisplayName("Should get refills by order")
        void shouldGetRefillsByOrder() {
            when(orderRefillRepository.findByOrderIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(testRefill));
            when(orderRefillMapper.toResponseList(anyList())).thenReturn(List.of(testRefillResponse));

            List<OrderRefillResponse> result = orderRefillService.getByOrder(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should check if order has pending refill")
        void shouldCheckIfOrderHasPendingRefill() {
            when(orderRefillRepository.hasPendingRefill(1L)).thenReturn(true);

            boolean result = orderRefillService.hasPendingRefill(1L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should count refills by order")
        void shouldCountRefillsByOrder() {
            when(orderRefillRepository.countByOrderId(1L)).thenReturn(5L);

            long result = orderRefillService.countByOrder(1L);

            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("By Status Queries")
    class ByStatusQueries {

        @Test
        @DisplayName("Should get refills by status")
        void shouldGetRefillsByStatus() {
            when(orderRefillRepository.findByStatusOrderByCreatedAtDesc(RefillStatus.PENDING))
                    .thenReturn(List.of(testRefill));
            when(orderRefillMapper.toResponseList(anyList())).thenReturn(List.of(testRefillResponse));

            List<OrderRefillResponse> result = orderRefillService.getByStatus(RefillStatus.PENDING);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get refills by status paginated")
        void shouldGetRefillsByStatusPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderRefill> page = new PageImpl<>(List.of(testRefill), pageable, 1);
            PageResponse<OrderRefillResponse> expectedPageResponse = PageResponse.<OrderRefillResponse>builder()
                    .content(List.of(testRefillResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(orderRefillRepository.findByStatus(RefillStatus.PENDING, pageable)).thenReturn(page);
            when(orderRefillMapper.toResponseList(anyList())).thenReturn(List.of(testRefillResponse));
            doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

            PageResponse<OrderRefillResponse> result = orderRefillService.getByStatusPaginated(RefillStatus.PENDING, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get pending refills")
        void shouldGetPendingRefills() {
            when(orderRefillRepository.findPendingRefills()).thenReturn(List.of(testRefill));
            when(orderRefillMapper.toResponseList(anyList())).thenReturn(List.of(testRefillResponse));

            List<OrderRefillResponse> result = orderRefillService.getPendingRefills();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("By User Queries")
    class ByUserQueries {

        @Test
        @DisplayName("Should get refills by user")
        void shouldGetRefillsByUser() {
            when(orderRefillRepository.findByUserId(1L)).thenReturn(List.of(testRefill));
            when(orderRefillMapper.toResponseList(anyList())).thenReturn(List.of(testRefillResponse));

            List<OrderRefillResponse> result = orderRefillService.getByUser(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get refills by user paginated")
        void shouldGetRefillsByUserPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderRefill> page = new PageImpl<>(List.of(testRefill), pageable, 1);
            PageResponse<OrderRefillResponse> expectedPageResponse = PageResponse.<OrderRefillResponse>builder()
                    .content(List.of(testRefillResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(orderRefillRepository.findByOrderUserId(1L, pageable)).thenReturn(page);
            when(orderRefillMapper.toResponseList(anyList())).thenReturn(List.of(testRefillResponse));
            doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

            PageResponse<OrderRefillResponse> result = orderRefillService.getByUserPaginated(1L, pageable);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Time-Based Queries")
    class TimeBasedQueries {

        @Test
        @DisplayName("Should get refills between dates")
        void shouldGetRefillsBetweenDates() {
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();
            when(orderRefillRepository.findRefillsBetweenDates(start, end))
                    .thenReturn(List.of(testRefill));
            when(orderRefillMapper.toResponseList(anyList())).thenReturn(List.of(testRefillResponse));

            List<OrderRefillResponse> result = orderRefillService.getRefillsBetweenDates(start, end);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Status Operations")
    class StatusOperations {

        @Test
        @DisplayName("Should update refill status")
        void shouldUpdateRefillStatus() {
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));
            when(orderRefillRepository.save(any(OrderRefill.class))).thenReturn(testRefill);
            when(orderRefillMapper.toResponse(any(OrderRefill.class))).thenReturn(testRefillResponse);

            OrderRefillResponse result = orderRefillService.updateStatus(1L, RefillStatus.PROCESSING);

            assertThat(result).isNotNull();
            verify(orderRefillRepository).save(any(OrderRefill.class));
        }

        @Test
        @DisplayName("Should throw exception when updating status of final refill")
        void shouldThrowExceptionWhenUpdatingFinalRefillStatus() {
            testRefill.setStatus(RefillStatus.COMPLETED);
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));

            assertThatThrownBy(() -> orderRefillService.updateStatus(1L, RefillStatus.PROCESSING))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("final state");
        }

        @Test
        @DisplayName("Should mark refill as processing")
        void shouldMarkRefillAsProcessing() {
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));
            when(orderRefillRepository.save(any(OrderRefill.class))).thenReturn(testRefill);
            when(orderRefillMapper.toResponse(any(OrderRefill.class))).thenReturn(testRefillResponse);

            OrderRefillResponse result = orderRefillService.markAsProcessing(1L, "PROV-REFILL-123");

            assertThat(result).isNotNull();
            verify(orderRefillRepository).save(any(OrderRefill.class));
        }

        @Test
        @DisplayName("Should throw exception when marking non-pending refill as processing")
        void shouldThrowExceptionWhenMarkingNonPendingAsProcessing() {
            testRefill.setStatus(RefillStatus.PROCESSING);
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));

            assertThatThrownBy(() -> orderRefillService.markAsProcessing(1L, "PROV-REFILL-123"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("Should complete refill")
        void shouldCompleteRefill() {
            testRefill.setStatus(RefillStatus.PROCESSING);
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));
            when(orderRefillRepository.save(any(OrderRefill.class))).thenReturn(testRefill);
            when(orderRefillMapper.toResponse(any(OrderRefill.class))).thenReturn(testRefillResponse);

            OrderRefillResponse result = orderRefillService.completeRefill(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when completing final refill")
        void shouldThrowExceptionWhenCompletingFinalRefill() {
            testRefill.setStatus(RefillStatus.COMPLETED);
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));

            assertThatThrownBy(() -> orderRefillService.completeRefill(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("final state");
        }

        @Test
        @DisplayName("Should cancel refill")
        void shouldCancelRefill() {
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));
            when(orderRefillRepository.save(any(OrderRefill.class))).thenReturn(testRefill);
            when(orderRefillMapper.toResponse(any(OrderRefill.class))).thenReturn(testRefillResponse);

            OrderRefillResponse result = orderRefillService.cancelRefill(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when cancelling final refill")
        void shouldThrowExceptionWhenCancellingFinalRefill() {
            testRefill.setStatus(RefillStatus.COMPLETED);
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));

            assertThatThrownBy(() -> orderRefillService.cancelRefill(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("final state");
        }

        @Test
        @DisplayName("Should reject refill")
        void shouldRejectRefill() {
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));
            when(orderRefillRepository.save(any(OrderRefill.class))).thenReturn(testRefill);
            when(orderRefillMapper.toResponse(any(OrderRefill.class))).thenReturn(testRefillResponse);

            OrderRefillResponse result = orderRefillService.rejectRefill(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when rejecting final refill")
        void shouldThrowExceptionWhenRejectingFinalRefill() {
            testRefill.setStatus(RefillStatus.COMPLETED);
            when(orderRefillRepository.findById(1L)).thenReturn(Optional.of(testRefill));

            assertThatThrownBy(() -> orderRefillService.rejectRefill(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("final state");
        }
    }

    @Nested
    @DisplayName("Statistics")
    class Statistics {

        @Test
        @DisplayName("Should count refills by status")
        void shouldCountRefillsByStatus() {
            when(orderRefillRepository.countByStatus(RefillStatus.PENDING)).thenReturn(15L);

            long result = orderRefillService.countByStatus(RefillStatus.PENDING);

            assertThat(result).isEqualTo(15L);
        }
    }

    @Nested
    @DisplayName("Summaries")
    class Summaries {

        @Test
        @DisplayName("Should get all refill summaries")
        void shouldGetAllRefillSummaries() {
            OrderRefillSummary summary = OrderRefillSummary.builder()
                    .id(1L)
                    .orderId(1L)
                    .status(RefillStatus.PENDING)
                    .build();

            when(orderRefillRepository.findAll()).thenReturn(List.of(testRefill));
            when(orderRefillMapper.toSummaryList(anyList())).thenReturn(List.of(summary));

            List<OrderRefillSummary> result = orderRefillService.getAllSummaries();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(RefillStatus.PENDING);
        }
    }
}
