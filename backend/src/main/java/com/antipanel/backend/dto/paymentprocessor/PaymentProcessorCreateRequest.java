package com.antipanel.backend.dto.paymentprocessor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new payment processor configuration.
 * Payment processors handle user deposits (PayPal, Stripe, Coinbase, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessorCreateRequest {

    /**
     * Name of the payment processor (e.g., "PayPal", "Stripe")
     */
    @NotBlank(message = "Payment processor name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    /**
     * Unique code for the processor (e.g., "PAYPAL", "STRIPE")
     */
    @NotBlank(message = "Processor code is required")
    @Pattern(regexp = "^[A-Z_]+$", message = "Code must contain only uppercase letters and underscores")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    /**
     * Processor website URL
     */
    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String website;

    /**
     * API key for authentication
     */
    @Size(max = 255, message = "API key must not exceed 255 characters")
    private String apiKey;

    /**
     * API secret for authentication
     */
    @Size(max = 255, message = "API secret must not exceed 255 characters")
    private String apiSecret;

    /**
     * Additional configuration in JSON format
     */
    private String configJson;

    /**
     * Minimum deposit amount allowed
     */
    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.01", message = "Minimum amount must be at least 0.01")
    private BigDecimal minAmount;

    /**
     * Maximum deposit amount allowed (null for no limit)
     */
    @DecimalMin(value = "0.01", message = "Maximum amount must be at least 0.01")
    private BigDecimal maxAmount;

    /**
     * Fee percentage (0-100)
     */
    @NotNull(message = "Fee percentage is required")
    @DecimalMin(value = "0.0", message = "Fee percentage must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Fee percentage must not exceed 100")
    @Builder.Default
    private BigDecimal feePercentage = BigDecimal.ZERO;

    /**
     * Fixed fee amount
     */
    @NotNull(message = "Fixed fee is required")
    @DecimalMin(value = "0.0", message = "Fixed fee must be 0 or greater")
    @Builder.Default
    private BigDecimal feeFixed = BigDecimal.ZERO;

    /**
     * Whether the processor is active
     */
    @NotNull(message = "Active status is required")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Display order (lower values appear first)
     */
    @NotNull(message = "Sort order is required")
    @Min(value = 0, message = "Sort order must be 0 or greater")
    @Builder.Default
    private Integer sortOrder = 0;
}
