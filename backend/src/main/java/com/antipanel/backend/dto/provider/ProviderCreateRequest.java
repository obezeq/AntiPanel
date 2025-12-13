package com.antipanel.backend.dto.provider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new external service provider.
 * Providers are external SMM panels that supply services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderCreateRequest {

    /**
     * Provider name
     */
    @NotBlank(message = "Provider name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    /**
     * Provider website URL
     */
    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String website;

    /**
     * Base URL for provider's API
     */
    @NotBlank(message = "API URL is required")
    @Size(max = 255, message = "API URL must not exceed 255 characters")
    private String apiUrl;

    /**
     * API key for authentication
     */
    @NotBlank(message = "API key is required")
    @Size(max = 255, message = "API key must not exceed 255 characters")
    private String apiKey;

    /**
     * Whether the provider is active
     */
    @NotNull(message = "Active status is required")
    @Builder.Default
    private Boolean isActive = true;
}
