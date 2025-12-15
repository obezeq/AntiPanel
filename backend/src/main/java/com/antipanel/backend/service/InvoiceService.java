package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.invoice.InvoiceCreateRequest;
import com.antipanel.backend.dto.invoice.InvoiceResponse;
import com.antipanel.backend.dto.invoice.InvoiceSummary;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Invoice operations.
 * Handles deposit invoice creation, payment processing, and statistics.
 */
public interface InvoiceService {

    // ============ CREATE OPERATIONS ============

    /**
     * Create a new deposit invoice for a user.
     *
     * @param userId  User ID
     * @param request Invoice creation data
     * @return Created invoice response
     */
    InvoiceResponse create(Long userId, InvoiceCreateRequest request);

    // ============ READ OPERATIONS ============

    /**
     * Get invoice by ID.
     *
     * @param id Invoice ID
     * @return Invoice response
     */
    InvoiceResponse getById(Long id);

    /**
     * Get invoice by processor invoice ID.
     *
     * @param processorInvoiceId External invoice ID
     * @return Invoice response
     */
    InvoiceResponse getByProcessorInvoiceId(String processorInvoiceId);

    // ============ USER QUERIES ============

    /**
     * Get user's invoice history.
     *
     * @param userId User ID
     * @return List of invoice responses
     */
    List<InvoiceResponse> getByUser(Long userId);

    /**
     * Get user's invoices with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of invoice responses
     */
    PageResponse<InvoiceResponse> getByUserPaginated(Long userId, Pageable pageable);

    /**
     * Get user's invoices by status.
     *
     * @param userId User ID
     * @param status Invoice status
     * @return List of invoice responses
     */
    List<InvoiceResponse> getByUserAndStatus(Long userId, InvoiceStatus status);

    /**
     * Get user's pending invoices.
     *
     * @param userId User ID
     * @return List of pending invoice responses
     */
    List<InvoiceResponse> getPendingByUser(Long userId);

    // ============ ADMIN QUERIES ============

    /**
     * Get all invoices by status.
     *
     * @param status Invoice status
     * @return List of invoice responses
     */
    List<InvoiceResponse> getByStatus(InvoiceStatus status);

    /**
     * Get invoices by status with pagination.
     *
     * @param status   Invoice status
     * @param pageable Pagination parameters
     * @return Page of invoice responses
     */
    PageResponse<InvoiceResponse> getByStatusPaginated(InvoiceStatus status, Pageable pageable);

    /**
     * Get invoices by payment processor.
     *
     * @param processorId Payment processor ID
     * @return List of invoice responses
     */
    List<InvoiceResponse> getByProcessor(Integer processorId);

    // ============ TIME-BASED QUERIES ============

    /**
     * Get invoices created within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of invoice responses
     */
    List<InvoiceResponse> getInvoicesBetweenDates(LocalDateTime start, LocalDateTime end);

    /**
     * Get invoices paid within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of invoice responses
     */
    List<InvoiceResponse> getPaidInvoicesBetweenDates(LocalDateTime start, LocalDateTime end);

    /**
     * Get expired pending invoices for auto-cancellation.
     *
     * @param expiryTime Expiry threshold
     * @return List of expired invoice responses
     */
    List<InvoiceResponse> getExpiredPendingInvoices(LocalDateTime expiryTime);

    // ============ STATUS OPERATIONS ============

    /**
     * Update invoice status.
     *
     * @param id     Invoice ID
     * @param status New status
     * @return Updated invoice response
     */
    InvoiceResponse updateStatus(Long id, InvoiceStatus status);

    /**
     * Mark invoice as processing.
     *
     * @param id                 Invoice ID
     * @param processorInvoiceId External invoice ID
     * @param paymentUrl         Payment URL
     * @return Updated invoice response
     */
    InvoiceResponse markAsProcessing(Long id, String processorInvoiceId, String paymentUrl);

    /**
     * Complete invoice payment (adds balance to user).
     *
     * @param id Invoice ID
     * @return Updated invoice response
     */
    InvoiceResponse completePayment(Long id);

    /**
     * Cancel invoice.
     *
     * @param id Invoice ID
     * @return Updated invoice response
     */
    InvoiceResponse cancelInvoice(Long id);

    /**
     * Expire invoice (auto-cancellation).
     *
     * @param id Invoice ID
     * @return Updated invoice response
     */
    InvoiceResponse expireInvoice(Long id);

    // ============ STATISTICS ============

    /**
     * Count invoices by status.
     *
     * @param status Invoice status
     * @return Number of invoices
     */
    long countByStatus(InvoiceStatus status);

    /**
     * Get total revenue from completed invoices.
     *
     * @return Total revenue
     */
    BigDecimal getTotalRevenue();

    /**
     * Get revenue within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return Revenue in period
     */
    BigDecimal getRevenueBetweenDates(LocalDateTime start, LocalDateTime end);

    /**
     * Get total revenue by user.
     *
     * @param userId User ID
     * @return Total revenue from user
     */
    BigDecimal getTotalRevenueByUser(Long userId);

    /**
     * Get average invoice amount.
     *
     * @return Average amount
     */
    BigDecimal getAverageInvoiceAmount();

    // ============ SUMMARIES ============

    /**
     * Get all invoice summaries.
     *
     * @return List of invoice summaries
     */
    List<InvoiceSummary> getAllSummaries();
}
