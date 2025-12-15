package com.antipanel.backend.dto.service;

import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight service summary for catalog listings.
 * Optimized for fast catalog display with minimal data.
 * PERFORMANCE CRITICAL - Used in public service catalog.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSummary {

    /**
     * Unique service ID
     */
    private Integer id;

    /**
     * Service name
     */
    private String name;

    /**
     * Service quality indicator
     */
    private ServiceQuality quality;

    /**
     * Service delivery speed
     */
    private ServiceSpeed speed;

    /**
     * Minimum quantity that can be ordered
     */
    private Integer minQuantity;

    /**
     * Maximum quantity that can be ordered
     */
    private Integer maxQuantity;

    /**
     * Price per 1000 units
     */
    private BigDecimal pricePerK;

    /**
     * Days of refill guarantee (0 = no refill)
     */
    private Integer refillDays;

    /**
     * Estimated delivery time (e.g., "1-24 hours")
     */
    private String averageTime;
}
