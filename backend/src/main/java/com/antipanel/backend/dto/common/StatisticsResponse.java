package com.antipanel.backend.dto.common;

import com.antipanel.backend.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Dashboard statistics response containing key business metrics.
 * Used for admin dashboard and analytics views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {

    /**
     * Total number of registered users
     */
    private Long totalUsers;

    /**
     * Number of users who logged in recently
     */
    private Long activeUsers;

    /**
     * Total number of orders placed
     */
    private Long totalOrders;

    /**
     * Number of orders currently pending
     */
    private Long pendingOrders;

    /**
     * Number of successfully completed orders
     */
    private Long completedOrders;

    /**
     * Total revenue from all completed orders
     */
    private BigDecimal totalRevenue;

    /**
     * Total profit (revenue - cost)
     */
    private BigDecimal totalProfit;

    /**
     * Average value per order
     */
    private BigDecimal averageOrderValue;

    /**
     * Order count grouped by status
     */
    private Map<OrderStatus, Long> ordersByStatus;

    /**
     * Order count grouped by category name
     */
    private Map<String, Long> ordersByCategory;
}
