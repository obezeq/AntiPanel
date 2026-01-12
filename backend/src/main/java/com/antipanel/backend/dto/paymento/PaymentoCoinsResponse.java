package com.antipanel.backend.dto.paymento;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Response DTO for Paymento accepted coins list.
 * Endpoint: GET /v1/payment/coins
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentoCoinsResponse {

    /**
     * Whether the request was successful.
     */
    private boolean success;

    /**
     * Optional message from API.
     */
    private String message;

    /**
     * List of accepted cryptocurrencies.
     */
    private List<PaymentoCoinDto> body;

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
     * Gets the list of coins, never null.
     *
     * @return List of coins or empty list
     */
    public List<PaymentoCoinDto> getCoins() {
        return body != null ? body : Collections.emptyList();
    }
}
