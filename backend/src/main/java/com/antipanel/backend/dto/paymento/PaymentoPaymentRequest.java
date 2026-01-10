package com.antipanel.backend.dto.paymento;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a payment with Paymento API.
 * Endpoint: POST /v1/payment/request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentoPaymentRequest {

    /**
     * Amount in fiat currency.
     */
    @JsonProperty("fiatAmount")
    private BigDecimal fiatAmount;

    /**
     * Fiat currency code (e.g., USD, EUR).
     */
    @JsonProperty("fiatCurrency")
    private String fiatCurrency;

    /**
     * URL to redirect user after payment completion.
     */
    @JsonProperty("ReturnUrl")
    private String returnUrl;

    /**
     * Internal order/invoice ID for reference.
     */
    @JsonProperty("orderId")
    private String orderId;

    /**
     * Transaction confirmation speed.
     * 0 = High (accepts crypto transactions on mempool)
     * 1 = Low (waits for specific number of block confirmations)
     */
    @JsonProperty("Speed")
    private Integer speed;

    /**
     * Optional specific crypto amount.
     */
    @JsonProperty("cryptoAmount")
    private BigDecimal cryptoAmount;

    /**
     * Optional additional metadata as JSON string.
     */
    @JsonProperty("additionalData")
    private String additionalData;

    /**
     * Optional customer email address.
     */
    @JsonProperty("EmailAddress")
    private String emailAddress;
}
