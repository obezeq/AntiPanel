package com.antipanel.backend.dto.user;

import java.math.BigDecimal;

/**
 * Response DTO for user dashboard statistics.
 * Contains order counts and current balance for the authenticated user.
 *
 * @param totalOrders     Total number of orders placed by the user (all time)
 * @param pendingOrders   Orders in PENDING or PROCESSING status
 * @param completedOrders Orders in COMPLETED status
 * @param ordersThisMonth Orders created in the current calendar month
 * @param balance         Current account balance
 */
public record UserStatisticsResponse(
    long totalOrders,
    long pendingOrders,
    long completedOrders,
    long ordersThisMonth,
    BigDecimal balance
) {}
