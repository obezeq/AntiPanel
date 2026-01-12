package com.antipanel.backend.dto.paymento;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for cryptocurrency information from Paymento API.
 * Represents a single supported cryptocurrency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentoCoinDto {

    /**
     * Full name of the cryptocurrency (e.g., "bitcoin", "ethereum").
     */
    private String name;

    /**
     * Short symbol/ticker (e.g., "btc", "eth", "usdt").
     */
    private String shortcut;

    /**
     * Gets display name with proper capitalization.
     *
     * @return Capitalized name
     */
    public String getDisplayName() {
        if (name == null || name.isEmpty()) {
            return shortcut != null ? shortcut.toUpperCase() : "";
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    /**
     * Gets uppercase shortcut.
     *
     * @return Uppercase symbol
     */
    public String getSymbol() {
        return shortcut != null ? shortcut.toUpperCase() : "";
    }
}
