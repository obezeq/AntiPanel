package com.antipanel.backend.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight category summary for nested references in other DTOs.
 * Contains only essential fields needed for display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummary {

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
}
