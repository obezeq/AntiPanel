package com.antipanel.backend.dto.paymentprocessor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight payment processor summary for nested references.
 * Contains essential fields needed for invoice display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessorSummary {

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
     * Minimum deposit amount
     */
    private BigDecimal minAmount;

    /**
     * Maximum deposit amount
     */
    private BigDecimal maxAmount;

    /**
     * Fee percentage
     */
    private BigDecimal feePercentage;

    /**
     * Fixed fee amount
     */
    private BigDecimal feeFixed;
}
