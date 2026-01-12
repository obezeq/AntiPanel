package com.antipanel.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DripfeedPanel provider API configuration properties.
 * Binds to dripfeed-panel.* properties in application.yml
 *
 * SECURITY: Environment variable takes priority over database values.
 */
@ConfigurationProperties(prefix = "dripfeed-panel")
public record DripfeedPanelConfig(
        /**
         * DripfeedPanel API key for authentication.
         * Obtained from DripfeedPanel merchant dashboard.
         */
        String apiKey,

        /**
         * DripfeedPanel API base URL.
         * Default: https://dripfeedpanel.com/api/v2
         */
        String baseUrl
) {
    /**
     * Default constructor with sensible defaults.
     */
    public DripfeedPanelConfig {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://dripfeedpanel.com/api/v2";
        }
    }
}
