package com.antipanel.backend.dto.servicetype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight service type summary for nested references in other DTOs.
 * Contains only essential fields needed for display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeSummary {

    /**
     * Unique identifier
     */
    private Integer id;

    /**
     * Service type name
     */
    private String name;

    /**
     * URL-friendly slug
     */
    private String slug;
}
