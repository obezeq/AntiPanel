package com.antipanel.backend.dto.provider.api;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for balance response from Dripfeed Panel API.
 * Maps the response from action=balance endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DripfeedBalanceResponse {

    /**
     * Current account balance
     */
    private String balance;

    /**
     * Currency code (usually USD)
     */
    private String currency;

    /**
     * Error message if request failed
     */
    private String error;

    /**
     * Checks if the response indicates an error
     */
    public boolean hasError() {
        return error != null && !error.isBlank();
    }

    /**
     * Parses balance to BigDecimal
     */
    public BigDecimal getBalanceAsDecimal() {
        if (balance == null || balance.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(balance);
    }
}
