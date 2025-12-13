package com.antipanel.backend.dto.provider;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing provider.
 * All fields are optional to support partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderUpdateRequest {

    /**
     * Updated provider name
     */
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    /**
     * Updated website URL
     */
    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String website;

    /**
     * Updated API URL
     */
    @Size(max = 255, message = "API URL must not exceed 255 characters")
    private String apiUrl;

    /**
     * Updated API key
     */
    @Size(max = 255, message = "API key must not exceed 255 characters")
    private String apiKey;

    /**
     * Updated active status
     */
    private Boolean isActive;
}
