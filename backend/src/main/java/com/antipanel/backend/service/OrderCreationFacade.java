package com.antipanel.backend.service;

import com.antipanel.backend.dto.order.OrderCreateRequest;
import com.antipanel.backend.dto.order.OrderResponse;

/**
 * Facade interface for order creation.
 * Orchestrates the balance hold pattern:
 * 1. Reserve balance (create hold)
 * 2. Create pending order
 * 3. Submit to provider
 * 4. Capture hold on success, release on failure
 */
public interface OrderCreationFacade {

    /**
     * Create and submit an order using the balance reservation pattern.
     * This method handles the entire order lifecycle:
     * - Validates service and quantity
     * - Creates a balance hold (reserves funds)
     * - Creates the pending order
     * - Submits to external provider
     * - Captures hold on success, releases on failure
     *
     * @param userId  User ID
     * @param request Order creation request
     * @return Created order response
     */
    OrderResponse createOrder(Long userId, OrderCreateRequest request);
}
