package com.antipanel.backend.dto.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for provider with all details.
 * Used for admin views and provider management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {

    /**
     * Unique identifier
     */
    private Integer id;

    /**
     * Provider name
     */
    private String name;

    /**
     * Provider website URL
     */
    private String website;

    /**
     * Base API URL
     */
    private String apiUrl;

    /**
     * API key (consider masking in production)
     */
    private String apiKey;

    /**
     * Whether the provider is active
     */
    private Boolean isActive;

    /**
     * Current balance with provider (if tracked)
     */
    private BigDecimal balance;

    /**
     * When the provider was registered
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Count of active services from this provider
     */
    private Long serviceCount;
}
