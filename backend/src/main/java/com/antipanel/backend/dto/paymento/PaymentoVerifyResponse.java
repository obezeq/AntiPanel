package com.antipanel.backend.dto.paymento;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Paymento payment verification.
 * Contains order details and confirmation status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentoVerifyResponse {

    /**
     * Whether the verification was successful.
     */
    private boolean success;

    /**
     * Optional message from API.
     */
    private String message;

    /**
     * Verification body with order details.
     */
    private PaymentoVerifyBody body;

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
     * Body of the verification response containing order details.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentoVerifyBody {

        /**
         * Payment token.
         */
        private String token;

        /**
         * Internal order ID.
         */
        private String orderId;

        /**
         * Additional metadata from original request.
         */
        private List<AdditionalDataItem> additionalData;
    }

    /**
     * Key-value item for additional data.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdditionalDataItem {

        private String key;
        private String value;
    }
}
