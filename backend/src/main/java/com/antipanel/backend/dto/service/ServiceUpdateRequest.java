package com.antipanel.backend.dto.service;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing service.
 * All fields are optional to support partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdateRequest {

    /**
     * Updated service name
     */
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    /**
     * Updated description
     */
    private String description;

    /**
     * Updated price per K
     */
    @DecimalMin(value = "0.0001", message = "Price per K must be greater than 0")
    private BigDecimal pricePerK;

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
     * Updated average delivery time
     */
    @Size(max = 50, message = "Average time must not exceed 50 characters")
    private String averageTime;

    /**
     * Updated active status
     */
    private Boolean isActive;

    /**
     * Updated sort order
     */
    @Min(value = 0, message = "Sort order must be 0 or greater")
    private Integer sortOrder;
}
