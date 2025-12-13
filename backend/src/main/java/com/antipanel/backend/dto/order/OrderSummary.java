package com.antipanel.backend.dto.order;

import com.antipanel.backend.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight order summary for list views.
 * Contains essential fields for order history display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummary {

    /**
     * Unique order ID
     */
    private Long id;

    /**
     * Service name snapshot
     */
    private String serviceName;

    /**
     * Quantity ordered
     */
    private Integer quantity;

    /**
     * Remaining quantity
     */
    private Integer remains;

    /**
     * Order status
     */
    private OrderStatus status;

    /**
     * Total amount charged
     */
    private BigDecimal totalCharge;

    /**
     * Order creation timestamp
     */
    private LocalDateTime createdAt;
}
