package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.transaction.TransactionResponse;
import com.antipanel.backend.dto.transaction.TransactionSummary;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Transaction operations.
 * Handles transaction history, audit records, and statistics.
 * Note: Transactions are read-only audit records created by other services.
 */
public interface TransactionService {

    // ============ CREATE OPERATIONS (Internal use) ============

    /**
     * Create a transaction record.
     * Used internally by OrderService and InvoiceService.
     *
     * @param user          User for the transaction
     * @param type          Transaction type
     * @param amount        Transaction amount
     * @param referenceType Reference entity type (ORDER, INVOICE, etc.)
     * @param referenceId   Reference entity ID
     * @param description   Transaction description
     * @return Created transaction
     */
    Transaction createTransaction(User user, TransactionType type, BigDecimal amount,
                                  String referenceType, Long referenceId, String description);

    // ============ READ OPERATIONS ============

    /**
     * Get transaction by ID.
     *
     * @param id Transaction ID
     * @return Transaction response
     */
    TransactionResponse getById(Long id);

    /**
     * Get latest transaction for user (for balance verification).
     *
     * @param userId User ID
     * @return Latest transaction response or null
     */
    TransactionResponse getLatestByUser(Long userId);

    // ============ USER QUERIES ============

    /**
     * Get user's transaction history.
     *
     * @param userId User ID
     * @return List of transaction responses
     */
    List<TransactionResponse> getByUser(Long userId);

    /**
     * Get user's transactions with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of transaction responses
     */
    PageResponse<TransactionResponse> getByUserPaginated(Long userId, Pageable pageable);

    /**
     * Get user's transactions by type.
     *
     * @param userId User ID
     * @param type   Transaction type
     * @return List of transaction responses
     */
    List<TransactionResponse> getByUserAndType(Long userId, TransactionType type);

    /**
     * Get user's transactions within date range.
     *
     * @param userId User ID
     * @param start  Start timestamp
     * @param end    End timestamp
     * @return List of transaction responses
     */
    List<TransactionResponse> getUserTransactionsBetweenDates(Long userId, LocalDateTime start, LocalDateTime end);

    // ============ ADMIN QUERIES ============

    /**
     * Get all transactions by type.
     *
     * @param type Transaction type
     * @return List of transaction responses
     */
    List<TransactionResponse> getByType(TransactionType type);

    /**
     * Get transactions by type with pagination.
     *
     * @param type     Transaction type
     * @param pageable Pagination parameters
     * @return Page of transaction responses
     */
    PageResponse<TransactionResponse> getByTypePaginated(TransactionType type, Pageable pageable);

    /**
     * Get transactions by reference.
     *
     * @param referenceType Reference entity type
     * @param referenceId   Reference entity ID
     * @return List of transaction responses
     */
    List<TransactionResponse> getByReference(String referenceType, Long referenceId);

    /**
     * Get all transactions within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of transaction responses
     */
    List<TransactionResponse> getTransactionsBetweenDates(LocalDateTime start, LocalDateTime end);

    // ============ AUDIT & VALIDATION ============

    /**
     * Find transactions with inconsistent balance calculations.
     *
     * @return List of inconsistent transaction responses
     */
    List<TransactionResponse> findInconsistentTransactions();

    /**
     * Validate user's current balance against transaction history.
     *
     * @param userId User ID
     * @return true if balance is consistent
     */
    boolean validateUserBalance(Long userId);

    // ============ STATISTICS ============

    /**
     * Count transactions by type.
     *
     * @param type Transaction type
     * @return Number of transactions
     */
    long countByType(TransactionType type);

    /**
     * Get total deposits.
     *
     * @return Sum of all deposit transactions
     */
    BigDecimal getTotalDeposits();

    /**
     * Get total order transactions.
     *
     * @return Sum of all order transaction amounts
     */
    BigDecimal getTotalOrderTransactions();

    /**
     * Get deposits within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return Deposits in period
     */
    BigDecimal getDepositsBetweenDates(LocalDateTime start, LocalDateTime end);

    // ============ SUMMARIES ============

    /**
     * Get all transaction summaries.
     *
     * @return List of transaction summaries
     */
    List<TransactionSummary> getAllSummaries();
}
