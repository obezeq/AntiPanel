package com.antipanel.backend.dto.providerservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight provider service summary for nested references.
 * Contains essential fields for service details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderServiceSummary {

    /**
     * Unique identifier
     */
    private Integer id;

    /**
     * Service ID in provider's system
     */
    private String providerServiceId;

    /**
     * Service name
     */
    private String name;

    /**
     * Cost per 1000 units
     */
    private BigDecimal costPerK;
}
