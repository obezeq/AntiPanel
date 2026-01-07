package com.antipanel.backend.entity;

import com.antipanel.backend.entity.enums.InvoiceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa las facturas/depósitos de los usuarios.
 *
 * Tabla: invoices
 */
@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoices_user", columnList = "user_id"),
    @Index(name = "idx_invoices_processor", columnList = "processor_id"),
    @Index(name = "idx_invoices_status", columnList = "status"),
    @Index(name = "idx_invoices_created", columnList = "created_at"),
    @Index(name = "idx_invoices_user_status", columnList = "user_id, status"),
    @Index(name = "idx_invoices_user_created", columnList = "user_id, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "El usuario no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_invoices_user"))
    private User user;

    @NotNull(message = "El procesador no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_invoices_processor"))
    private PaymentProcessor processor;

    @Size(max = 255, message = "El processor invoice ID no puede exceder 255 caracteres")
    @Column(name = "processor_invoice_id", length = 255)
    private String processorInvoiceId;

    @NotNull(message = "El monto no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull(message = "La comisión no puede ser nula")
    @DecimalMin(value = "0.0", message = "La comisión no puede ser negativa")
    @Column(name = "fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal fee = BigDecimal.ZERO;

    @NotNull(message = "El monto neto no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El monto neto debe ser mayor a 0")
    @Column(name = "net_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @NotBlank(message = "La moneda no puede estar vacía")
    @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe ser un código ISO 4217 de 3 letras")
    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "USD";

    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Size(max = 500, message = "La payment URL no puede exceder 500 caracteres")
    @Column(name = "payment_url", length = 500)
    private String paymentUrl;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Métodos de utilidad

    /**
     * Verifica si la factura fue pagada
     */
    public boolean isPaid() {
        return status.isSuccessful();
    }

    /**
     * Verifica si la factura está en un estado final
     */
    public boolean isFinal() {
        return status.isFinal();
    }

    /**
     * Calcula el monto neto a partir del monto y comisión
     */
    public void calculateNetAmount() {
        this.netAmount = this.amount.subtract(this.fee);
    }
}
