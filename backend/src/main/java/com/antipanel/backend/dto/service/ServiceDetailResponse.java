package com.antipanel.backend.dto.service;

import com.antipanel.backend.dto.category.CategorySummary;
import com.antipanel.backend.dto.providerservice.ProviderServiceSummary;
import com.antipanel.backend.dto.servicetype.ServiceTypeSummary;
import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed service response with nested related entities.
 * Used for full service details page with all context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDetailResponse {

    /**
     * Unique service ID
     */
    private Integer id;

    /**
     * Category details
     */
    private CategorySummary category;

    /**
     * Service type details
     */
    private ServiceTypeSummary serviceType;

    /**
     * Provider service details
     */
    private ProviderServiceSummary providerService;

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
     * Price per K
     */
    private BigDecimal pricePerK;

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
