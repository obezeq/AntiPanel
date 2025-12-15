package com.antipanel.backend.dto.category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new category.
 * Categories represent social media platforms (Instagram, TikTok, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {

    /**
     * Name of the category (e.g., "Instagram", "TikTok")
     */
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    /**
     * URL-friendly slug (e.g., "instagram", "tiktok").
     * If null, will be auto-generated from name.
     */
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Size(max = 50, message = "Slug must not exceed 50 characters")
    private String slug;

    /**
     * URL of the category icon/logo
     */
    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    /**
     * Display order in the catalog (lower values appear first)
     */
    @NotNull(message = "Sort order is required")
    @Min(value = 0, message = "Sort order must be 0 or greater")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Whether the category is visible in the public catalog
     */
    @NotNull(message = "Active status is required")
    @Builder.Default
    private Boolean isActive = true;
}
