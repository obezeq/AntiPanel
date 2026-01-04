package com.antipanel.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for CORS settings.
 * Bound to properties under the "app.cors" prefix.
 *
 * <p>This allows profile-based CORS configuration via application YAML files
 * and environment variable overrides for Docker/Kubernetes deployments.</p>
 *
 * @see CorsConfig
 */
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsProperties {

    /**
     * List of allowed origins for CORS requests.
     * Must not use wildcards when allowCredentials is true.
     */
    private List<String> allowedOrigins = List.of("http://localhost:4200");

    /**
     * List of allowed HTTP methods for CORS requests.
     * OPTIONS is required for preflight requests.
     */
    private List<String> allowedMethods = List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    );

    /**
     * List of allowed headers in CORS requests.
     */
    private List<String> allowedHeaders = List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
    );

    /**
     * List of headers exposed to the client in CORS responses.
     */
    private List<String> exposedHeaders = List.of("Authorization");

    /**
     * Whether to allow credentials (cookies, authorization headers) in CORS requests.
     * When true, allowedOrigins cannot contain wildcards.
     */
    private boolean allowCredentials = true;

    /**
     * Maximum age (in seconds) for the preflight cache.
     * Browsers will cache preflight responses for this duration.
     */
    private long maxAge = 3600L;
}
