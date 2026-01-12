package com.antipanel.backend.service;

import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.dto.provider.api.*;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.entity.ProviderService;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.exception.ProviderApiException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.OrderMapper;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.service.impl.ExternalOrderServiceImpl;
import com.antipanel.backend.service.provider.ProviderApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for ExternalOrderService.
 * Uses Mockito to mock dependencies and verify order submission and status update behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalOrderService Tests")
class ExternalOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private ProviderApiClient providerApiClient;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private ExternalOrderServiceImpl externalOrderService;

    private Provider testProvider;
    private ProviderService testProviderService;
    private Order testOrder;
    private OrderResponse testOrderResponse;
    private UserSummary testUserSummary;

    @BeforeEach
    void setUp() {
        testProvider = Provider.builder()
                .id(1)
                .name("DripfeedPanel")
                .apiUrl("https://dripfeedpanel.com/api/v2")
                .apiKey("test_api_key")
                .isActive(true)
                .balance(new BigDecimal("500.00"))
                .build();

        testProviderService = ProviderService.builder()
                .id(1)
                .provider(testProvider)
                .providerServiceId("13311")
                .name("Instagram Followers")
                .costPerK(new BigDecimal("1.00"))
                .minQuantity(10)
                .maxQuantity(300000)
                .isActive(true)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .providerService(testProviderService)
                .target("https://instagram.com/testuser")
                .quantity(1000)
                .remains(1000)
                .status(OrderStatus.PENDING)
                .pricePerK(new BigDecimal("1.99"))
                .costPerK(new BigDecimal("1.00"))
                .totalCharge(new BigDecimal("1.99"))
                .totalCost(new BigDecimal("1.00"))
                .profit(new BigDecimal("0.99"))
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
                .target("https://instagram.com/testuser")
                .quantity(1000)
                .remains(1000)
                .status(OrderStatus.PENDING)
                .totalCharge(new BigDecimal("1.99"))
                .isRefillable(true)
                .refillDays(30)
                .build();
    }

    @Nested
    @DisplayName("Submit Order")
    class SubmitOrderTests {

        @Test
        @DisplayName("Should submit order successfully")
        void shouldSubmitOrderSuccessfully() {
            DripfeedOrderResponse apiResponse = new DripfeedOrderResponse();
            apiResponse.setOrder(12345L);

            OrderResponse processingResponse = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.PROCESSING)
                    .providerOrderId("12345")
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(providerApiClient.createOrder(eq(testProvider), any(DripfeedOrderRequest.class)))
                    .thenReturn(apiResponse);
            when(orderService.markAsProcessing(1L, "12345")).thenReturn(processingResponse);

            OrderResponse result = externalOrderService.submitOrder(1L);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
            assertThat(result.getProviderOrderId()).isEqualTo("12345");

            ArgumentCaptor<DripfeedOrderRequest> requestCaptor = ArgumentCaptor.forClass(DripfeedOrderRequest.class);
            verify(providerApiClient).createOrder(eq(testProvider), requestCaptor.capture());

            DripfeedOrderRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.getServiceId()).isEqualTo(13311);
            assertThat(capturedRequest.getLink()).isEqualTo("https://instagram.com/testuser");
            assertThat(capturedRequest.getQuantity()).isEqualTo(1000);
        }

        @Test
        @DisplayName("Should submit order entity directly")
        void shouldSubmitOrderEntityDirectly() {
            DripfeedOrderResponse apiResponse = new DripfeedOrderResponse();
            apiResponse.setOrder(12345L);

            OrderResponse processingResponse = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.PROCESSING)
                    .providerOrderId("12345")
                    .build();

            when(providerApiClient.createOrder(eq(testProvider), any(DripfeedOrderRequest.class)))
                    .thenReturn(apiResponse);
            when(orderService.markAsProcessing(1L, "12345")).thenReturn(processingResponse);

            OrderResponse result = externalOrderService.submitOrder(testOrder);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> externalOrderService.submitOrder(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order");
        }

        @Test
        @DisplayName("Should throw exception when provider API fails")
        void shouldThrowExceptionWhenProviderApiFails() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(providerApiClient.createOrder(eq(testProvider), any(DripfeedOrderRequest.class)))
                    .thenThrow(new ProviderApiException("DripfeedPanel", "add", "Not enough funds"));

            assertThatThrownBy(() -> externalOrderService.submitOrder(1L))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Not enough funds");
        }
    }

    @Nested
    @DisplayName("Update Order Status")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update order status from provider")
        void shouldUpdateOrderStatusFromProvider() {
            testOrder.setProviderOrderId("12345");
            testOrder.setStatus(OrderStatus.PROCESSING);

            DripfeedStatusResponse statusResponse = new DripfeedStatusResponse();
            statusResponse.setStatus("In progress");
            statusResponse.setStartCount("500");
            statusResponse.setRemains("250");
            statusResponse.setCharge("1.99");

            OrderResponse updatedResponse = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.IN_PROGRESS)
                    .startCount(500)
                    .remains(250)
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(providerApiClient.getOrderStatus(testProvider, "12345")).thenReturn(statusResponse);
            when(orderService.updateProgress(eq(1L), eq(500), eq(250))).thenReturn(updatedResponse);
            when(orderService.updateStatus(eq(1L), eq(OrderStatus.IN_PROGRESS))).thenReturn(updatedResponse);

            OrderResponse result = externalOrderService.updateOrderStatus(1L);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
            verify(orderService).updateProgress(1L, 500, 250);
        }

        @Test
        @DisplayName("Should complete order when status is completed")
        void shouldCompleteOrderWhenStatusCompleted() {
            testOrder.setProviderOrderId("12345");
            testOrder.setStatus(OrderStatus.IN_PROGRESS);

            DripfeedStatusResponse statusResponse = new DripfeedStatusResponse();
            statusResponse.setStatus("Completed");
            statusResponse.setStartCount("500");
            statusResponse.setRemains("0");

            OrderResponse completedResponse = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.COMPLETED)
                    .remains(0)
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(providerApiClient.getOrderStatus(testProvider, "12345")).thenReturn(statusResponse);
            when(orderService.updateProgress(eq(1L), anyInt(), anyInt())).thenReturn(testOrderResponse);
            when(orderService.completeOrder(1L)).thenReturn(completedResponse);

            OrderResponse result = externalOrderService.updateOrderStatus(1L);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
            verify(orderService).completeOrder(1L);
        }

        @Test
        @DisplayName("Should cancel order when status is cancelled")
        void shouldCancelOrderWhenStatusCancelled() {
            testOrder.setProviderOrderId("12345");
            testOrder.setStatus(OrderStatus.PROCESSING);

            DripfeedStatusResponse statusResponse = new DripfeedStatusResponse();
            statusResponse.setStatus("Canceled");
            statusResponse.setStartCount("0");
            statusResponse.setRemains("1000");

            OrderResponse cancelledResponse = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.CANCELLED)
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(providerApiClient.getOrderStatus(testProvider, "12345")).thenReturn(statusResponse);
            when(orderService.updateProgress(eq(1L), anyInt(), anyInt())).thenReturn(testOrderResponse);
            when(orderService.cancelOrder(1L)).thenReturn(cancelledResponse);

            OrderResponse result = externalOrderService.updateOrderStatus(1L);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderService).cancelOrder(1L);
        }

        @Test
        @DisplayName("Should throw exception when order has no provider order ID")
        void shouldThrowExceptionWhenNoProviderOrderId() {
            testOrder.setProviderOrderId(null);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> externalOrderService.updateOrderStatus(1L))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("no provider order ID");
        }
    }

    @Nested
    @DisplayName("Cancel Order At Provider")
    class CancelOrderAtProviderTests {

        @Test
        @DisplayName("Should cancel order at provider successfully")
        void shouldCancelOrderAtProviderSuccessfully() {
            testOrder.setProviderOrderId("12345");
            testOrder.setStatus(OrderStatus.PROCESSING);

            DripfeedCancelResponse cancelResponse = new DripfeedCancelResponse();
            cancelResponse.setOrder(12345L);
            cancelResponse.setCancel(Map.of("status", "success"));

            OrderResponse cancelledResponse = OrderResponse.builder()
                    .id(1L)
                    .status(OrderStatus.CANCELLED)
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(providerApiClient.cancelOrders(testProvider, List.of("12345")))
                    .thenReturn(List.of(cancelResponse));
            when(orderService.cancelOrder(1L)).thenReturn(cancelledResponse);

            OrderResponse result = externalOrderService.cancelOrderAtProvider(1L);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderService).cancelOrder(1L);
        }

        @Test
        @DisplayName("Should throw exception when cancel fails at provider")
        void shouldThrowExceptionWhenCancelFails() {
            testOrder.setProviderOrderId("12345");

            DripfeedCancelResponse cancelResponse = new DripfeedCancelResponse();
            cancelResponse.setOrder(12345L);
            cancelResponse.setCancel(Map.of("error", "Order already completed"));

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(providerApiClient.cancelOrders(testProvider, List.of("12345")))
                    .thenReturn(List.of(cancelResponse));

            assertThatThrownBy(() -> externalOrderService.cancelOrderAtProvider(1L))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Failed to cancel");
        }

        @Test
        @DisplayName("Should throw exception when order has no provider order ID for cancel")
        void shouldThrowExceptionWhenNoProviderOrderIdForCancel() {
            testOrder.setProviderOrderId(null);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> externalOrderService.cancelOrderAtProvider(1L))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("no provider order ID");
        }
    }

    @Nested
    @DisplayName("Request Refill")
    class RequestRefillTests {

        @Test
        @DisplayName("Should request refill successfully")
        void shouldRequestRefillSuccessfully() {
            testOrder.setProviderOrderId("12345");
            testOrder.setStatus(OrderStatus.COMPLETED);
            testOrder.setIsRefillable(true);
            testOrder.setRefillDays(30);
            testOrder.setRefillDeadline(LocalDateTime.now().plusDays(15));

            DripfeedRefillResponse refillResponse = new DripfeedRefillResponse();
            refillResponse.setRefill("67890");

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(providerApiClient.requestRefill(testProvider, "12345")).thenReturn(refillResponse);

            String result = externalOrderService.requestRefill(1L);

            assertThat(result).isEqualTo("67890");
            verify(providerApiClient).requestRefill(testProvider, "12345");
        }

        @Test
        @DisplayName("Should throw exception when order not eligible for refill")
        void shouldThrowExceptionWhenNotEligibleForRefill() {
            testOrder.setIsRefillable(false);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> externalOrderService.requestRefill(1L))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("not eligible for refill");
        }

        @Test
        @DisplayName("Should throw exception when order has no provider order ID for refill")
        void shouldThrowExceptionWhenNoProviderOrderIdForRefill() {
            testOrder.setProviderOrderId(null);
            testOrder.setStatus(OrderStatus.COMPLETED);
            testOrder.setIsRefillable(true);
            testOrder.setRefillDays(30);
            testOrder.setRefillDeadline(LocalDateTime.now().plusDays(15));
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> externalOrderService.requestRefill(1L))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("no provider order ID");
        }
    }

    @Nested
    @DisplayName("Batch Update Order Statuses")
    class BatchUpdateOrderStatusesTests {

        @Test
        @DisplayName("Should batch update order statuses successfully")
        void shouldBatchUpdateOrderStatusesSuccessfully() {
            Order order1 = createOrderWithProvider("12345", OrderStatus.PROCESSING);
            Order order2 = createOrderWithProvider("12346", OrderStatus.IN_PROGRESS);

            DripfeedStatusResponse status1 = createStatusResponse("Completed", "500", "0");
            DripfeedStatusResponse status2 = createStatusResponse("In progress", "1000", "500");

            Map<String, DripfeedStatusResponse> statusMap = Map.of(
                    "12345", status1,
                    "12346", status2
            );

            when(orderRepository.findOrdersNeedingUpdate(any(LocalDateTime.class)))
                    .thenReturn(List.of(order1, order2));
            when(providerApiClient.getMultipleOrderStatus(eq(testProvider), anyList()))
                    .thenReturn(statusMap);
            when(orderService.updateProgress(anyLong(), anyInt(), anyInt())).thenReturn(testOrderResponse);
            when(orderService.completeOrder(anyLong())).thenReturn(testOrderResponse);
            when(orderService.updateStatus(anyLong(), any())).thenReturn(testOrderResponse);

            int result = externalOrderService.batchUpdateOrderStatuses(100);

            assertThat(result).isEqualTo(2);
            verify(providerApiClient).getMultipleOrderStatus(eq(testProvider), anyList());
        }

        @Test
        @DisplayName("Should return zero when no orders need update")
        void shouldReturnZeroWhenNoOrdersNeedUpdate() {
            when(orderRepository.findOrdersNeedingUpdate(any(LocalDateTime.class)))
                    .thenReturn(List.of());

            int result = externalOrderService.batchUpdateOrderStatuses(100);

            assertThat(result).isZero();
            verify(providerApiClient, never()).getMultipleOrderStatus(any(), anyList());
        }

        @Test
        @DisplayName("Should skip orders without provider order ID")
        void shouldSkipOrdersWithoutProviderOrderId() {
            Order orderWithoutProviderId = Order.builder()
                    .id(2L)
                    .providerService(testProviderService)
                    .providerOrderId(null)
                    .status(OrderStatus.PENDING)
                    .build();

            Order orderWithProviderId = createOrderWithProvider("12345", OrderStatus.PROCESSING);

            DripfeedStatusResponse status = createStatusResponse("Completed", "500", "0");
            Map<String, DripfeedStatusResponse> statusMap = Map.of("12345", status);

            when(orderRepository.findOrdersNeedingUpdate(any(LocalDateTime.class)))
                    .thenReturn(List.of(orderWithoutProviderId, orderWithProviderId));
            when(providerApiClient.getMultipleOrderStatus(eq(testProvider), eq(List.of("12345"))))
                    .thenReturn(statusMap);
            when(orderService.updateProgress(anyLong(), anyInt(), anyInt())).thenReturn(testOrderResponse);
            when(orderService.completeOrder(anyLong())).thenReturn(testOrderResponse);

            int result = externalOrderService.batchUpdateOrderStatuses(100);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle provider API errors gracefully in batch")
        void shouldHandleProviderApiErrorsGracefullyInBatch() {
            Order order1 = createOrderWithProvider("12345", OrderStatus.PROCESSING);

            when(orderRepository.findOrdersNeedingUpdate(any(LocalDateTime.class)))
                    .thenReturn(List.of(order1));
            when(providerApiClient.getMultipleOrderStatus(eq(testProvider), anyList()))
                    .thenThrow(new ProviderApiException("DripfeedPanel", "status", "API Error"));

            int result = externalOrderService.batchUpdateOrderStatuses(100);

            assertThat(result).isZero();
        }
    }

    /**
     * Helper method to create an order with a provider order ID.
     */
    private Order createOrderWithProvider(String providerOrderId, OrderStatus status) {
        return Order.builder()
                .id(Long.parseLong(providerOrderId.substring(providerOrderId.length() - 2)))
                .providerService(testProviderService)
                .providerOrderId(providerOrderId)
                .target("https://instagram.com/testuser")
                .quantity(1000)
                .status(status)
                .build();
    }

    /**
     * Helper method to create a status response.
     */
    private DripfeedStatusResponse createStatusResponse(String status, String startCount, String remains) {
        DripfeedStatusResponse response = new DripfeedStatusResponse();
        response.setStatus(status);
        response.setStartCount(startCount);
        response.setRemains(remains);
        response.setCharge("1.99");
        response.setCurrency("USD");
        return response;
    }
}
