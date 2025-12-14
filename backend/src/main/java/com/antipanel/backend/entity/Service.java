package com.antipanel.backend.entity;

import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
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
 * Entidad que representa el catálogo público de servicios mostrados a los usuarios.
 *
 * Tabla: services
 */
@Entity
@Table(name = "services", indexes = {
    @Index(name = "idx_services_category", columnList = "category_id"),
    @Index(name = "idx_services_service_type", columnList = "service_type_id"),
    @Index(name = "idx_services_provider_service", columnList = "provider_service_id"),
    @Index(name = "idx_services_catalog", columnList = "category_id, service_type_id, is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "La categoría no puede ser nula")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_services_category"))
    private Category category;

    @NotNull(message = "El tipo de servicio no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_services_service_type"))
    private ServiceType serviceType;

    @NotNull(message = "El servicio del proveedor no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_service_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_services_provider_service"))
    private ProviderService providerService;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La calidad no puede ser nula")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "quality", nullable = false)
    private ServiceQuality quality;

    @NotNull(message = "La velocidad no puede ser nula")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "speed", nullable = false)
    private ServiceSpeed speed;

    @NotNull(message = "La cantidad mínima no puede ser nula")
    @Min(value = 1, message = "La cantidad mínima debe ser mayor a 0")
    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity;

    @NotNull(message = "La cantidad máxima no puede ser nula")
    @Min(value = 1, message = "La cantidad máxima debe ser mayor a 0")
    @Column(name = "max_quantity", nullable = false)
    private Integer maxQuantity;

    @NotNull(message = "El precio por K no puede ser nulo")
    @DecimalMin(value = "0.0001", message = "El precio por K debe ser mayor a 0")
    @Column(name = "price_per_k", precision = 10, scale = 4, nullable = false)
    private BigDecimal pricePerK;

    @NotNull(message = "Los días de refill no pueden ser nulos")
    @Min(value = 0, message = "Los días de refill no pueden ser negativos")
    @Column(name = "refill_days", nullable = false)
    private Integer refillDays = 0;

    @Size(max = 50, message = "El average time no puede exceder 50 caracteres")
    @Column(name = "average_time", length = 50)
    private String averageTime;

    @NotNull(message = "El campo is_active no puede ser nulo")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "El sort order no puede ser nulo")
    @Min(value = 0, message = "El sort order no puede ser negativo")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Métodos de utilidad

    /**
     * Verifica si la cantidad está dentro del rango permitido
     */
    public boolean isQuantityValid(Integer quantity) {
        return quantity != null && quantity >= minQuantity && quantity <= maxQuantity;
    }

    /**
     * Calcula el precio total para una cantidad dada
     */
    public BigDecimal calculatePrice(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        return pricePerK.multiply(BigDecimal.valueOf(quantity))
                        .divide(BigDecimal.valueOf(1000), 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Verifica si el servicio ofrece refill
     */
    public boolean hasRefill() {
        return refillDays > 0;
    }
}
