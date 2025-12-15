package com.antipanel.backend.dto.orderrefill;

import com.antipanel.backend.entity.enums.RefillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for order refill with all details.
 * Used for refill tracking and admin views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefillResponse {

    /**
     * Unique refill ID
     */
    private Long id;

    /**
     * Original order ID
     */
    private Long orderId;

    /**
     * Refill ID from provider
     */
    private String providerRefillId;

    /**
     * Quantity being refilled
     */
    private Integer quantity;

    /**
     * Refill status
     */
    private RefillStatus status;

    /**
     * When refill was requested
     */
    private LocalDateTime createdAt;

    /**
     * When refill was completed
     */
    private LocalDateTime completedAt;
}
