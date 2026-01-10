package com.antipanel.backend.dto.service;

import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Public DTO for service information.
 * Excludes sensitive business data (cost, profit margin, provider IDs).
 * Used for unauthenticated/public API endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePublicResponse {

    /** Service ID */
    private Integer id;

    /** Service name */
    private String name;

    /** Service description */
    private String description;

    /** Quality level (LOW, MEDIUM, HIGH, PREMIUM) */
    private ServiceQuality quality;

    /** Delivery speed (SLOW, MEDIUM, FAST, INSTANT) */
    private ServiceSpeed speed;

    /** Minimum quantity that can be ordered */
    private Integer minQuantity;

    /** Maximum quantity that can be ordered */
    private Integer maxQuantity;

    /** Price per 1000 units (public price) */
    private BigDecimal pricePerK;

    /** Refill guarantee in days (0 = no refill) */
    private Integer refillDays;

    /** Average delivery time description */
    private String averageTime;
}
