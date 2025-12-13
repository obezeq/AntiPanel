package com.antipanel.backend.dto.providerservice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing provider service.
 * All fields are optional to support partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderServiceUpdateRequest {

    /**
     * Updated service name
     */
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    /**
     * Updated minimum quantity
     */
    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private Integer minQuantity;

    /**
     * Updated maximum quantity
     */
    @Min(value = 1, message = "Maximum quantity must be at least 1")
    private Integer maxQuantity;

    /**
     * Updated cost per K
     */
    @DecimalMin(value = "0.0001", message = "Cost per K must be greater than 0")
    private BigDecimal costPerK;

    /**
     * Updated refill days
     */
    @Min(value = 0, message = "Refill days must be 0 or greater")
    private Integer refillDays;

    /**
     * Updated active status
     */
    private Boolean isActive;
}
