package com.antipanel.backend.entity;

import com.antipanel.backend.entity.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa el registro de auditoría de todos los movimientos de balance.
 *
 * Tabla: transactions
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transactions_user", columnList = "user_id"),
    @Index(name = "idx_transactions_type", columnList = "type"),
    @Index(name = "idx_transactions_created", columnList = "created_at"),
    @Index(name = "idx_transactions_user_created", columnList = "user_id, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "El usuario no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_transactions_user"))
    private User user;

    @NotNull(message = "El tipo no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "transaction_type_enum", nullable = false)
    private TransactionType type;

    @NotNull(message = "El monto no puede ser nulo")
    @Column(name = "amount", precision = 12, scale = 4, nullable = false)
    private BigDecimal amount;

    @NotNull(message = "El balance before no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El balance before no puede ser negativo")
    @Column(name = "balance_before", precision = 12, scale = 4, nullable = false)
    private BigDecimal balanceBefore;

    @NotNull(message = "El balance after no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El balance after no puede ser negativo")
    @Column(name = "balance_after", precision = 12, scale = 4, nullable = false)
    private BigDecimal balanceAfter;

    @Size(max = 50, message = "El reference type no puede exceder 50 caracteres")
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Métodos de utilidad

    /**
     * Verifica si la transacción es un crédito (aumenta el balance)
     */
    public boolean isCredit() {
        return type.isCredit();
    }

    /**
     * Verifica si la transacción es un débito (disminuye el balance)
     */
    public boolean isDebit() {
        return type.isDebit();
    }

    /**
     * Obtiene el monto como valor absoluto
     */
    public BigDecimal getAbsoluteAmount() {
        return amount.abs();
    }

    /**
     * Valida la consistencia del balance
     */
    public boolean isBalanceConsistent() {
        return balanceAfter.compareTo(balanceBefore.add(amount)) == 0;
    }
}
