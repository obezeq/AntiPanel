package com.antipanel.backend.repository;

import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Invoice entity.
 * Handles database operations for payment deposits and revenue tracking.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // ============ USER QUERIES ============

    /**
     * Get user invoice history sorted by creation date
     *
     * @param userId User ID
     * @return List of invoices
     */
    List<Invoice> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Get user invoices with pagination
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of invoices
     */
    Page<Invoice> findByUserId(Long userId, Pageable pageable);

    /**
     * Get user invoices by status
     *
     * @param userId User ID
     * @param status Invoice status
     * @return List of invoices
     */
    List<Invoice> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, InvoiceStatus status);

    /**
     * Get pending invoices for user
     *
     * @param userId User ID
     * @return List of pending or processing invoices
     */
    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId " +
           "AND i.status IN ('PENDING', 'PROCESSING') " +
           "ORDER BY i.createdAt DESC")
    List<Invoice> findPendingInvoicesByUser(@Param("userId") Long userId);

    // ============ ADMIN QUERIES ============

    /**
     * Get all invoices by status
     *
     * @param status Invoice status
     * @return List of invoices
     */
    List<Invoice> findByStatusOrderByCreatedAtDesc(InvoiceStatus status);

    /**
     * Get invoices by status with pagination
     *
     * @param status   Invoice status
     * @param pageable Pagination parameters
     * @return Page of invoices
     */
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    /**
     * Get invoices by payment processor
     *
     * @param processorId Payment processor ID
     * @return List of invoices
     */
    List<Invoice> findByProcessorIdOrderByCreatedAtDesc(Integer processorId);

    /**
     * Find invoice by processor invoice ID
     *
     * @param processorInvoiceId External invoice ID
     * @return Optional invoice
     */
    Optional<Invoice> findByProcessorInvoiceId(String processorInvoiceId);

    // ============ LOCKING QUERIES ============

    /**
     * Find invoice by ID with pessimistic write lock.
     * Use this for payment completion to prevent race conditions.
     *
     * @param id Invoice ID
     * @return Optional invoice with database lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);

    // ============ TIME-BASED QUERIES ============

    /**
     * Find invoices created within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of invoices
     */
    @Query("SELECT i FROM Invoice i WHERE i.createdAt BETWEEN :start AND :end " +
           "ORDER BY i.createdAt DESC")
    List<Invoice> findInvoicesBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find invoices paid within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of paid invoices
     */
    @Query("SELECT i FROM Invoice i WHERE i.paidAt BETWEEN :start AND :end " +
           "ORDER BY i.paidAt DESC")
    List<Invoice> findPaidInvoicesBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find expired pending invoices (for auto-cancellation)
     *
     * @param expiryTime Expiry timestamp threshold
     * @return List of expired pending invoices
     */
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PENDING' " +
           "AND i.createdAt < :expiryTime " +
           "ORDER BY i.createdAt ASC")
    List<Invoice> findExpiredPendingInvoices(@Param("expiryTime") LocalDateTime expiryTime);

    // ============ STATISTICS ============

    /**
     * Count invoices by status
     *
     * @param status Invoice status
     * @return Number of invoices
     */
    long countByStatus(InvoiceStatus status);

    /**
     * Calculate total revenue from completed invoices
     *
     * @return Total revenue (sum of net amounts)
     */
    @Query("SELECT SUM(i.netAmount) FROM Invoice i WHERE i.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();

    /**
     * Calculate revenue within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return Revenue in period
     */
    @Query("SELECT SUM(i.netAmount) FROM Invoice i " +
           "WHERE i.status = 'COMPLETED' AND i.paidAt BETWEEN :start AND :end")
    BigDecimal getRevenueBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Calculate total revenue by user
     *
     * @param userId User ID
     * @return Total revenue from user
     */
    @Query("SELECT SUM(i.netAmount) FROM Invoice i " +
           "WHERE i.user.id = :userId AND i.status = 'COMPLETED'")
    BigDecimal getTotalRevenueByUser(@Param("userId") Long userId);

    /**
     * Get revenue grouped by payment processor
     * Returns Object[] with [String processorName, BigDecimal totalRevenue]
     *
     * @return List of processors and revenue
     */
    @Query("SELECT i.processor.name, SUM(i.netAmount) FROM Invoice i " +
           "WHERE i.status = 'COMPLETED' " +
           "GROUP BY i.processor.name " +
           "ORDER BY SUM(i.netAmount) DESC")
    List<Object[]> getRevenueByProcessor();

    /**
     * Calculate average invoice amount
     *
     * @return Average invoice amount
     */
    @Query("SELECT AVG(i.amount) FROM Invoice i WHERE i.status = 'COMPLETED'")
    BigDecimal getAverageInvoiceAmount();
}
