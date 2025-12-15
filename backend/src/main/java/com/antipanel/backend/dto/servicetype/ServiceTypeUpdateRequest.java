package com.antipanel.backend.dto.servicetype;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing service type.
 * All fields are optional to support partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeUpdateRequest {

    /**
     * Updated service type name
     */
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

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
