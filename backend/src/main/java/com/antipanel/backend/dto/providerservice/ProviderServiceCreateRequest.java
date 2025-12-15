package com.antipanel.backend.dto.providerservice;

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
 * Request DTO for creating a new provider service.
 * Provider services are the actual services available from external providers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderServiceCreateRequest {

    /**
     * ID of the provider offering this service
     */
    @NotNull(message = "Provider ID is required")
    private Integer providerId;

    /**
     * Service ID in the provider's system
     */
    @NotBlank(message = "Provider service ID is required")
    @Size(max = 50, message = "Provider service ID must not exceed 50 characters")
    private String providerServiceId;

    /**
     * Service name/description from provider
     */
    @NotBlank(message = "Service name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    /**
     * Minimum quantity allowed by provider
     */
    @NotNull(message = "Minimum quantity is required")
    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private Integer minQuantity;

    /**
     * Maximum quantity allowed by provider
     */
    @NotNull(message = "Maximum quantity is required")
    @Min(value = 1, message = "Maximum quantity must be at least 1")
    private Integer maxQuantity;

    /**
     * Cost per 1000 units (what we pay to provider)
     */
    @NotNull(message = "Cost per K is required")
    @DecimalMin(value = "0.0001", message = "Cost per K must be greater than 0")
    private BigDecimal costPerK;

    /**
     * Days of refill guarantee from provider
     */
    @NotNull(message = "Refill days is required")
    @Min(value = 0, message = "Refill days must be 0 or greater")
    @Builder.Default
    private Integer refillDays = 0;

    /**
     * Whether this provider service is active
     */
    @NotNull(message = "Active status is required")
    @Builder.Default
    private Boolean isActive = true;
}
