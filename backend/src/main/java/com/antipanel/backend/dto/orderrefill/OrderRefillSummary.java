package com.antipanel.backend.dto.orderrefill;

import com.antipanel.backend.entity.enums.RefillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight order refill summary for list views.
 * Contains essential fields for refill history display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefillSummary {

    /**
     * Unique refill ID
     */
    private Long id;

    /**
     * Original order ID
     */
    private Long orderId;

    /**
     * Quantity being refilled
     */
    private Integer quantity;

    /**
     * Refill status
     */
    private RefillStatus status;

    /**
     * Request timestamp
     */
    private LocalDateTime createdAt;
}
