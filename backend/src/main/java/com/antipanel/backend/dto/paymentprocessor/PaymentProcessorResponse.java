package com.antipanel.backend.dto.paymentprocessor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for payment processor.
 * API credentials are excluded for security reasons.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessorResponse {

    /**
     * Unique identifier
     */
    private Integer id;

    /**
     * Processor name
     */
    private String name;

    /**
     * Unique processor code
     */
    private String code;

    /**
     * Processor website URL
     */
    private String website;

    /**
     * Minimum deposit amount
     */
    private BigDecimal minAmount;

    /**
     * Maximum deposit amount (null for no limit)
     */
    private BigDecimal maxAmount;

    /**
     * Fee percentage (0-100)
     */
    private BigDecimal feePercentage;

    /**
     * Fixed fee amount
     */
    private BigDecimal feeFixed;

    /**
     * Whether the processor is active
     */
    private Boolean isActive;

    /**
     * Display order
     */
    private Integer sortOrder;

    // Note: API credentials (apiKey, apiSecret, configJson) are intentionally excluded for security
}
