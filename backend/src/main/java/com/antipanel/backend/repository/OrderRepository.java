package com.antipanel.backend.repository;

import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity.
 * BUSINESS CRITICAL - Core business transactions and analytics.
 * Contains comprehensive queries for order management, refills, and statistics.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ============ USER QUERIES ============

    /**
     * Get user order history sorted by creation date
     *
     * @param userId User ID
     * @return List of orders
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Get user orders with pagination
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of orders
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * Get user orders by status
     *
     * @param userId User ID
     * @param status Order status
     * @return List of orders
     */
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

    /**
     * Get user orders by status with pagination
     *
     * @param userId   User ID
     * @param status   Order status
     * @param pageable Pagination parameters
     * @return Page of orders
     */
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    /**
     * Get user active orders (not in final state)
     *
     * @param userId User ID
     * @return List of active orders
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId " +
           "AND o.status NOT IN ('COMPLETED', 'CANCELLED', 'REFUNDED') " +
           "ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByUser(@Param("userId") Long userId);

    /**
     * Get user refillable orders (completed orders within refill deadline)
     *
     * @param userId User ID
     * @param now    Current timestamp
     * @return List of refillable orders
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId " +
           "AND o.isRefillable = true " +
           "AND o.status = 'COMPLETED' " +
           "AND o.refillDeadline > :now " +
           "ORDER BY o.refillDeadline ASC")
    List<Order> findRefillableOrdersByUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    // ============ ADMIN QUERIES ============

    /**
     * Get all orders by status
     *
     * @param status Order status
     * @return List of orders
     */
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    /**
     * Get orders by status with pagination
     *
     * @param status   Order status
     * @param pageable Pagination parameters
     * @return Page of orders
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Get orders by service
     *
     * @param serviceId Service ID
     * @return List of orders
     */
    List<Order> findByServiceIdOrderByCreatedAtDesc(Integer serviceId);

    /**
     * Get orders by provider service
     *
     * @param providerServiceId Provider service ID
     * @return List of orders
     */
    List<Order> findByProviderServiceIdOrderByCreatedAtDesc(Integer providerServiceId);

    /**
     * Find order by provider order ID
     *
     * @param providerOrderId Provider's order ID
     * @return Optional order
     */
    Optional<Order> findByProviderOrderId(String providerOrderId);

    /**
     * Find orders needing provider update (stale orders in progress)
     *
     * @param threshold Timestamp threshold for last update
     * @return List of orders needing sync
     */
    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'PROCESSING', 'IN_PROGRESS') " +
           "AND o.updatedAt < :threshold " +
           "ORDER BY o.updatedAt ASC")
    List<Order> findOrdersNeedingUpdate(@Param("threshold") LocalDateTime threshold);

    // ============ REFILL MANAGEMENT ============

    /**
     * Find orders with refill expiring soon
     *
     * @param now      Current timestamp
     * @param deadline Future timestamp threshold
     * @return List of orders with refill expiring soon
     */
    @Query("SELECT o FROM Order o WHERE o.isRefillable = true " +
           "AND o.status = 'COMPLETED' " +
           "AND o.refillDeadline BETWEEN :now AND :deadline " +
           "ORDER BY o.refillDeadline ASC")
    List<Order> findOrdersWithRefillExpiringSoon(
            @Param("now") LocalDateTime now,
            @Param("deadline") LocalDateTime deadline);

    /**
     * Find orders with expired refill guarantee
     *
     * @param now Current timestamp
     * @return List of orders with expired refill
     */
    @Query("SELECT o FROM Order o WHERE o.isRefillable = true " +
           "AND o.status = 'COMPLETED' " +
           "AND o.refillDeadline < :now")
    List<Order> findOrdersWithExpiredRefill(@Param("now") LocalDateTime now);

    // ============ TIME-BASED QUERIES ============

    /**
     * Find orders created within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of orders
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end " +
           "ORDER BY o.createdAt DESC")
    List<Order> findOrdersBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find orders completed within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of completed orders
     */
    @Query("SELECT o FROM Order o WHERE o.completedAt BETWEEN :start AND :end " +
           "ORDER BY o.completedAt DESC")
    List<Order> findCompletedOrdersBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ============ STATISTICS ============

    /**
     * Count orders by status
     *
     * @param status Order status
     * @return Number of orders
     */
    long countByStatus(OrderStatus status);

    /**
     * Count total orders by user
     *
     * @param userId User ID
     * @return Number of orders
     */
    long countByUserId(Long userId);

    /**
     * Count orders by user and status
     *
     * @param userId User ID
     * @param status Order status
     * @return Number of orders
     */
    long countByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Count orders by user created after a specific date.
     * Used for "orders this month" statistics.
     *
     * @param userId    User ID
     * @param startDate Start date (inclusive)
     * @return Number of orders
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.createdAt >= :startDate")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    /**
     * Calculate total revenue (sum of all charges on completed orders)
     *
     * @return Total revenue
     */
    @Query("SELECT SUM(o.totalCharge) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();

    /**
     * Calculate total cost (sum of all costs on completed orders)
     *
     * @return Total cost
     */
    @Query("SELECT SUM(o.totalCost) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal getTotalCost();

    /**
     * Calculate total profit (sum of all profits on completed orders)
     *
     * @return Total profit
     */
    @Query("SELECT SUM(o.profit) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal getTotalProfit();

    /**
     * Calculate revenue within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return Revenue in period
     */
    @Query("SELECT SUM(o.totalCharge) FROM Order o " +
           "WHERE o.status = 'COMPLETED' AND o.completedAt BETWEEN :start AND :end")
    BigDecimal getRevenueBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Calculate profit within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return Profit in period
     */
    @Query("SELECT SUM(o.profit) FROM Order o " +
           "WHERE o.status = 'COMPLETED' AND o.completedAt BETWEEN :start AND :end")
    BigDecimal getProfitBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Calculate average order value
     *
     * @return Average order value
     */
    @Query("SELECT AVG(o.totalCharge) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal getAverageOrderValue();

    /**
     * Count orders grouped by category
     * Returns Object[] with [String categoryName, Long count]
     *
     * @return List of category names and order counts
     */
    @Query("SELECT s.category.name, COUNT(o) FROM Order o " +
           "JOIN o.service s " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY s.category.name " +
           "ORDER BY COUNT(o) DESC")
    List<Object[]> countOrdersByCategory();

    /**
     * Count orders grouped by status
     * Returns Object[] with [OrderStatus status, Long count]
     *
     * @return List of statuses and order counts
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o " +
           "GROUP BY o.status " +
           "ORDER BY COUNT(o) DESC")
    List<Object[]> countOrdersByStatus();

    /**
     * Find top services by order count
     * Returns Object[] with [Integer serviceId, String serviceName, Long count]
     *
     * @param pageable Pagination parameters
     * @return Page of top services
     */
    @Query("SELECT o.service.id, o.serviceName, COUNT(o) FROM Order o " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY o.service.id, o.serviceName " +
           "ORDER BY COUNT(o) DESC")
    Page<Object[]> findTopServicesByOrderCount(Pageable pageable);

    /**
     * Find top services by revenue
     * Returns Object[] with [Integer serviceId, String serviceName, BigDecimal totalRevenue]
     *
     * @param pageable Pagination parameters
     * @return Page of top services by revenue
     */
    @Query("SELECT o.service.id, o.serviceName, SUM(o.totalCharge) FROM Order o " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY o.service.id, o.serviceName " +
           "ORDER BY SUM(o.totalCharge) DESC")
    Page<Object[]> findTopServicesByRevenue(Pageable pageable);

    /**
     * Find top users by spending
     * Returns Object[] with [Long userId, String email, Long orderCount, BigDecimal totalSpent]
     *
     * @param pageable Pagination parameters
     * @return Page of top users
     */
    @Query("SELECT o.user.id, o.user.email, COUNT(o), SUM(o.totalCharge) FROM Order o " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY o.user.id, o.user.email " +
           "ORDER BY SUM(o.totalCharge) DESC")
    Page<Object[]> findTopUsersBySpending(Pageable pageable);
}
