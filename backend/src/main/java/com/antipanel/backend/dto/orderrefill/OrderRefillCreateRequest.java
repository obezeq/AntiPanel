package com.antipanel.backend.dto.orderrefill;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new refill request.
 * Users can request refills for orders within the guarantee period.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefillCreateRequest {

    /**
     * ID of the order to refill
     */
    @NotNull(message = "Order ID is required")
    private Long orderId;
}
