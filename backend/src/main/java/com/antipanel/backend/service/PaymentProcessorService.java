package com.antipanel.backend.service;

import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorCreateRequest;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorResponse;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorSummary;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorUpdateRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for PaymentProcessor operations.
 */
public interface PaymentProcessorService {

    // ============ CRUD OPERATIONS ============

    /**
     * Create a new payment processor.
     *
     * @param request Payment processor creation data
     * @return Created payment processor response
     */
    PaymentProcessorResponse create(PaymentProcessorCreateRequest request);

    /**
     * Get payment processor by ID.
     *
     * @param id Payment processor ID
     * @return Payment processor response
     */
    PaymentProcessorResponse getById(Integer id);

    /**
     * Get payment processor by code.
     *
     * @param code Payment processor code
     * @return Payment processor response
     */
    PaymentProcessorResponse getByCode(String code);

    /**
     * Update payment processor.
     *
     * @param id      Payment processor ID
     * @param request Update data
     * @return Updated payment processor response
     */
    PaymentProcessorResponse update(Integer id, PaymentProcessorUpdateRequest request);

    /**
     * Delete payment processor by ID.
     *
     * @param id Payment processor ID
     */
    void delete(Integer id);

    // ============ LISTING ============

    /**
     * Get all payment processors.
     *
     * @return List of all payment processors
     */
    List<PaymentProcessorResponse> getAll();

    /**
     * Get all active payment processors.
     *
     * @return List of active payment processors
     */
    List<PaymentProcessorResponse> getAllActive();

    /**
     * Get all payment processor summaries.
     *
     * @return List of payment processor summaries
     */
    List<PaymentProcessorSummary> getAllSummaries();

    /**
     * Get active payment processors that support a specific amount.
     *
     * @param amount Deposit amount to validate
     * @return List of compatible processors
     */
    List<PaymentProcessorResponse> getProcessorsForAmount(BigDecimal amount);

    // ============ STATUS OPERATIONS ============

    /**
     * Toggle payment processor active status.
     *
     * @param id Payment processor ID
     * @return Updated payment processor response
     */
    PaymentProcessorResponse toggleActive(Integer id);

    /**
     * Activate a payment processor.
     *
     * @param id Payment processor ID
     * @return Updated payment processor response
     */
    PaymentProcessorResponse activate(Integer id);

    /**
     * Deactivate a payment processor.
     *
     * @param id Payment processor ID
     * @return Updated payment processor response
     */
    PaymentProcessorResponse deactivate(Integer id);

    // ============ STATISTICS ============

    /**
     * Count active payment processors.
     *
     * @return Number of active payment processors
     */
    long countActive();

    // ============ VALIDATION ============

    /**
     * Check if code already exists.
     *
     * @param code Code to check
     * @return true if code exists
     */
    boolean existsByCode(String code);
}
