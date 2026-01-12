package com.antipanel.backend.dto.paymento;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Paymento payment settings.
 * Returned by both GET and POST /v1/payment/settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentoSettingsResponse {

    /**
     * Whether the request was successful.
     */
    private boolean success;

    /**
     * Optional message from API.
     */
    private String message;

    /**
     * Settings body with configuration details.
     */
    private PaymentoSettingsBody body;

    /**
     * Error message (when failed).
     */
    private String error;

    /**
     * Checks if the response indicates an error.
     *
     * @return true if there was an error
     */
    public boolean hasError() {
        return !success || error != null;
    }

    /**
     * Body of the settings response containing configuration.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentoSettingsBody {

        /**
         * Configured webhook URL.
         */
        @JsonProperty("IPN_Url")
        private String ipnUrl;

        /**
         * Configured HTTP method.
         * 1 = POST, 2 = PUT
         */
        @JsonProperty("IPN_httpMethod")
        private Integer ipnHttpMethod;

        /**
         * Checks if POST method is configured.
         *
         * @return true if POST is configured
         */
        public boolean isPostMethod() {
            return ipnHttpMethod != null && ipnHttpMethod == 1;
        }
    }
}
