package com.antipanel.backend.dto.paymento;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Paymento payment request.
 * Contains the payment token used for redirecting users to the payment page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentoPaymentResponse {

    private static final String PAYMENT_GATEWAY_URL = "https://app.paymento.io/gateway?token=";

    /**
     * Whether the request was successful.
     */
    private boolean success;

    /**
     * Optional message from API.
     */
    private String message;

    /**
     * Payment token (when successful).
     */
    private String body;

    /**
     * Error message (when failed).
     */
    private String error;

    /**
     * Gets the payment token from the response body.
     *
     * @return Payment token or null if request failed
     */
    public String getToken() {
        return success ? body : null;
    }

    /**
     * Builds the full payment URL for redirecting users.
     *
     * @return Payment gateway URL with token, or null if request failed
     */
    public String getPaymentUrl() {
        return success && body != null ? PAYMENT_GATEWAY_URL + body : null;
    }

    /**
     * Checks if the response indicates an error.
     *
     * @return true if there was an error
     */
    public boolean hasError() {
        return !success || error != null;
    }

    /**
     * Gets the error message if present.
     *
     * @return Error message or null
     */
    public String getErrorMessage() {
        if (!success && error != null) {
            return error;
        }
        if (!success) {
            return message != null ? message : "Unknown error";
        }
        return null;
    }
}
