package com.antipanel.backend.entity;

import com.antipanel.backend.entity.enums.OrderStatus;
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
 * Entidad que representa las órdenes realizadas por los usuarios.
 *
 * Tabla: orders
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user", columnList = "user_id"),
    @Index(name = "idx_orders_service", columnList = "service_id"),
    @Index(name = "idx_orders_provider_service", columnList = "provider_service_id"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_created", columnList = "created_at"),
    @Index(name = "idx_orders_user_status", columnList = "user_id, status"),
    @Index(name = "idx_orders_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_orders_idempotency_key", columnList = "idempotency_key")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Version field for optimistic locking.
     * Prevents concurrent modification issues (lost updates).
     */
    @Version
    @Column(name = "version")
    private Long version;

    @NotNull(message = "El usuario no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_orders_user"))
    private User user;

    @NotNull(message = "El servicio no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_orders_service"))
    private Service service;

    @NotBlank(message = "El nombre del servicio no puede estar vacío")
    @Size(max = 255, message = "El nombre del servicio no puede exceder 255 caracteres")
    @Column(name = "service_name", length = 255, nullable = false)
    private String serviceName;

    @NotNull(message = "El servicio del proveedor no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_service_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_orders_provider_service"))
    private ProviderService providerService;

    @Size(max = 100, message = "El provider order ID no puede exceder 100 caracteres")
    @Column(name = "provider_order_id", length = 100)
    private String providerOrderId;

    /**
     * Idempotency key for duplicate prevention.
     * Client-generated UUID to ensure idempotent order creation.
     */
    @Size(max = 64, message = "El idempotency key no puede exceder 64 caracteres")
    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    /**
     * Reference to the balance hold used for this order.
     * Null for legacy orders created before balance hold system.
     */
    @Column(name = "balance_hold_id")
    private Long balanceHoldId;

    @NotBlank(message = "El target no puede estar vacío")
    @Size(max = 500, message = "El target no puede exceder 500 caracteres")
    @Column(name = "target", length = 500, nullable = false)
    private String target;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Min(value = 0, message = "El start count no puede ser negativo")
    @Column(name = "start_count")
    private Integer startCount;

    @NotNull(message = "Los remains no pueden ser nulos")
    @Min(value = 0, message = "Los remains no pueden ser negativos")
    @Column(name = "remains", nullable = false)
    private Integer remains = 0;

    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @NotNull(message = "El precio por K no puede ser nulo")
    @DecimalMin(value = "0.0001", message = "El precio por K debe ser mayor a 0")
    @Column(name = "price_per_k", precision = 10, scale = 4, nullable = false)
    private BigDecimal pricePerK;

    @NotNull(message = "El costo por K no puede ser nulo")
    @DecimalMin(value = "0.0001", message = "El costo por K debe ser mayor a 0")
    @Column(name = "cost_per_k", precision = 10, scale = 4, nullable = false)
    private BigDecimal costPerK;

    @NotNull(message = "El total charge no puede ser nulo")
    @DecimalMin(value = "0.0001", message = "El total charge debe ser mayor a 0")
    @Column(name = "total_charge", precision = 12, scale = 4, nullable = false)
    private BigDecimal totalCharge;

    @NotNull(message = "El total cost no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El total cost no puede ser negativo")
    @Column(name = "total_cost", precision = 12, scale = 4, nullable = false)
    private BigDecimal totalCost;

    @NotNull(message = "El profit no puede ser nulo")
    @Column(name = "profit", precision = 12, scale = 4, nullable = false)
    private BigDecimal profit;

    @NotNull(message = "El campo is_refillable no puede ser nulo")
    @Column(name = "is_refillable", nullable = false)
    private Boolean isRefillable = false;

    @NotNull(message = "Los días de refill no pueden ser nulos")
    @Min(value = 0, message = "Los días de refill no pueden ser negativos")
    @Column(name = "refill_days", nullable = false)
    private Integer refillDays = 0;

    @Column(name = "refill_deadline")
    private LocalDateTime refillDeadline;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Métodos de utilidad

    /**
     * Calcula el progreso de la orden (porcentaje completado)
     */
    public Integer getProgress() {
        if (quantity == 0) return 0;
        int delivered = quantity - remains;
        return (delivered * 100) / quantity;
    }

    /**
     * Verifica si la orden está en un estado final
     */
    public boolean isFinal() {
        return status.isFinal();
    }

    /**
     * Verifica si la orden puede recibir refill
     */
    public boolean canRequestRefill() {
        if (!isRefillable || refillDeadline == null) {
            return false;
        }
        return status == OrderStatus.COMPLETED &&
               LocalDateTime.now().isBefore(refillDeadline);
    }

    /**
     * Calcula y establece el profit
     */
    public void calculateProfit() {
        this.profit = this.totalCharge.subtract(this.totalCost);
    }
}
