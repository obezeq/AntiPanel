package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillCreateRequest;
import com.antipanel.backend.dto.orderrefill.OrderRefillResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillSummary;
import com.antipanel.backend.entity.enums.RefillStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for OrderRefill operations.
 * Handles refill request creation, status management, and queries.
 */
public interface OrderRefillService {

    // ============ CREATE OPERATIONS ============

    /**
     * Create a refill request for an order.
     *
     * @param userId  User ID
     * @param request Refill creation data
     * @return Created refill response
     */
    OrderRefillResponse create(Long userId, OrderRefillCreateRequest request);

    // ============ READ OPERATIONS ============

    /**
     * Get refill by ID.
     *
     * @param id Refill ID
     * @return Refill response
     */
    OrderRefillResponse getById(Long id);

    /**
     * Get refill by provider refill ID.
     *
     * @param providerRefillId Provider's refill ID
     * @return Refill response
     */
    OrderRefillResponse getByProviderRefillId(String providerRefillId);

    // ============ BY ORDER ============

    /**
     * Get all refills for an order.
     *
     * @param orderId Order ID
     * @return List of refill responses
     */
    List<OrderRefillResponse> getByOrder(Long orderId);

    /**
     * Check if order has pending refill request.
     *
     * @param orderId Order ID
     * @return true if order has pending refill
     */
    boolean hasPendingRefill(Long orderId);

    /**
     * Count refills for an order.
     *
     * @param orderId Order ID
     * @return Number of refills
     */
    long countByOrder(Long orderId);

    // ============ BY STATUS ============

    /**
     * Get all refills by status.
     *
     * @param status Refill status
     * @return List of refill responses
     */
    List<OrderRefillResponse> getByStatus(RefillStatus status);

    /**
     * Get refills by status with pagination.
     *
     * @param status   Refill status
     * @param pageable Pagination parameters
     * @return Page of refill responses
     */
    PageResponse<OrderRefillResponse> getByStatusPaginated(RefillStatus status, Pageable pageable);

    /**
     * Get all pending refills needing processing.
     *
     * @return List of pending refill responses
     */
    List<OrderRefillResponse> getPendingRefills();

    // ============ BY USER ============

    /**
     * Get all refills for a user.
     *
     * @param userId User ID
     * @return List of refill responses
     */
    List<OrderRefillResponse> getByUser(Long userId);

    /**
     * Get user's refills with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of refill responses
     */
    PageResponse<OrderRefillResponse> getByUserPaginated(Long userId, Pageable pageable);

    // ============ TIME-BASED QUERIES ============

    /**
     * Get refills created within date range.
     *
     * @param start Start timestamp
     * @param end   End timestamp
     * @return List of refill responses
     */
    List<OrderRefillResponse> getRefillsBetweenDates(LocalDateTime start, LocalDateTime end);

    // ============ STATUS OPERATIONS ============

    /**
     * Update refill status.
     *
     * @param id     Refill ID
     * @param status New status
     * @return Updated refill response
     */
    OrderRefillResponse updateStatus(Long id, RefillStatus status);

    /**
     * Mark refill as processing.
     *
     * @param id               Refill ID
     * @param providerRefillId Provider's refill ID
     * @return Updated refill response
     */
    OrderRefillResponse markAsProcessing(Long id, String providerRefillId);

    /**
     * Complete refill.
     *
     * @param id Refill ID
     * @return Updated refill response
     */
    OrderRefillResponse completeRefill(Long id);

    /**
     * Cancel refill.
     *
     * @param id Refill ID
     * @return Updated refill response
     */
    OrderRefillResponse cancelRefill(Long id);

    /**
     * Reject refill.
     *
     * @param id Refill ID
     * @return Updated refill response
     */
    OrderRefillResponse rejectRefill(Long id);

    // ============ STATISTICS ============

    /**
     * Count refills by status.
     *
     * @param status Refill status
     * @return Number of refills
     */
    long countByStatus(RefillStatus status);

    // ============ SUMMARIES ============

    /**
     * Get all refill summaries.
     *
     * @return List of refill summaries
     */
    List<OrderRefillSummary> getAllSummaries();
}
