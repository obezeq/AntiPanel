package com.antipanel.backend.dto.servicetype;

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
 * Request DTO for creating a new service type.
 * Service types represent the kinds of services within a category
 * (e.g., Followers, Likes, Comments for Instagram).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeCreateRequest {

    /**
     * ID of the parent category
     */
    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    /**
     * Name of the service type (e.g., "Followers", "Likes")
     */
    @NotBlank(message = "Service type name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    /**
     * URL-friendly slug (e.g., "followers", "likes")
     */
    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Size(max = 50, message = "Slug must not exceed 50 characters")
    private String slug;

    /**
     * Display order within the category (lower values appear first)
     */
    @NotNull(message = "Sort order is required")
    @Min(value = 0, message = "Sort order must be 0 or greater")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Whether the service type is visible in the public catalog
     */
    @NotNull(message = "Active status is required")
    @Builder.Default
    private Boolean isActive = true;
}
