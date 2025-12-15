package com.antipanel.backend.dto.transaction;

import com.antipanel.backend.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight transaction summary for list views.
 * Contains essential fields for transaction history display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {

    /**
     * Unique transaction ID
     */
    private Long id;

    /**
     * Transaction type
     */
    private TransactionType type;

    /**
     * Transaction amount
     */
    private BigDecimal amount;

    /**
     * Balance after transaction
     */
    private BigDecimal balanceAfter;

    /**
     * Transaction timestamp
     */
    private LocalDateTime createdAt;
}
