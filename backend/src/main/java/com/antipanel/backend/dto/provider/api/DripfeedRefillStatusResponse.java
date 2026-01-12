package com.antipanel.backend.dto.provider.api;

import lombok.*;

/**
 * DTO for refill status response from Dripfeed Panel API.
 * Maps the response from action=refill_status endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DripfeedRefillStatusResponse {

    /**
     * Current refill status.
     * Possible values: Pending, In progress, Completed, Rejected, Error
     */
    private String status;

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
     * Checks if refill is completed
     */
    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }

    /**
     * Checks if refill was rejected
     */
    public boolean isRejected() {
        return "Rejected".equalsIgnoreCase(status);
    }
}
