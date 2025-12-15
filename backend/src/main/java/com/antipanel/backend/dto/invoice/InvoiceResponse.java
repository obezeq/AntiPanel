package com.antipanel.backend.dto.invoice;

import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorSummary;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for invoice with all details.
 * Used for user deposit tracking and admin views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    /**
     * Unique invoice ID
     */
    private Long id;

    /**
     * User who created the invoice
     */
    private UserSummary user;

    /**
     * Payment processor used
     */
    private PaymentProcessorSummary processor;

    /**
     * Invoice ID from payment processor
     */
    private String processorInvoiceId;

    /**
     * Requested deposit amount
     */
    private BigDecimal amount;

    /**
     * Processing fee charged
     */
    private BigDecimal fee;

    /**
     * Net amount credited to user (amount - fee)
     */
    private BigDecimal netAmount;

    /**
     * Currency code
     */
    private String currency;

    /**
     * Invoice status
     */
    private InvoiceStatus status;

    /**
     * Payment URL (if applicable)
     */
    private String paymentUrl;

    /**
     * When payment was completed
     */
    private LocalDateTime paidAt;

    /**
     * When invoice was created
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}
