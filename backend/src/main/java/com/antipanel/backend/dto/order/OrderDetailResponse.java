package com.antipanel.backend.dto.order;

import com.antipanel.backend.dto.service.ServiceSummary;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed order response with nested service information.
 * Used for full order details page with complete context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    /**
     * Unique order ID
     */
    private Long id;

    /**
     * User who placed the order
     */
    private UserSummary user;

    /**
     * Service details
     */
    private ServiceSummary service;

    /**
     * Target URL or username
     */
    private String target;

    /**
     * Quantity ordered
     */
    private Integer quantity;

    /**
     * Initial count
     */
    private Integer startCount;

    /**
     * Remaining quantity
     */
    private Integer remains;

    /**
     * Order status
     */
    private OrderStatus status;

    /**
     * Delivery progress percentage (0-100)
     */
    private Integer progress;

    /**
     * Price per K at time of order
     */
    private BigDecimal pricePerK;

    /**
     * Total amount charged
     */
    private BigDecimal totalCharge;

    /**
     * Whether refill is available
     */
    private Boolean isRefillable;

    /**
     * Refill guarantee days
     */
    private Integer refillDays;

    /**
     * Refill deadline
     */
    private LocalDateTime refillDeadline;

    /**
     * Can user request refill now
     */
    private Boolean canRequestRefill;

    /**
     * Provider order ID
     */
    private String providerOrderId;

    /**
     * Order creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Order completion timestamp
     */
    private LocalDateTime completedAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}
