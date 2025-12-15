package com.antipanel.backend.dto.servicetype;

import com.antipanel.backend.dto.category.CategorySummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for service type with all details.
 * Used for single service type retrieval and admin views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeResponse {

    /**
     * Unique identifier
     */
    private Integer id;

    /**
     * Parent category summary
     */
    private CategorySummary category;

    /**
     * Service type name
     */
    private String name;

    /**
     * URL-friendly slug
     */
    private String slug;

    /**
     * Display order within the category
     */
    private Integer sortOrder;

    /**
     * Whether the service type is active
     */
    private Boolean isActive;

    /**
     * Count of active services of this type (optional, populated on demand)
     */
    private Long serviceCount;
}
