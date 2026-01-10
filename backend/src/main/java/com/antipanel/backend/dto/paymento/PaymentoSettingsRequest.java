package com.antipanel.backend.dto.paymento;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for configuring Paymento payment settings.
 * Endpoint: POST /v1/payment/settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentoSettingsRequest {

    /**
     * HTTP method for IPN callbacks.
     * 1 = POST
     * 2 = PUT
     */
    public static final int HTTP_METHOD_POST = 1;
    public static final int HTTP_METHOD_PUT = 2;

    /**
     * Webhook (IPN) URL for payment notifications.
     */
    @JsonProperty("IPN_Url")
    private String ipnUrl;

    /**
     * HTTP method for webhook calls.
     * 1 = POST, 2 = PUT
     */
    @JsonProperty("httpMethod")
    private Integer httpMethod;

    /**
     * Creates a settings request for POST webhooks.
     *
     * @param ipnUrl Webhook URL
     * @return Settings request
     */
    public static PaymentoSettingsRequest forPostWebhook(String ipnUrl) {
        return PaymentoSettingsRequest.builder()
                .ipnUrl(ipnUrl)
                .httpMethod(HTTP_METHOD_POST)
                .build();
    }
}
