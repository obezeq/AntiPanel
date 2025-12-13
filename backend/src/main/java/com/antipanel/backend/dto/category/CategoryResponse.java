package com.antipanel.backend.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for category with all details.
 * Used for single category retrieval and admin views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    /**
     * Unique identifier
     */
    private Integer id;

    /**
     * Category name
     */
    private String name;

    /**
     * URL-friendly slug
     */
    private String slug;

    /**
     * Icon/logo URL
     */
    private String iconUrl;

    /**
     * Display order in the catalog
     */
    private Integer sortOrder;

    /**
     * Whether the category is active
     */
    private Boolean isActive;

    /**
     * Timestamp when the category was created
     */
    private LocalDateTime createdAt;

    /**
     * Count of active services in this category (optional, populated on demand)
     */
    private Long serviceCount;
}
