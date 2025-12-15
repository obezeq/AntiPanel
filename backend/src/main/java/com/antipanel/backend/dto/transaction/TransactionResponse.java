package com.antipanel.backend.dto.transaction;

import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction with all details.
 * Transactions are read-only audit records of balance movements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    /**
     * Unique transaction ID
     */
    private Long id;

    /**
     * User affected by this transaction
     */
    private UserSummary user;

    /**
     * Transaction type
     */
    private TransactionType type;

    /**
     * Transaction amount (positive for credits, negative for debits)
     */
    private BigDecimal amount;

    /**
     * User balance before transaction
     */
    private BigDecimal balanceBefore;

    /**
     * User balance after transaction
     */
    private BigDecimal balanceAfter;

    /**
     * Reference type (e.g., "invoice", "order", "refund")
     */
    private String referenceType;

    /**
     * Reference ID (links to related entity)
     */
    private Long referenceId;

    /**
     * Transaction description
     */
    private String description;

    /**
     * Transaction timestamp
     */
    private LocalDateTime createdAt;
}
