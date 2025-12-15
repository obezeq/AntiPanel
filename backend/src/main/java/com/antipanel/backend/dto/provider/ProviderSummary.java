package com.antipanel.backend.dto.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight provider summary for nested references.
 * Contains only essential fields needed for display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderSummary {

    /**
     * Unique identifier
     */
    private Integer id;

    /**
     * Provider name
     */
    private String name;

    /**
     * Whether the provider is active
     */
    private Boolean isActive;
}
