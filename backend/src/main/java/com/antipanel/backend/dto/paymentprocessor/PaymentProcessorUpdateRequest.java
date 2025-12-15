package com.antipanel.backend.dto.paymentprocessor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing payment processor.
 * All fields are optional to support partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessorUpdateRequest {

    /**
     * Updated processor name
     */
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    /**
     * Updated website URL
     */
    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String website;

    /**
     * Updated API key
     */
    @Size(max = 255, message = "API key must not exceed 255 characters")
    private String apiKey;

    /**
     * Updated API secret
     */
    @Size(max = 255, message = "API secret must not exceed 255 characters")
    private String apiSecret;

    /**
     * Updated configuration JSON
     */
    private String configJson;

    /**
     * Updated minimum amount
     */
    @DecimalMin(value = "0.01", message = "Minimum amount must be at least 0.01")
    private BigDecimal minAmount;

    /**
     * Updated maximum amount
     */
    @DecimalMin(value = "0.01", message = "Maximum amount must be at least 0.01")
    private BigDecimal maxAmount;

    /**
     * Updated fee percentage
     */
    @DecimalMin(value = "0.0", message = "Fee percentage must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Fee percentage must not exceed 100")
    private BigDecimal feePercentage;

    /**
     * Updated fixed fee
     */
    @DecimalMin(value = "0.0", message = "Fixed fee must be 0 or greater")
    private BigDecimal feeFixed;

    /**
     * Updated active status
     */
    private Boolean isActive;

    /**
     * Updated sort order
     */
    @Min(value = 0, message = "Sort order must be 0 or greater")
    private Integer sortOrder;
}
