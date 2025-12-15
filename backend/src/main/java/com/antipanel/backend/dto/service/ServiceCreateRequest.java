package com.antipanel.backend.dto.service;

import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new public service.
 * Services are what users see and purchase in the catalog.
 * CRITICAL for catalog management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCreateRequest {

    /**
     * Category ID (social media platform)
     */
    @NotNull(message = "Category is required")
    private Integer categoryId;

    /**
     * Service type ID (Followers, Likes, etc.)
     */
    @NotNull(message = "Service type is required")
    private Integer serviceTypeId;

    /**
     * Provider service ID (maps to actual provider)
     */
    @NotNull(message = "Provider service is required")
    private Integer providerServiceId;

    /**
     * Public service name shown to users
     */
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    /**
     * Service description for users
     */
    private String description;

    /**
     * Service quality indicator
     */
    @NotNull(message = "Quality is required")
    private ServiceQuality quality;

    /**
     * Service delivery speed
     */
    @NotNull(message = "Speed is required")
    private ServiceSpeed speed;

    /**
     * Minimum quantity users can order
     */
    @NotNull(message = "Minimum quantity is required")
    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private Integer minQuantity;

    /**
     * Maximum quantity users can order
     */
    @NotNull(message = "Maximum quantity is required")
    @Min(value = 1, message = "Maximum quantity must be at least 1")
    private Integer maxQuantity;

    /**
     * Price per 1000 units (what users pay)
     */
    @NotNull(message = "Price per K is required")
    @DecimalMin(value = "0.0001", message = "Price per K must be greater than 0")
    private BigDecimal pricePerK;

    /**
     * Days of refill guarantee offered to users
     */
    @NotNull(message = "Refill days is required")
    @Min(value = 0, message = "Refill days must be 0 or greater")
    @Builder.Default
    private Integer refillDays = 0;

    /**
     * Estimated delivery time (e.g., "1-24 hours")
     */
    @Size(max = 50, message = "Average time must not exceed 50 characters")
    private String averageTime;

    /**
     * Whether service is visible in catalog
     */
    @NotNull(message = "Active status is required")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Display order in catalog
     */
    @NotNull(message = "Sort order is required")
    @Min(value = 0, message = "Sort order must be 0 or greater")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Validates that maximum quantity is greater than or equal to minimum quantity
     */
    @AssertTrue(message = "Maximum quantity must be greater than or equal to minimum quantity")
    private boolean isQuantityRangeValid() {
        return maxQuantity == null || minQuantity == null || maxQuantity >= minQuantity;
    }
}
