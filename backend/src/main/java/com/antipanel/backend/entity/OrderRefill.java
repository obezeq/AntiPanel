package com.antipanel.backend.entity;

import com.antipanel.backend.entity.enums.RefillStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entidad que representa las solicitudes de refill para órdenes completadas.
 *
 * Tabla: order_refills
 */
@Entity
@Table(name = "order_refills", indexes = {
    @Index(name = "idx_order_refills_order", columnList = "order_id"),
    @Index(name = "idx_order_refills_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "La orden no puede ser nula")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_order_refills_order"))
    private Order order;

    @Size(max = 100, message = "El provider refill ID no puede exceder 100 caracteres")
    @Column(name = "provider_refill_id", length = 100)
    private String providerRefillId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private RefillStatus status = RefillStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Métodos de utilidad

    /**
     * Verifica si el refill está en un estado final
     */
    public boolean isFinal() {
        return status.isFinal();
    }

    /**
     * Verifica si el refill fue exitoso
     */
    public boolean isSuccessful() {
        return status == RefillStatus.COMPLETED;
    }
}
