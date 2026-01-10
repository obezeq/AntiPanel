package com.antipanel.backend.dto.provider.api;

import lombok.*;

/**
 * DTO for refill response from Dripfeed Panel API.
 * Maps the response from action=refill endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DripfeedRefillResponse {

    /**
     * The created refill ID.
     * Can be a string number or an error object.
     */
    private String refill;

    /**
     * Order ID for multi-refill responses
     */
    private Long order;

    /**
     * Error message if refill request failed
     */
    private String error;

    /**
     * Checks if the response indicates success
     */
    public boolean isSuccess() {
        return refill != null && error == null;
    }

    /**
     * Gets refill ID as Long
     */
    public Long getRefillId() {
        if (refill == null || refill.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(refill);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
