package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.order.OrderCreateRequest;
import com.antipanel.backend.dto.order.OrderDetailResponse;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.dto.order.OrderSummary;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.ProviderService;
import com.antipanel.backend.entity.Service;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.exception.BadRequestException;
import com.antipanel.backend.exception.InsufficientBalanceException;
import com.antipanel.backend.exception.ProviderApiException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.OrderMapper;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.repository.ServiceRepository;
import com.antipanel.backend.repository.TransactionRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.ExternalOrderService;
import com.antipanel.backend.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PageMapper pageMapper;

    @Mock
    private ExternalOrderService externalOrderService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Service testService;
    private ProviderService testProviderService;
    private Order testOrder;
    private OrderResponse testOrderResponse;
    private OrderDetailResponse testOrderDetailResponse;
    private OrderCreateRequest createRequest;
    private UserSummary testUserSummary;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .balance(new BigDecimal("100.00"))
                .isBanned(false)
                .build();

        testProviderService = ProviderService.builder()
                .id(1)
                .costPerK(new BigDecimal("0.50"))
                .build();

        testService = Service.builder()
                .id(1)
                .name("Instagram Followers")
                .pricePerK(new BigDecimal("1.00"))
                .minQuantity(100)
                .maxQuantity(10000)
                .isActive(true)
                .refillDays(30)
                .providerService(testProviderService)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .service(testService)
                .serviceName("Instagram Followers")
                .providerService(testProviderService)
                .target("https://instagram.com/test")
                .quantity(1000)
                .remains(1000)
                .status(OrderStatus.PENDING)
                .pricePerK(new BigDecimal("1.00"))
                .costPerK(new BigDecimal("0.50"))
                .totalCharge(new BigDecimal("1.00"))
                .totalCost(new BigDecimal("0.50"))
                .profit(new BigDecimal("0.50"))
                .isRefillable(true)
                .refillDays(30)
                .createdAt(LocalDateTime.now())
                .build();

        testUserSummary = UserSummary.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testOrderResponse = OrderResponse.builder()
                .id(1L)
                .user(testUserSummary)
                .serviceId(1)
                .serviceName("Instagram Followers")
                .target("https://instagram.com/test")
                .quantity(1000)
                .remains(1000)
                .status(OrderStatus.PENDING)
                .totalCharge(new BigDecimal("1.00"))
                .isRefillable(true)
                .refillDays(30)
                .build();

        testOrderDetailResponse = OrderDetailResponse.builder()
                .id(1L)
                .target("https://instagram.com/test")
                .quantity(1000)
                .status(OrderStatus.PENDING)
                .build();

        createRequest = OrderCreateRequest.builder()
                .serviceId(1)
                .target("https://instagram.com/test")
                .quantity(1000)
                .build();
    }

    @Nested
    @DisplayName("Create Operations")
    class CreateOperations {

        @Test
        @DisplayName("Should create order successfully without calling external provider")
        void shouldCreateOrderSuccessfully() {
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
            when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(Transaction.builder().build());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);

            OrderResponse result = orderService.create(1L, createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(orderRepository).save(any(Order.class));
            verify(transactionRepository).save(any(Transaction.class));
            verify(userRepository).save(any(User.class));
            // create() should NOT call external provider - that's done in submitOrderToProvider()
            verify(externalOrderService, never()).submitOrder(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.create(999L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("Should throw exception when user is banned")
        void shouldThrowExceptionWhenUserIsBanned() {
            testUser.setIsBanned(true);
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> orderService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("banned");
        }

        @Test
        @DisplayName("Should throw exception when service not found")
        void shouldThrowExceptionWhenServiceNotFound() {
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
            when(serviceRepository.findById(999)).thenReturn(Optional.empty());
            createRequest.setServiceId(999);

            assertThatThrownBy(() -> orderService.create(1L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Service");
        }

        @Test
        @DisplayName("Should throw exception when service is not active")
        void shouldThrowExceptionWhenServiceNotActive() {
            testService.setIsActive(false);
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
            when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));

            assertThatThrownBy(() -> orderService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("Should throw exception when quantity is invalid")
        void shouldThrowExceptionWhenQuantityInvalid() {
            createRequest.setQuantity(50); // Below min of 100
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
            when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));

            assertThatThrownBy(() -> orderService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Quantity");
        }

        @Test
        @DisplayName("Should throw exception when user has insufficient balance")
        void shouldThrowExceptionWhenInsufficientBalance() {
            testUser.setBalance(new BigDecimal("0.01")); // Very low balance
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
            when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));

            assertThatThrownBy(() -> orderService.create(1L, createRequest))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessageContaining("Insufficient balance");
        }

        @Test
        @DisplayName("Should deduct balance and create transaction on order creation")
        void shouldDeductBalanceAndCreateTransaction() {
            BigDecimal initialBalance = new BigDecimal("100.00");
            testUser.setBalance(initialBalance);

            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
            when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(Transaction.builder().build());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);

            orderService.create(1L, createRequest);

            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());

            Transaction savedTransaction = transactionCaptor.getValue();
            assertThat(savedTransaction.getType()).isEqualTo(TransactionType.ORDER);
            assertThat(savedTransaction.getAmount()).isNegative();
            assertThat(savedTransaction.getReferenceType()).isEqualTo("ORDER");
        }

        @Test
        @DisplayName("Should return existing order when idempotency key already exists")
        void shouldReturnExistingOrderWhenIdempotencyKeyExists() {
            String idempotencyKey = "test-idempotency-key-123";
            createRequest.setIdempotencyKey(idempotencyKey);
            testOrder.setIdempotencyKey(idempotencyKey);

            // User lock must be acquired FIRST (fixes race condition)
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
            when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

            OrderResponse result = orderService.create(1L, createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            // Should NOT call save or external service - just return existing order
            verify(orderRepository, never()).save(any(Order.class));
            verify(externalOrderService, never()).submitOrder(any(Order.class));
        }

    }

    @Nested
    @DisplayName("Submit To Provider Operations")
    class SubmitToProviderOperations {

        @Test
        @DisplayName("Should submit order to provider successfully")
        void shouldSubmitOrderToProviderSuccessfully() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(externalOrderService.submitOrder(testOrder)).thenReturn(testOrderResponse);

            OrderResponse result = orderService.submitOrderToProvider(1L);

            assertThat(result).isNotNull();
            verify(externalOrderService).submitOrder(testOrder);
        }

        @Test
        @DisplayName("Should skip submission if order not in PENDING status")
        void shouldSkipSubmissionIfNotPending() {
            testOrder.setStatus(OrderStatus.PROCESSING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

            OrderResponse result = orderService.submitOrderToProvider(1L);

            assertThat(result).isNotNull();
            // Should NOT call external provider
            verify(externalOrderService, never()).submitOrder(any(Order.class));
        }

        @Test
        @DisplayName("Should compensate and throw when provider API fails")
        void shouldCompensateWhenProviderApiFails() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(externalOrderService.submitOrder(testOrder))
                    .thenThrow(new ProviderApiException("TestProvider", "order", "API Error"));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(Transaction.builder().build());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            assertThatThrownBy(() -> orderService.submitOrderToProvider(1L))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("API Error");

            // Verify compensation was triggered
            verify(orderRepository, times(2)).findById(1L); // Once in submit, once in compensate
        }

        @Test
        @DisplayName("Should throw exception when order not found for submission")
        void shouldThrowExceptionWhenOrderNotFoundForSubmission() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.submitOrderToProvider(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order");
        }
    }

    @Nested
    @DisplayName("Compensation Operations")
    class CompensationOperations {

        @Test
        @DisplayName("Should compensate failed order by refunding balance")
        void shouldCompensateFailedOrderByRefundingBalance() {
            BigDecimal initialBalance = new BigDecimal("99.00"); // After deduction
            testUser.setBalance(initialBalance);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(Transaction.builder().build());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            orderService.compensateFailedOrder(1L);

            // Verify refund transaction was created
            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());
            Transaction refundTransaction = transactionCaptor.getValue();
            assertThat(refundTransaction.getType()).isEqualTo(TransactionType.REFUND);
            assertThat(refundTransaction.getAmount()).isPositive();

            // Verify order was marked as FAILED
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());
            assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.FAILED);
        }

        @Test
        @DisplayName("Should skip compensation if order already FAILED")
        void shouldSkipCompensationIfAlreadyFailed() {
            testOrder.setStatus(OrderStatus.FAILED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            orderService.compensateFailedOrder(1L);

            // Should NOT create refund or save order again
            verify(transactionRepository, never()).save(any(Transaction.class));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should skip compensation if order already REFUNDED")
        void shouldSkipCompensationIfAlreadyRefunded() {
            testOrder.setStatus(OrderStatus.REFUNDED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            orderService.compensateFailedOrder(1L);

            // Should NOT create refund or save order again
            verify(transactionRepository, never()).save(any(Transaction.class));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when order not found for compensation")
        void shouldThrowExceptionWhenOrderNotFoundForCompensation() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.compensateFailedOrder(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order");
        }
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadOperations {

        @Test
        @DisplayName("Should get order by ID")
        void shouldGetOrderById() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

            OrderResponse result = orderService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order");
        }

        @Test
        @DisplayName("Should get order detail by ID")
        void shouldGetOrderDetailById() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toDetailResponse(testOrder)).thenReturn(testOrderDetailResponse);

            OrderDetailResponse result = orderService.getDetailById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should get order by provider order ID")
        void shouldGetOrderByProviderOrderId() {
            testOrder.setProviderOrderId("PROV-123");
            when(orderRepository.findByProviderOrderId("PROV-123")).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

            OrderResponse result = orderService.getByProviderOrderId("PROV-123");

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("User Queries")
    class UserQueries {

        @Test
        @DisplayName("Should get orders by user")
        void shouldGetOrdersByUser() {
            when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testOrderResponse));

            List<OrderResponse> result = orderService.getByUser(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get orders by user paginated")
        void shouldGetOrdersByUserPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            PageResponse<OrderResponse> expectedPageResponse = PageResponse.<OrderResponse>builder()
                    .content(List.of(testOrderResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(orderRepository.findByUserId(1L, pageable)).thenReturn(page);
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testOrderResponse));
            doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

            PageResponse<OrderResponse> result = orderService.getByUserPaginated(1L, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get orders by user and status")
        void shouldGetOrdersByUserAndStatus() {
            when(orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, OrderStatus.PENDING))
                    .thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testOrderResponse));

            List<OrderResponse> result = orderService.getByUserAndStatus(1L, OrderStatus.PENDING);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get active orders by user")
        void shouldGetActiveOrdersByUser() {
            when(orderRepository.findActiveOrdersByUser(1L)).thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testOrderResponse));

            List<OrderResponse> result = orderService.getActiveByUser(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get refillable orders by user")
        void shouldGetRefillableOrdersByUser() {
            when(orderRepository.findRefillableOrdersByUser(eq(1L), any(LocalDateTime.class)))
                    .thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testOrderResponse));

            List<OrderResponse> result = orderService.getRefillableByUser(1L);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Admin Queries")
    class AdminQueries {

        @Test
        @DisplayName("Should get orders by status")
        void shouldGetOrdersByStatus() {
            when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING))
                    .thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testOrderResponse));

            List<OrderResponse> result = orderService.getByStatus(OrderStatus.PENDING);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get orders by status paginated")
        void shouldGetOrdersByStatusPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            PageResponse<OrderResponse> expectedPageResponse = PageResponse.<OrderResponse>builder()
                    .content(List.of(testOrderResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(orderRepository.findByStatus(OrderStatus.PENDING, pageable)).thenReturn(page);
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testOrderResponse));
            doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

            PageResponse<OrderResponse> result = orderService.getByStatusPaginated(OrderStatus.PENDING, pageable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should get orders by service")
        void shouldGetOrdersByService() {
            when(orderRepository.findByServiceIdOrderByCreatedAtDesc(1)).thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testOrderResponse));

            List<OrderResponse> result = orderService.getByService(1);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get orders needing update")
        void shouldGetOrdersNeedingUpdate() {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
            when(orderRepository.findOrdersNeedingUpdate(threshold)).thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testOrderResponse));

            List<OrderResponse> result = orderService.getOrdersNeedingUpdate(threshold);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Status Operations")
    class StatusOperations {

        @Test
        @DisplayName("Should update order status")
        void shouldUpdateOrderStatus() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);

            OrderResponse result = orderService.updateStatus(1L, OrderStatus.PROCESSING);

            assertThat(result).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when updating status of final order")
        void shouldThrowExceptionWhenUpdatingFinalOrderStatus() {
            testOrder.setStatus(OrderStatus.COMPLETED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.PROCESSING))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("final state");
        }

        @Test
        @DisplayName("Should mark order as processing")
        void shouldMarkOrderAsProcessing() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);

            OrderResponse result = orderService.markAsProcessing(1L, "PROV-123");

            assertThat(result).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when marking non-pending order as processing")
        void shouldThrowExceptionWhenMarkingNonPendingAsProcessing() {
            testOrder.setStatus(OrderStatus.PROCESSING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.markAsProcessing(1L, "PROV-123"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("Should update order progress")
        void shouldUpdateOrderProgress() {
            testOrder.setStatus(OrderStatus.IN_PROGRESS);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);

            OrderResponse result = orderService.updateProgress(1L, 100, 500);

            assertThat(result).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should complete order")
        void shouldCompleteOrder() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);

            OrderResponse result = orderService.completeOrder(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should cancel order and refund balance")
        void shouldCancelOrderAndRefundBalance() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(Transaction.builder().build());
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);

            OrderResponse result = orderService.cancelOrder(1L);

            assertThat(result).isNotNull();
            verify(transactionRepository).save(any(Transaction.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when cancelling final order")
        void shouldThrowExceptionWhenCancellingFinalOrder() {
            testOrder.setStatus(OrderStatus.COMPLETED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("final state");
        }

        @Test
        @DisplayName("Should refund completed order")
        void shouldRefundCompletedOrder() {
            testOrder.setStatus(OrderStatus.COMPLETED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(Transaction.builder().build());
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);

            OrderResponse result = orderService.refundOrder(1L);

            assertThat(result).isNotNull();
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw exception when refunding non-completed order")
        void shouldThrowExceptionWhenRefundingNonCompletedOrder() {
            testOrder.setStatus(OrderStatus.PENDING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.refundOrder(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("completed");
        }
    }

    @Nested
    @DisplayName("Statistics")
    class Statistics {

        @Test
        @DisplayName("Should count orders by status")
        void shouldCountOrdersByStatus() {
            when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(5L);

            long result = orderService.countByStatus(OrderStatus.PENDING);

            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should count orders by user")
        void shouldCountOrdersByUser() {
            when(orderRepository.countByUserId(1L)).thenReturn(10L);

            long result = orderService.countByUser(1L);

            assertThat(result).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should get total revenue")
        void shouldGetTotalRevenue() {
            when(orderRepository.getTotalRevenue()).thenReturn(new BigDecimal("1000.00"));

            BigDecimal result = orderService.getTotalRevenue();

            assertThat(result).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should return zero when total revenue is null")
        void shouldReturnZeroWhenTotalRevenueNull() {
            when(orderRepository.getTotalRevenue()).thenReturn(null);

            BigDecimal result = orderService.getTotalRevenue();

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should get total cost")
        void shouldGetTotalCost() {
            when(orderRepository.getTotalCost()).thenReturn(new BigDecimal("500.00"));

            BigDecimal result = orderService.getTotalCost();

            assertThat(result).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should get total profit")
        void shouldGetTotalProfit() {
            when(orderRepository.getTotalProfit()).thenReturn(new BigDecimal("500.00"));

            BigDecimal result = orderService.getTotalProfit();

            assertThat(result).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should get revenue between dates")
        void shouldGetRevenueBetweenDates() {
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();
            when(orderRepository.getRevenueBetweenDates(start, end)).thenReturn(new BigDecimal("200.00"));

            BigDecimal result = orderService.getRevenueBetweenDates(start, end);

            assertThat(result).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("Should get average order value")
        void shouldGetAverageOrderValue() {
            when(orderRepository.getAverageOrderValue()).thenReturn(new BigDecimal("25.00"));

            BigDecimal result = orderService.getAverageOrderValue();

            assertThat(result).isEqualByComparingTo(new BigDecimal("25.00"));
        }
    }

    @Nested
    @DisplayName("Summaries")
    class Summaries {

        @Test
        @DisplayName("Should get all order summaries")
        void shouldGetAllOrderSummaries() {
            OrderSummary summary = OrderSummary.builder()
                    .id(1L)
                    .serviceName("Instagram Followers")
                    .status(OrderStatus.PENDING)
                    .build();

            when(orderRepository.findAll()).thenReturn(List.of(testOrder));
            when(orderMapper.toSummaryList(anyList())).thenReturn(List.of(summary));

            List<OrderSummary> result = orderService.getAllSummaries();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getServiceName()).isEqualTo("Instagram Followers");
        }
    }
}
