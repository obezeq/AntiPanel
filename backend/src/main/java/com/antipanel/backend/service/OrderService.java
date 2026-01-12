package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.order.OrderCreateRequest;
import com.antipanel.backend.dto.order.OrderDetailResponse;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.dto.order.OrderSummary;
import com.antipanel.backend.entity.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Order operations.
 * Core business service handling order creation, status management, and statistics.
 */
public interface OrderService {

    // ============ CREATE OPERATIONS ============

    /**
     * Create a new order for a user.
     * Validates service availability, quantity limits, and user balance.
     * This method only creates the order and deducts balance - it does NOT submit to provider.
     * Call {@link #submitOrderToProvider(Long)} after this method to submit to external provider.
     *
     * @param userId  User ID
     * @param request Order creation data
     * @return Created order response
     */
    OrderResponse create(Long userId, OrderCreateRequest request);

    /**
     * Submit an order to the external provider.
     * This should be called AFTER create() returns, outside the main transaction.
     * Uses REQUIRES_NEW propagation to ensure proper transaction boundaries.
     *
     * @param orderId Order ID to submit
     * @return Updated order response from provider
     */
    OrderResponse submitOrderToProvider(Long orderId);

    /**
     * Compensate a failed order by refunding the user.
     * Uses REQUIRES_NEW propagation to ensure the refund is not rolled back.
     *
     * @param orderId Order ID to compensate
     */
    void compensateFailedOrder(Long orderId);

    // ============ READ OPERATIONS ============

    /**
     * Get order by ID.
     *
     * @param id Order ID
     * @return Order response
     */
    OrderResponse getById(Long id);

    /**
     * Get order detail by ID (includes nested DTOs).
     *
     * @param id Order ID
     * @return Order detail response
     */
    OrderDetailResponse getDetailById(Long id);

    /**
     * Get order by provider order ID.
     *
     * @param providerOrderId Provider's order ID
     * @return Order response
     */
    OrderResponse getByProviderOrderId(String providerOrderId);

    // ============ USER ORDER QUERIES ============

    /**
     * Get user's order history.
     *
     * @param userId User ID
     * @return List of order responses
     */
    List<OrderResponse> getByUser(Long userId);

    /**
     * Get user's orders with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of order responses
     */
    PageResponse<OrderResponse> getByUserPaginated(Long userId, Pageable pageable);

    /**
     * Get user's orders by status.
     *
     * @param userId User ID
     * @param status Order status
     * @return List of order responses
     */
    List<OrderResponse> getByUserAndStatus(Long userId, OrderStatus status);

    /**
     * Get user's active orders (not completed/cancelled/refunded).
     *
     * @param userId User ID
     * @return List of active order responses
     */
    List<OrderResponse> getActiveByUser(Long userId);

    /**
     * Get user's refillable orders.
     *
     * @param userId User ID
     * @return List of refillable order responses
     */
    List<OrderResponse> getRefillableByUser(Long userId);

    // ============ ADMIN ORDER QUERIES ============

    /**
     * Get all orders by status.
     *
     * @param status Order status
     * @return List of order responses
     */
    List<OrderResponse> getByStatus(OrderStatus status);

    /**
     * Get orders by status with pagination.
     *
     * @param status   Order status
     * @param pageable Pagination parameters
     * @return Page of order responses
     */
    PageResponse<OrderResponse> getByStatusPaginated(OrderStatus status, Pageable pageable);

    /**
     * Get orders by service.
     *
     * @param serviceId Service ID
     * @return List of order responses
     */
    List<OrderResponse> getByService(Integer serviceId);

    /**
     * Get orders by provider service.
     *
     * @param providerServiceId Provider service ID
     * @return List of order responses
     */
    List<OrderResponse> getByProviderService(Integer providerServiceId);

    /**
     * Get orders needing provider update.
     *
     * @param threshold Timestamp threshold
     * @return List of order responses
     */
    List<OrderResponse> getOrdersNeedingUpdate(LocalDateTime threshold);

    // ============ TIME-BASED QUERIES ============

    /**
     * Get orders created within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of order responses
     */
    List<OrderResponse> getOrdersBetweenDates(LocalDateTime start, LocalDateTime end);

    /**
     * Get completed orders within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of order responses
     */
    List<OrderResponse> getCompletedOrdersBetweenDates(LocalDateTime start, LocalDateTime end);

    // ============ REFILL MANAGEMENT ============

    /**
     * Get orders with refill expiring soon.
     *
     * @param deadline Deadline threshold
     * @return List of order responses
     */
    List<OrderResponse> getOrdersWithRefillExpiringSoon(LocalDateTime deadline);

    /**
     * Get orders with expired refill guarantee.
     *
     * @return List of order responses
     */
    List<OrderResponse> getOrdersWithExpiredRefill();

    // ============ STATUS OPERATIONS ============

    /**
     * Update order status.
     *
     * @param id     Order ID
     * @param status New status
     * @return Updated order response
     */
    OrderResponse updateStatus(Long id, OrderStatus status);

    /**
     * Mark order as processing.
     *
     * @param id              Order ID
     * @param providerOrderId Provider's order ID
     * @return Updated order response
     */
    OrderResponse markAsProcessing(Long id, String providerOrderId);

    /**
     * Update order progress.
     *
     * @param id         Order ID
     * @param startCount Start count
     * @param remains    Remaining quantity
     * @return Updated order response
     */
    OrderResponse updateProgress(Long id, Integer startCount, Integer remains);

    /**
     * Complete order.
     *
     * @param id Order ID
     * @return Updated order response
     */
    OrderResponse completeOrder(Long id);

    /**
     * Cancel order.
     *
     * @param id Order ID
     * @return Updated order response
     */
    OrderResponse cancelOrder(Long id);

    /**
     * Refund order.
     *
     * @param id Order ID
     * @return Updated order response
     */
    OrderResponse refundOrder(Long id);

    // ============ STATISTICS ============

    /**
     * Count orders by status.
     *
     * @param status Order status
     * @return Number of orders
     */
    long countByStatus(OrderStatus status);

    /**
     * Count total orders by user.
     *
     * @param userId User ID
     * @return Number of orders
     */
    long countByUser(Long userId);

    /**
     * Count orders by user and status.
     *
     * @param userId User ID
     * @param status Order status
     * @return Number of orders
     */
    long countByUserAndStatus(Long userId, OrderStatus status);

    /**
     * Get total revenue.
     *
     * @return Total revenue
     */
    BigDecimal getTotalRevenue();

    /**
     * Get total cost.
     *
     * @return Total cost
     */
    BigDecimal getTotalCost();

    /**
     * Get total profit.
     *
     * @return Total profit
     */
    BigDecimal getTotalProfit();

    /**
     * Get revenue within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return Revenue in period
     */
    BigDecimal getRevenueBetweenDates(LocalDateTime start, LocalDateTime end);

    /**
     * Get profit within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return Profit in period
     */
    BigDecimal getProfitBetweenDates(LocalDateTime start, LocalDateTime end);

    /**
     * Get average order value.
     *
     * @return Average order value
     */
    BigDecimal getAverageOrderValue();

    // ============ SUMMARIES ============

    /**
     * Get all order summaries.
     *
     * @return List of order summaries
     */
    List<OrderSummary> getAllSummaries();
}
