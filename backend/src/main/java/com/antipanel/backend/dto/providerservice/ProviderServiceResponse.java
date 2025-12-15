package com.antipanel.backend.dto.providerservice;

import com.antipanel.backend.dto.provider.ProviderSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for provider service with all details.
 * Used for admin views and service management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderServiceResponse {

    /**
     * Unique identifier
     */
    private Integer id;

    /**
     * Provider offering this service
     */
    private ProviderSummary provider;

    /**
     * Service ID in provider's system
     */
    private String providerServiceId;

    /**
     * Service name from provider
     */
    private String name;

    /**
     * Minimum quantity allowed
     */
    private Integer minQuantity;

    /**
     * Maximum quantity allowed
     */
    private Integer maxQuantity;

    /**
     * Cost per 1000 units
     */
    private BigDecimal costPerK;

    /**
     * Days of refill guarantee
     */
    private Integer refillDays;

    /**
     * Whether this service is active
     */
    private Boolean isActive;

    /**
     * Last synchronization with provider API
     */
    private LocalDateTime lastSyncedAt;
}
