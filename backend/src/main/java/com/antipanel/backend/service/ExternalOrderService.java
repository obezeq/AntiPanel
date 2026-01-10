package com.antipanel.backend.service;

import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.entity.Order;

/**
 * Service interface for managing orders with external providers.
 * Handles order submission, status updates, and cancellation via provider APIs.
 */
public interface ExternalOrderService {

    /**
     * Submits an order to the external provider.
     * Updates the order with the provider order ID on success.
     *
     * @param orderId the local order ID to submit
     * @return updated order response with provider order ID
     */
    OrderResponse submitOrder(Long orderId);

    /**
     * Submits an order entity to the external provider.
     * Used internally after order creation.
     *
     * @param order the order entity to submit
     * @return updated order response with provider order ID
     */
    OrderResponse submitOrder(Order order);

    /**
     * Updates order status from the provider.
     *
     * @param orderId the local order ID to update
     * @return updated order response
     */
    OrderResponse updateOrderStatus(Long orderId);

    /**
     * Requests cancellation of an order from the provider.
     *
     * @param orderId the local order ID to cancel
     * @return updated order response
     */
    OrderResponse cancelOrderAtProvider(Long orderId);

    /**
     * Requests a refill for an order from the provider.
     *
     * @param orderId the local order ID to refill
     * @return refill ID from the provider
     */
    String requestRefill(Long orderId);

    /**
     * Updates status of multiple orders in batch.
     * More efficient than individual status checks.
     *
     * @param limit maximum number of orders to update
     * @return number of orders updated
     */
    int batchUpdateOrderStatuses(int limit);
}
