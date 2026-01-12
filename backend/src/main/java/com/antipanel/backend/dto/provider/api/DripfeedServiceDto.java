package com.antipanel.backend.dto.provider.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO representing a service from the Dripfeed Panel API.
 * Maps the response from action=services endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DripfeedServiceDto {

    /**
     * Unique service ID in the provider system
     */
    @JsonProperty("service")
    private Integer serviceId;

    /**
     * Service display name
     */
    private String name;

    /**
     * Service type (Default, Custom Comments, etc.)
     */
    private String type;

    /**
     * Category grouping
     */
    private String category;

    /**
     * Cost per 1000 units (as string from API)
     */
    private String rate;

    /**
     * Minimum order quantity (as string from API)
     */
    private String min;

    /**
     * Maximum order quantity (as string from API)
     */
    private String max;

    /**
     * Whether refill is supported
     */
    private Boolean refill;

    /**
     * Whether cancellation is supported
     */
    private Boolean cancel;

    /**
     * Parses rate string to BigDecimal
     */
    public BigDecimal getRateAsDecimal() {
        if (rate == null || rate.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(rate);
    }

    /**
     * Parses min string to Integer
     */
    public Integer getMinAsInteger() {
        if (min == null || min.isBlank()) {
            return 1;
        }
        return Integer.parseInt(min);
    }

    /**
     * Parses max string to Integer
     */
    public Integer getMaxAsInteger() {
        if (max == null || max.isBlank()) {
            return 1000000;
        }
        return Integer.parseInt(max);
    }
}
