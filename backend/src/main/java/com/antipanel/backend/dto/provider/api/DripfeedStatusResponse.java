package com.antipanel.backend.dto.provider.api;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for order status response from Dripfeed Panel API.
 * Maps the response from action=status endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DripfeedStatusResponse {

    /**
     * Amount charged for the order
     */
    private String charge;

    /**
     * Initial count when order started
     */
    @lombok.Builder.Default
    private String start_count = "0";

    /**
     * Current order status
     */
    private String status;

    /**
     * Remaining quantity to be delivered
     */
    private String remains;

    /**
     * Currency code (usually USD)
     */
    private String currency;

    /**
     * Error message if order not found
     */
    private String error;

    /**
     * Checks if the response indicates an error
     */
    public boolean hasError() {
        return error != null && !error.isBlank();
    }

    /**
     * Parses charge to BigDecimal
     */
    public BigDecimal getChargeAsDecimal() {
        if (charge == null || charge.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(charge);
    }

    /**
     * Parses start_count to Integer
     */
    public Integer getStartCountAsInteger() {
        if (start_count == null || start_count.isBlank()) {
            return 0;
        }
        return Integer.parseInt(start_count);
    }

    /**
     * Parses remains to Integer
     */
    public Integer getRemainsAsInteger() {
        if (remains == null || remains.isBlank()) {
            return 0;
        }
        return Integer.parseInt(remains);
    }
}
