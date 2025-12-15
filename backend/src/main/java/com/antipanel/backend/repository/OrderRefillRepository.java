package com.antipanel.backend.repository;

import com.antipanel.backend.entity.OrderRefill;
import com.antipanel.backend.entity.enums.RefillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OrderRefill entity.
 * Handles database operations for order refill requests and tracking.
 */
@Repository
public interface OrderRefillRepository extends JpaRepository<OrderRefill, Long> {

    // ============ BY ORDER ============

    /**
     * Get all refills for an order sorted by creation date
     *
     * @param orderId Order ID
     * @return List of refills
     */
    List<OrderRefill> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    /**
     * Check if order has pending refill request
     *
     * @param orderId Order ID
     * @return true if order has pending refill
     */
    @Query("SELECT COUNT(r) > 0 FROM OrderRefill r WHERE r.order.id = :orderId " +
           "AND r.status IN ('PENDING', 'PROCESSING')")
    boolean hasPendingRefill(@Param("orderId") Long orderId);

    /**
     * Count refills for order
     *
     * @param orderId Order ID
     * @return Number of refills
     */
    long countByOrderId(Long orderId);

    // ============ BY STATUS ============

    /**
     * Get all refills by status
     *
     * @param status Refill status
     * @return List of refills
     */
    List<OrderRefill> findByStatusOrderByCreatedAtDesc(RefillStatus status);

    /**
     * Get refills by status with pagination
     *
     * @param status   Refill status
     * @param pageable Pagination parameters
     * @return Page of refills
     */
    Page<OrderRefill> findByStatus(RefillStatus status, Pageable pageable);

    /**
     * Get all pending refills needing processing
     *
     * @return List of pending refills
     */
    @Query("SELECT r FROM OrderRefill r WHERE r.status = 'PENDING' " +
           "ORDER BY r.createdAt ASC")
    List<OrderRefill> findPendingRefills();

    // ============ BY USER (THROUGH ORDER) ============

    /**
     * Get refills by user (through order relationship)
     *
     * @param userId User ID
     * @return List of refills
     */
    @Query("SELECT r FROM OrderRefill r WHERE r.order.user.id = :userId " +
           "ORDER BY r.createdAt DESC")
    List<OrderRefill> findByUserId(@Param("userId") Long userId);

    /**
     * Get user refills with pagination
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of refills
     */
    Page<OrderRefill> findByOrderUserId(Long userId, Pageable pageable);

    // ============ EXTERNAL REFERENCE ============

    /**
     * Find refill by provider refill ID
     *
     * @param providerRefillId Provider's refill ID
     * @return Optional refill
     */
    Optional<OrderRefill> findByProviderRefillId(String providerRefillId);

    // ============ TIME-BASED ============

    /**
     * Find refills created within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of refills
     */
    @Query("SELECT r FROM OrderRefill r WHERE r.createdAt BETWEEN :start AND :end " +
           "ORDER BY r.createdAt DESC")
    List<OrderRefill> findRefillsBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ============ STATISTICS ============

    /**
     * Count refills by status
     *
     * @param status Refill status
     * @return Number of refills
     */
    long countByStatus(RefillStatus status);

    /**
     * Get refill counts grouped by status
     * Returns Object[] with [RefillStatus status, Long count]
     *
     * @return List of statuses and refill counts
     */
    @Query("SELECT r.status, COUNT(r) FROM OrderRefill r " +
           "GROUP BY r.status " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> countRefillsByStatus();
}
