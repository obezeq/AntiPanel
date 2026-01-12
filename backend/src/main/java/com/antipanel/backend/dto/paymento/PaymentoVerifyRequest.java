package com.antipanel.backend.dto.paymento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying a payment with Paymento API.
 * Endpoint: POST /v1/payment/verify
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentoVerifyRequest {

    /**
     * Payment token received from payment request.
     */
    private String token;
}
