package com.antipanel.backend.service;

/**
 * Service responsible for compensating failed orders.
 * Handles refund logic when order submission to providers fails.
 *
 * This service exists to avoid Spring proxy self-invocation issues
 * when REQUIRES_NEW transaction propagation is needed.
 */
public interface OrderCompensationService {

    /**
     * Compensate a failed order by refunding the user's balance
     * and marking the order as FAILED.
     *
     * Uses REQUIRES_NEW propagation to ensure compensation persists
     * even if the calling transaction rolls back.
     *
     * Idempotent: Safe to call multiple times for the same order.
     *
     * @param orderId the order ID to compensate
     */
    void compensateFailedOrder(Long orderId);
}
