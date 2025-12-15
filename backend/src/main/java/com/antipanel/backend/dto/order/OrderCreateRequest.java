package com.antipanel.backend.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new order.
 * Orders are the core business transaction in the SMM panel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    /**
     * ID of the service being ordered
     */
    @NotNull(message = "Service ID is required")
    private Integer serviceId;

    /**
     * Target URL or username (e.g., Instagram post URL or @username)
     */
    @NotBlank(message = "Target is required")
    @Size(max = 500, message = "Target must not exceed 500 characters")
    private String target;

    /**
     * Quantity to order
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
