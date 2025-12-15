package com.antipanel.backend.dto.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new invoice (deposit request).
 * User initiates a deposit to add funds to their account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCreateRequest {

    /**
     * Payment processor ID to use
     */
    @NotNull(message = "Payment processor is required")
    private Integer processorId;

    /**
     * Amount to deposit
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    /**
     * Currency code (ISO 4217)
     */
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    @Builder.Default
    private String currency = "USD";
}
