package com.antipanel.backend.repository;

import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.enums.TransactionType;
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
 * Repository for Transaction entity.
 * Handles database operations for balance movement audit trail.
 * Transactions are read-only records for auditing purposes.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ============ USER QUERIES ============

    /**
     * Get user transaction history sorted by creation date
     *
     * @param userId User ID
     * @return List of transactions
     */
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Get user transactions with pagination
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of transactions
     */
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    /**
     * Get user transactions by type
     *
     * @param userId User ID
     * @param type   Transaction type
     * @return List of transactions
     */
    List<Transaction> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, TransactionType type);

    /**
     * Get user transactions within date range
     *
     * @param userId User ID
     * @param start  Start timestamp
     * @param end    End timestamp
     * @return List of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.createdAt BETWEEN :start AND :end " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findUserTransactionsBetweenDates(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ============ ADMIN QUERIES ============

    /**
     * Get all transactions by type
     *
     * @param type Transaction type
     * @return List of transactions
     */
    List<Transaction> findByTypeOrderByCreatedAtDesc(TransactionType type);

    /**
     * Get transactions by type with pagination
     *
     * @param type     Transaction type
     * @param pageable Pagination parameters
     * @return Page of transactions
     */
    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    /**
     * Find transactions by reference (linked to invoice, order, etc.)
     *
     * @param referenceType Type of reference entity
     * @param referenceId   ID of reference entity
     * @return List of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.referenceType = :referenceType " +
           "AND t.referenceId = :referenceId " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByReference(
            @Param("referenceType") String referenceType,
            @Param("referenceId") Long referenceId);

    /**
     * Find all transactions within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ============ AUDIT & VALIDATION ============

    /**
     * Find transactions with inconsistent balance calculations
     * (balance_after != balance_before + amount)
     *
     * @return List of inconsistent transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.balanceAfter != (t.balanceBefore + t.amount)")
    List<Transaction> findInconsistentTransactions();

    /**
     * Get latest transaction for user (for balance verification)
     * Uses Spring Data derived query method (JPQL doesn't support LIMIT)
     *
     * @param userId User ID
     * @return Optional transaction
     */
    Optional<Transaction> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Get user balance history over time
     * Returns Object[] with [LocalDateTime createdAt, BigDecimal balanceAfter]
     *
     * @param userId User ID
     * @return List of timestamps and balance snapshots
     */
    @Query("SELECT t.createdAt, t.balanceAfter FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "ORDER BY t.createdAt ASC")
    List<Object[]> getUserBalanceHistory(@Param("userId") Long userId);

    // ============ STATISTICS ============

    /**
     * Count transactions by type
     *
     * @param type Transaction type
     * @return Number of transactions
     */
    long countByType(TransactionType type);

    /**
     * Calculate total deposits
     *
     * @return Sum of all deposit transactions
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'DEPOSIT'")
    BigDecimal getTotalDeposits();

    /**
     * Calculate total order transactions (absolute value)
     *
     * @return Sum of all order transaction amounts
     */
    @Query("SELECT SUM(ABS(t.amount)) FROM Transaction t WHERE t.type = 'ORDER'")
    BigDecimal getTotalOrderTransactions();

    /**
     * Calculate deposits within date range
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return Deposits in period
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
           "WHERE t.type = 'DEPOSIT' AND t.createdAt BETWEEN :start AND :end")
    BigDecimal getDepositsBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Get transaction volume grouped by type
     * Returns Object[] with [TransactionType type, Long count, BigDecimal totalAmount]
     *
     * @return List of transaction types, counts, and total amounts
     */
    @Query("SELECT t.type, COUNT(t), SUM(ABS(t.amount)) FROM Transaction t " +
           "GROUP BY t.type " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> getTransactionVolumeByType();

    /**
     * Get daily transaction summary within date range
     * Returns Object[] with [LocalDate date, TransactionType type, Long count, BigDecimal totalAmount]
     * Uses native query because CAST(... AS DATE) is PostgreSQL-specific syntax
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of daily summaries
     */
    @Query(value = "SELECT CAST(t.created_at AS DATE) as tx_date, t.type, COUNT(*), SUM(t.amount) " +
           "FROM transactions t " +
           "WHERE t.created_at BETWEEN :start AND :end " +
           "GROUP BY CAST(t.created_at AS DATE), t.type " +
           "ORDER BY tx_date DESC, t.type",
           nativeQuery = true)
    List<Object[]> getDailyTransactionSummary(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
