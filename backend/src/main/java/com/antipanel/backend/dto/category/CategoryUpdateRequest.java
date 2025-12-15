package com.antipanel.backend.dto.category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing category.
 * All fields are optional to support partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {

    /**
     * Updated category name
     */
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    /**
     * Updated icon URL
     */
    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    /**
     * Updated sort order
     */
    @Min(value = 0, message = "Sort order must be 0 or greater")
    private Integer sortOrder;

    /**
     * Updated active status
     */
    private Boolean isActive;
}
