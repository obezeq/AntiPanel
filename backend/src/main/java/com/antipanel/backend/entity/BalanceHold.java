package com.antipanel.backend.entity;

import com.antipanel.backend.entity.enums.BalanceHoldStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a balance hold/reservation.
 * Used to temporarily reserve funds before order submission.
 * Follows the Balance Reservation Pattern for ACID compliance.
 *
 * Table: balance_holds
 */
@Entity
@Table(name = "balance_holds", indexes = {
    @Index(name = "idx_balance_holds_user_status", columnList = "user_id, status"),
    @Index(name = "idx_balance_holds_idempotency", columnList = "idempotency_key"),
    @Index(name = "idx_balance_holds_expires", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @NotNull(message = "User cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_balance_holds_user"))
    private User user;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0001", message = "Amount must be greater than 0")
    @Column(name = "amount", precision = 12, scale = 4, nullable = false)
    private BigDecimal amount;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private BalanceHoldStatus status = BalanceHoldStatus.HELD;

    @Size(max = 64, message = "Idempotency key cannot exceed 64 characters")
    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    @Size(max = 50, message = "Reference type cannot exceed 50 characters")
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @NotNull(message = "Expiration time cannot be null")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Size(max = 500, message = "Release reason cannot exceed 500 characters")
    @Column(name = "release_reason", length = 500)
    private String releaseReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Checks if this hold can still be captured.
     */
    public boolean canCapture() {
        return status == BalanceHoldStatus.HELD && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Checks if this hold has expired.
     */
    public boolean isExpired() {
        return status == BalanceHoldStatus.HELD && LocalDateTime.now().isAfter(expiresAt);
    }
}
