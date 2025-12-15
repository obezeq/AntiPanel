package com.antipanel.backend.dto.order;

import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for order with all details.
 * Includes computed fields for progress tracking and refill eligibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /**
     * Unique order ID
     */
    private Long id;

    /**
     * User who placed the order
     */
    private UserSummary user;

    /**
     * ID of the service ordered
     */
    private Integer serviceId;

    /**
     * Snapshot of service name at time of order
     */
    private String serviceName;

    /**
     * Target URL or username
     */
    private String target;

    /**
     * Quantity ordered
     */
    private Integer quantity;

    /**
     * Initial count (if applicable)
     */
    private Integer startCount;

    /**
     * Remaining quantity to be delivered
     */
    private Integer remains;

    /**
     * Current order status
     */
    private OrderStatus status;

    /**
     * Delivery progress percentage (0-100)
     * Computed: (quantity - remains) / quantity * 100
     */
    private Integer progress;

    /**
     * Total amount charged to user
     */
    private BigDecimal totalCharge;

    /**
     * Whether this order is eligible for refill
     */
    private Boolean isRefillable;

    /**
     * Days of refill guarantee
     */
    private Integer refillDays;

    /**
     * Deadline to request refill
     */
    private LocalDateTime refillDeadline;

    /**
     * Whether user can currently request a refill
     * Computed: isRefillable AND status=COMPLETED AND now < refillDeadline
     */
    private Boolean canRequestRefill;

    /**
     * When the order was created
     */
    private LocalDateTime createdAt;

    /**
     * When the order was completed (if applicable)
     */
    private LocalDateTime completedAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}
