package com.antipanel.backend.dto.provider.api;

import lombok.*;

/**
 * DTO for order creation response from Dripfeed Panel API.
 * Maps the response from action=add endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DripfeedOrderResponse {

    /**
     * The created order ID in the provider system.
     * Null if there was an error.
     */
    private Long order;

    /**
     * Error message if order creation failed.
     */
    private String error;

    /**
     * Checks if the response indicates success
     */
    public boolean isSuccess() {
        return order != null && error == null;
    }
}
