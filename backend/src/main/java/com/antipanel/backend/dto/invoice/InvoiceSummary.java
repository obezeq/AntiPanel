package com.antipanel.backend.dto.invoice;

import com.antipanel.backend.entity.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight invoice summary for list views.
 * Contains essential fields for invoice history display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSummary {

    /**
     * Unique invoice ID
     */
    private Long id;

    /**
     * Requested amount
     */
    private BigDecimal amount;

    /**
     * Net amount credited
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
     * Creation timestamp
     */
    private LocalDateTime createdAt;
}
