package com.antipanel.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa el catálogo de servicios disponibles en cada proveedor.
 *
 * Tabla: provider_services
 */
@Entity
@Table(name = "provider_services",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_provider_services_provider_service",
                         columnNames = {"provider_id", "provider_service_id"})
    },
    indexes = {
        @Index(name = "idx_provider_services_provider", columnList = "provider_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "El proveedor no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_provider_services_provider"))
    private Provider provider;

    @NotBlank(message = "El provider service ID no puede estar vacío")
    @Size(max = 50, message = "El provider service ID no puede exceder 50 caracteres")
    @Column(name = "provider_service_id", length = 50, nullable = false)
    private String providerServiceId;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @NotNull(message = "La cantidad mínima no puede ser nula")
    @Min(value = 1, message = "La cantidad mínima debe ser mayor a 0")
    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity;

    @NotNull(message = "La cantidad máxima no puede ser nula")
    @Min(value = 1, message = "La cantidad máxima debe ser mayor a 0")
    @Column(name = "max_quantity", nullable = false)
    private Integer maxQuantity;

    @NotNull(message = "El costo por K no puede ser nulo")
    @DecimalMin(value = "0.0001", message = "El costo por K debe ser mayor a 0")
    @Column(name = "cost_per_k", precision = 10, scale = 4, nullable = false)
    private BigDecimal costPerK;

    @NotNull(message = "Los días de refill no pueden ser nulos")
    @Min(value = 0, message = "Los días de refill no pueden ser negativos")
    @Column(name = "refill_days", nullable = false)
    private Integer refillDays = 0;

    @NotNull(message = "El campo is_active no puede ser nulo")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    // Métodos de utilidad

    /**
     * Verifica si la cantidad está dentro del rango permitido
     */
    public boolean isQuantityValid(Integer quantity) {
        return quantity != null && quantity >= minQuantity && quantity <= maxQuantity;
    }

    /**
     * Calcula el costo total para una cantidad dada
     */
    public BigDecimal calculateCost(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        return costPerK.multiply(BigDecimal.valueOf(quantity))
                       .divide(BigDecimal.valueOf(1000), 4, java.math.RoundingMode.HALF_UP);
    }
}
