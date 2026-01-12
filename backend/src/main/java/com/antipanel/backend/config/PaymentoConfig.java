package com.antipanel.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Paymento payment gateway configuration properties.
 * Binds to paymento.* properties in application.yml
 *
 * Configuration can be overridden per PaymentProcessor via config_json field.
 */
@ConfigurationProperties(prefix = "paymento")
public record PaymentoConfig(
        /**
         * Paymento API key for authentication.
         * Obtained from Paymento merchant dashboard.
         */
        String apiKey,

        /**
         * Paymento API secret for webhook HMAC verification.
         * Used to validate incoming webhook signatures.
         */
        String apiSecret,

        /**
         * Paymento API base URL.
         * Default: https://api.paymento.io/v1
         */
        String baseUrl,

        /**
         * Return URL where users are redirected after payment.
         * Should point to the wallet page.
         */
        String returnUrl,

        /**
         * Default transaction confirmation speed.
         * 0 = High (accepts transactions on mempool)
         * 1 = Low (waits for block confirmations)
         */
        int defaultSpeed
) {
    /**
     * Default constructor with sensible defaults.
     */
    public PaymentoConfig {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.paymento.io/v1";
        }
        if (returnUrl == null || returnUrl.isBlank()) {
            returnUrl = "http://localhost:4200/wallet";
        }
        if (defaultSpeed < 0 || defaultSpeed > 1) {
            defaultSpeed = 0; // High speed by default
        }
    }

    /**
     * Gets the payment gateway URL format.
     */
    public String getPaymentGatewayUrl() {
        return "https://app.paymento.io/gateway?token=";
    }
}
