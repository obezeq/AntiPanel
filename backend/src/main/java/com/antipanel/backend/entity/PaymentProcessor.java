package com.antipanel.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa la configuración de los procesadores de pago.
 *
 * Tabla: payment_processors
 */
@Entity
@Table(name = "payment_processors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @NotBlank(message = "El código no puede estar vacío")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Size(max = 255, message = "El website no puede exceder 255 caracteres")
    @Column(name = "website", length = 255)
    private String website;

    @Size(max = 255, message = "La API Key no puede exceder 255 caracteres")
    @Column(name = "api_key", length = 255)
    private String apiKey;

    @Size(max = 255, message = "El API Secret no puede exceder 255 caracteres")
    @Column(name = "api_secret", length = 255)
    private String apiSecret;

    /**
     * Configuración adicional en formato JSON.
     * Por simplicidad en el MVP, se almacena como String.
     * Para producción, considerar usar hibernate-types para Map<String, Object>
     */
    @Column(name = "config_json", columnDefinition = "jsonb")
    private String configJson;

    @NotNull(message = "El monto mínimo no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El monto mínimo debe ser mayor a 0")
    @Column(name = "min_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal minAmount = BigDecimal.valueOf(1.00);

    @DecimalMin(value = "0.01", message = "El monto máximo debe ser mayor a 0")
    @Column(name = "max_amount", precision = 10, scale = 2)
    private BigDecimal maxAmount;

    @NotNull(message = "El porcentaje de comisión no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El porcentaje de comisión no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El porcentaje de comisión no puede exceder 100")
    @Column(name = "fee_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal feePercentage = BigDecimal.ZERO;

    @NotNull(message = "La comisión fija no puede ser nula")
    @DecimalMin(value = "0.0", message = "La comisión fija no puede ser negativa")
    @Column(name = "fee_fixed", precision = 10, scale = 2, nullable = false)
    private BigDecimal feeFixed = BigDecimal.ZERO;

    @NotNull(message = "El campo is_active no puede ser nulo")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "El sort order no puede ser nulo")
    @Min(value = 0, message = "El sort order no puede ser negativo")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // Métodos de utilidad

    /**
     * Calcula la comisión total para un monto dado
     */
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        BigDecimal percentageFee = amount.multiply(feePercentage)
                                         .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        return percentageFee.add(feeFixed);
    }

    /**
     * Calcula el monto neto después de comisiones
     */
    public BigDecimal calculateNetAmount(BigDecimal amount) {
        return amount.subtract(calculateFee(amount));
    }

    /**
     * Verifica si el monto está dentro del rango permitido
     */
    public boolean isAmountValid(BigDecimal amount) {
        if (amount == null || amount.compareTo(minAmount) < 0) {
            return false;
        }
        return maxAmount == null || amount.compareTo(maxAmount) <= 0;
    }
}
