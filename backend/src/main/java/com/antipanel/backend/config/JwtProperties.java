package com.antipanel.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration properties.
 * Binds to spring.jwt.* properties in application.yml
 */
@ConfigurationProperties(prefix = "spring.jwt")
public record JwtProperties(
        /**
         * Base64-encoded secret key for signing tokens.
         * Must be at least 256 bits (32 bytes) for HS256.
         */
        String secret,

        /**
         * Access token expiration time in milliseconds.
         * Default: 15 minutes (900000ms)
         */
        long accessTokenExpiration,

        /**
         * Refresh token expiration time in milliseconds.
         * Default: 7 days (604800000ms)
         */
        long refreshTokenExpiration,

        /**
         * Token issuer identifier.
         */
        String issuer
) {
    /**
     * Default constructor with sensible defaults.
     */
    public JwtProperties {
        if (accessTokenExpiration <= 0) {
            accessTokenExpiration = 900000L; // 15 minutes
        }
        if (refreshTokenExpiration <= 0) {
            refreshTokenExpiration = 604800000L; // 7 days
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "antipanel";
        }
    }
}
