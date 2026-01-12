package com.antipanel.backend.dto.provider.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO for cancel response from Dripfeed Panel API.
 * Maps the response from action=cancel endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DripfeedCancelResponse {

    /**
     * Order ID that was cancelled
     */
    private Long order;

    /**
     * Cancel result: 1 for success, or error object
     */
    @JsonProperty("cancel")
    private Object cancelResult;

    /**
     * Checks if cancellation was successful
     */
    public boolean isSuccess() {
        if (cancelResult == null) {
            return false;
        }
        if (cancelResult instanceof Number) {
            return ((Number) cancelResult).intValue() == 1;
        }
        return "1".equals(cancelResult.toString());
    }

    /**
     * Gets error message if cancellation failed
     */
    public String getError() {
        if (cancelResult instanceof java.util.Map<?, ?> map) {
            Object error = map.get("error");
            return error != null ? error.toString() : null;
        }
        return null;
    }
}
