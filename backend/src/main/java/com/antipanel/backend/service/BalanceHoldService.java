package com.antipanel.backend.service;

import com.antipanel.backend.entity.BalanceHold;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

/**
 * Service interface for balance hold operations.
 * Manages the lifecycle of balance reservations:
 * create hold -> capture (on success) or release (on failure/expiry).
 */
public interface BalanceHoldService {

    /**
     * Create a new balance hold, reserving funds from user's balance.
     * Idempotent - returns existing hold if idempotency key matches.
     *
     * @param userId         User ID
     * @param amount         Amount to reserve
     * @param idempotencyKey Unique request identifier
     * @param holdDuration   How long the hold should be valid
     * @return Created or existing balance hold
     */
    BalanceHold createHold(Long userId, BigDecimal amount, String idempotencyKey, Duration holdDuration);

    /**
     * Capture a hold, finalizing the balance deduction.
     * Creates a transaction record for the order.
     *
     * @param holdId  Hold ID to capture
     * @param orderId Order ID to link to
     */
    void captureHold(Long holdId, Long orderId);

    /**
     * Release a hold, refunding the reserved funds to user's balance.
     *
     * @param holdId Hold ID to release
     * @param reason Reason for releasing (for audit)
     */
    void releaseHold(Long holdId, String reason);

    /**
     * Find a balance hold by its idempotency key.
     *
     * @param idempotencyKey Unique request identifier
     * @return Optional balance hold
     */
    Optional<BalanceHold> findByIdempotencyKey(String idempotencyKey);

    /**
     * Release all expired holds, refunding their amounts.
     * Called by the cleanup scheduler.
     *
     * @return Number of holds released
     */
    int releaseExpiredHolds();
}
