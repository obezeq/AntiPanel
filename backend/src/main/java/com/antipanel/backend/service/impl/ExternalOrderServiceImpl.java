package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.dto.provider.api.*;
import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.entity.ProviderService;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.exception.ProviderApiException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.OrderMapper;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.service.ExternalOrderService;
import com.antipanel.backend.service.OrderService;
import com.antipanel.backend.service.provider.ProviderApiClient;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ExternalOrderService.
 * Handles order submission and status updates with external provider APIs.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class ExternalOrderServiceImpl implements ExternalOrderService {

    private static final int MAX_BATCH_SIZE = 100;

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ProviderApiClient providerApiClient;
    private final OrderMapper orderMapper;

    /**
     * Constructor with @Lazy on OrderService to break circular dependency.
     * OrderService -> ExternalOrderService -> OrderService
     */
    public ExternalOrderServiceImpl(
            OrderRepository orderRepository,
            @Lazy OrderService orderService,
            ProviderApiClient providerApiClient,
            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.providerApiClient = providerApiClient;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderResponse submitOrder(Long orderId) {
        Order order = findOrderById(orderId);
        return submitOrder(order);
    }

    @Override
    @Transactional
    public OrderResponse submitOrder(Order order) {
        log.info("Submitting order {} to provider", order.getId());

        ProviderService providerService = order.getProviderService();
        Provider provider = providerService.getProvider();

        // Build order request
        DripfeedOrderRequest request = DripfeedOrderRequest.builder()
                .serviceId(Integer.parseInt(providerService.getProviderServiceId()))
                .link(order.getTarget())
                .quantity(order.getQuantity())
                .build();

        try {
            // Submit to provider
            DripfeedOrderResponse response = providerApiClient.createOrder(provider, request);

            // Update order with provider order ID
            return orderService.markAsProcessing(order.getId(), response.getOrder().toString());

        } catch (ProviderApiException e) {
            log.error("Failed to submit order {} to provider: {}", order.getId(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId) {
        Order order = findOrderById(orderId);

        if (order.getProviderOrderId() == null) {
            throw new ProviderApiException("Cannot update status: order has no provider order ID");
        }

        Provider provider = order.getProviderService().getProvider();

        try {
            DripfeedStatusResponse statusResponse = providerApiClient.getOrderStatus(
                    provider, order.getProviderOrderId()
            );

            return updateOrderFromStatus(order, statusResponse);

        } catch (ProviderApiException e) {
            log.error("Failed to get status for order {}: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderResponse cancelOrderAtProvider(Long orderId) {
        Order order = findOrderById(orderId);

        if (order.getProviderOrderId() == null) {
            throw new ProviderApiException("Cannot cancel: order has no provider order ID");
        }

        Provider provider = order.getProviderService().getProvider();

        try {
            List<DripfeedCancelResponse> responses = providerApiClient.cancelOrders(
                    provider, List.of(order.getProviderOrderId())
            );

            if (!responses.isEmpty() && responses.getFirst().isSuccess()) {
                return orderService.cancelOrder(orderId);
            } else {
                String error = responses.isEmpty() ? "No response" : responses.getFirst().getError();
                throw new ProviderApiException("DripfeedPanel", "cancel",
                        "Failed to cancel order: " + error);
            }

        } catch (ProviderApiException e) {
            log.error("Failed to cancel order {} at provider: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public String requestRefill(Long orderId) {
        Order order = findOrderById(orderId);

        if (!order.canRequestRefill()) {
            throw new ProviderApiException("Order is not eligible for refill");
        }

        if (order.getProviderOrderId() == null) {
            throw new ProviderApiException("Cannot refill: order has no provider order ID");
        }

        Provider provider = order.getProviderService().getProvider();

        try {
            DripfeedRefillResponse response = providerApiClient.requestRefill(
                    provider, order.getProviderOrderId()
            );

            log.info("Refill requested for order {}. Refill ID: {}",
                    orderId, response.getRefill());

            return response.getRefill();

        } catch (ProviderApiException e) {
            log.error("Failed to request refill for order {}: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public int batchUpdateOrderStatuses(int limit) {
        // Find orders needing update (in progress states, not updated recently)
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<Order> ordersNeedingUpdate = orderRepository.findOrdersNeedingUpdate(threshold);

        if (ordersNeedingUpdate.isEmpty()) {
            log.debug("No orders need status update");
            return 0;
        }

        // Limit the number of orders to process
        List<Order> ordersToProcess = ordersNeedingUpdate.stream()
                .limit(Math.min(limit, MAX_BATCH_SIZE))
                .toList();

        // Group orders by provider
        Map<Provider, List<Order>> ordersByProvider = ordersToProcess.stream()
                .filter(o -> o.getProviderOrderId() != null)
                .collect(Collectors.groupingBy(o -> o.getProviderService().getProvider()));

        int updatedCount = 0;

        for (Map.Entry<Provider, List<Order>> entry : ordersByProvider.entrySet()) {
            Provider provider = entry.getKey();
            List<Order> orders = entry.getValue();

            try {
                updatedCount += updateOrdersBatch(provider, orders);
            } catch (Exception e) {
                log.error("Failed to batch update orders for provider {}: {}",
                        provider.getName(), e.getMessage());
            }
        }

        log.info("Batch status update completed. Updated {} orders", updatedCount);
        return updatedCount;
    }

    /**
     * Updates a batch of orders from a single provider.
     */
    private int updateOrdersBatch(Provider provider, List<Order> orders) {
        List<String> providerOrderIds = orders.stream()
                .map(Order::getProviderOrderId)
                .toList();

        Map<String, DripfeedStatusResponse> statusMap =
                providerApiClient.getMultipleOrderStatus(provider, providerOrderIds);

        int updatedCount = 0;
        for (Order order : orders) {
            DripfeedStatusResponse status = statusMap.get(order.getProviderOrderId());
            if (status != null && !status.hasError()) {
                try {
                    updateOrderFromStatus(order, status);
                    updatedCount++;
                } catch (Exception e) {
                    log.error("Failed to update order {} from status: {}",
                            order.getId(), e.getMessage());
                }
            }
        }

        return updatedCount;
    }

    /**
     * Updates an order entity based on provider status response.
     */
    private OrderResponse updateOrderFromStatus(Order order, DripfeedStatusResponse statusResponse) {
        OrderStatus newStatus = mapProviderStatus(statusResponse.getStatus());
        Integer startCount = statusResponse.getStartCountAsInteger();
        Integer remains = statusResponse.getRemainsAsInteger();

        // Update progress
        orderService.updateProgress(order.getId(), startCount, remains);

        // Update status if changed
        if (order.getStatus() != newStatus) {
            log.info("Order {} status changed: {} -> {}", order.getId(), order.getStatus(), newStatus);

            if (newStatus == OrderStatus.COMPLETED) {
                return orderService.completeOrder(order.getId());
            } else if (newStatus == OrderStatus.CANCELLED) {
                return orderService.cancelOrder(order.getId());
            } else {
                return orderService.updateStatus(order.getId(), newStatus);
            }
        }

        return orderMapper.toResponse(order);
    }

    /**
     * Maps provider status string to OrderStatus enum.
     */
    private OrderStatus mapProviderStatus(String providerStatus) {
        if (providerStatus == null) {
            return OrderStatus.PENDING;
        }

        return switch (providerStatus.toLowerCase()) {
            case "pending" -> OrderStatus.PENDING;
            case "processing" -> OrderStatus.PROCESSING;
            case "in progress" -> OrderStatus.IN_PROGRESS;
            case "completed" -> OrderStatus.COMPLETED;
            case "partial" -> OrderStatus.PARTIAL;
            case "canceled", "cancelled" -> OrderStatus.CANCELLED;
            default -> {
                log.warn("Unknown provider status: {}", providerStatus);
                yield OrderStatus.PROCESSING;
            }
        };
    }

    /**
     * Finds an order by ID or throws ResourceNotFoundException.
     */
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }
}
