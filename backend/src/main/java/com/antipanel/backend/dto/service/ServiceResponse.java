package com.antipanel.backend.dto.service;

import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for service (admin view).
 * Includes cost and profit margin calculations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    /**
     * Unique service ID
     */
    private Integer id;

    /**
     * Category ID
     */
    private Integer categoryId;

    /**
     * Service type ID
     */
    private Integer serviceTypeId;

    /**
     * Provider service ID
     */
    private Integer providerServiceId;

    /**
     * Service name
     */
    private String name;

    /**
     * Service description
     */
    private String description;

    /**
     * Service quality
     */
    private ServiceQuality quality;

    /**
     * Service speed
     */
    private ServiceSpeed speed;

    /**
     * Minimum quantity
     */
    private Integer minQuantity;

    /**
     * Maximum quantity
     */
    private Integer maxQuantity;

    /**
     * Price per K (what users pay)
     */
    private BigDecimal pricePerK;

    /**
     * Cost per K from provider (admin only)
     */
    private BigDecimal costPerK;

    /**
     * Profit margin percentage (admin only)
     * Calculated: (pricePerK - costPerK) / pricePerK * 100
     */
    private BigDecimal profitMargin;

    /**
     * Refill guarantee days
     */
    private Integer refillDays;

    /**
     * Average delivery time
     */
    private String averageTime;

    /**
     * Whether service is active
     */
    private Boolean isActive;

    /**
     * Display order
     */
    private Integer sortOrder;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}
