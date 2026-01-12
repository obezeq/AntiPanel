package com.antipanel.backend.service.payment.impl;

import com.antipanel.backend.config.PaymentoConfig;
import com.antipanel.backend.dto.paymento.*;
import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.PaymentProcessor;
import com.antipanel.backend.exception.PaymentoApiException;
import com.antipanel.backend.service.payment.PaymentoClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of PaymentoClient for Paymento cryptocurrency payment gateway.
 * Handles all HTTP communication with the Paymento API.
 *
 * API Base URL: https://api.paymento.io/v1
 * Method: POST/GET with JSON body
 * Response: JSON
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentoClientImpl implements PaymentoClient {

    private static final String PAYMENT_REQUEST_PATH = "/payment/request";
    private static final String PAYMENT_VERIFY_PATH = "/payment/verify";
    private static final String PAYMENT_SETTINGS_PATH = "/payment/settings";
    private static final String PAYMENT_COINS_PATH = "/payment/coins";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final PaymentoConfig paymentoConfig;

    @Override
    public PaymentoPaymentResponse createPayment(PaymentProcessor processor, Invoice invoice) {
        log.debug("Creating Paymento payment for invoice ID: {}", invoice.getId());

        PaymentoPaymentRequest request = PaymentoPaymentRequest.builder()
                .fiatAmount(invoice.getAmount().toPlainString())
                .fiatCurrency(invoice.getCurrency())
                .returnUrl(getReturnUrl(processor))
                .orderId(invoice.getId().toString())
                .speed(getSpeed(processor))
                .additionalData(buildAdditionalData(invoice))
                .emailAddress(invoice.getUser().getEmail())
                .build();

        String response = executePost(processor, PAYMENT_REQUEST_PATH, request);

        try {
            PaymentoPaymentResponse paymentResponse = objectMapper.readValue(
                    response, PaymentoPaymentResponse.class);

            if (paymentResponse.hasError()) {
                String errorMsg = paymentResponse.getErrorMessage();
                log.error("Paymento createPayment error for invoice {}: {}", invoice.getId(), errorMsg);
                throw new PaymentoApiException("createPayment", errorMsg);
            }

            log.info("Created Paymento payment for invoice ID: {}, token: {}",
                    invoice.getId(), paymentResponse.getToken());
            return paymentResponse;
        } catch (JsonProcessingException e) {
            throw new PaymentoApiException("createPayment", "Failed to parse response", e);
        }
    }

    @Override
    public PaymentoVerifyResponse verifyPayment(PaymentProcessor processor, String token) {
        log.debug("Verifying Paymento payment token: {}", token);

        PaymentoVerifyRequest request = PaymentoVerifyRequest.builder()
                .token(token)
                .build();

        String response = executePost(processor, PAYMENT_VERIFY_PATH, request);

        try {
            PaymentoVerifyResponse verifyResponse = objectMapper.readValue(
                    response, PaymentoVerifyResponse.class);

            if (verifyResponse.hasError()) {
                log.warn("Paymento verification failed for token {}: {}", token, verifyResponse.getError());
            } else {
                log.info("Paymento payment verified for token: {}", token);
            }

            return verifyResponse;
        } catch (JsonProcessingException e) {
            throw new PaymentoApiException("verifyPayment", "Failed to parse response", e);
        }
    }

    @Override
    public List<PaymentoCoinDto> getCoins(PaymentProcessor processor) {
        log.debug("Fetching available coins from Paymento");

        String response = executeGet(processor, PAYMENT_COINS_PATH);

        try {
            JsonNode root = objectMapper.readTree(response);

            // Check for success
            if (root.has("success") && !root.get("success").asBoolean()) {
                String error = root.has("error") ? root.get("error").asText() : "Unknown error";
                throw new PaymentoApiException("getCoins", error);
            }

            // Parse body array
            if (root.has("body") && root.get("body").isArray()) {
                List<PaymentoCoinDto> coins = objectMapper.readValue(
                        root.get("body").toString(),
                        new TypeReference<List<PaymentoCoinDto>>() {});
                log.info("Fetched {} coins from Paymento", coins.size());
                return coins;
            }

            return Collections.emptyList();
        } catch (JsonProcessingException e) {
            throw new PaymentoApiException("getCoins", "Failed to parse response", e);
        }
    }

    @Override
    public PaymentoSettingsResponse configureWebhook(PaymentProcessor processor, String webhookUrl) {
        log.info("Configuring Paymento webhook URL: {}", webhookUrl);

        PaymentoSettingsRequest request = PaymentoSettingsRequest.forPostWebhook(webhookUrl);
        String response = executePost(processor, PAYMENT_SETTINGS_PATH, request);

        try {
            PaymentoSettingsResponse settingsResponse = objectMapper.readValue(
                    response, PaymentoSettingsResponse.class);

            if (settingsResponse.hasError()) {
                log.error("Failed to configure Paymento webhook: {}", settingsResponse.getError());
                throw new PaymentoApiException("configureWebhook", settingsResponse.getError());
            }

            log.info("Paymento webhook configured successfully");
            return settingsResponse;
        } catch (JsonProcessingException e) {
            throw new PaymentoApiException("configureWebhook", "Failed to parse response", e);
        }
    }

    @Override
    public PaymentoSettingsResponse getSettings(PaymentProcessor processor) {
        log.debug("Fetching Paymento settings");

        String response = executeGet(processor, PAYMENT_SETTINGS_PATH);

        try {
            return objectMapper.readValue(response, PaymentoSettingsResponse.class);
        } catch (JsonProcessingException e) {
            throw new PaymentoApiException("getSettings", "Failed to parse response", e);
        }
    }

    // ============ HTTP Methods ============

    /**
     * Executes a POST request to the Paymento API.
     */
    private String executePost(PaymentProcessor processor, String path, Object body) {
        String baseUrl = getBaseUrl(processor);
        String apiKey = getApiKey(processor);

        try {
            RestClient client = restClientBuilder.baseUrl(baseUrl).build();

            String jsonBody = objectMapper.writeValueAsString(body);
            log.trace("Paymento POST {} request: {}", path, jsonBody);

            String response = client.post()
                    .uri(path)
                    .header("Api-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_PLAIN)
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new PaymentoApiException(path, "Empty response from Paymento");
            }

            log.trace("Paymento POST {} response: {}", path, response);
            return response;

        } catch (RestClientException e) {
            log.error("HTTP error calling Paymento {}: {}", path, e.getMessage());
            throw new PaymentoApiException(path, "HTTP error: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new PaymentoApiException(path, "Failed to serialize request", e);
        }
    }

    /**
     * Executes a GET request to the Paymento API.
     */
    private String executeGet(PaymentProcessor processor, String path) {
        String baseUrl = getBaseUrl(processor);
        String apiKey = getApiKey(processor);

        try {
            RestClient client = restClientBuilder.baseUrl(baseUrl).build();

            String response = client.get()
                    .uri(path)
                    .header("Api-Key", apiKey)
                    .accept(MediaType.TEXT_PLAIN)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new PaymentoApiException(path, "Empty response from Paymento");
            }

            log.trace("Paymento GET {} response: {}", path, response);
            return response;

        } catch (RestClientException e) {
            log.error("HTTP error calling Paymento {}: {}", path, e.getMessage());
            throw new PaymentoApiException(path, "HTTP error: " + e.getMessage(), e);
        }
    }

    // ============ Configuration Helpers ============

    /**
     * Gets the API base URL from processor config or defaults.
     */
    private String getBaseUrl(PaymentProcessor processor) {
        if (processor.getConfigJson() != null && !processor.getConfigJson().isBlank()) {
            try {
                JsonNode config = objectMapper.readTree(processor.getConfigJson());
                if (config.has("baseUrl") && !config.get("baseUrl").asText().isBlank()) {
                    return config.get("baseUrl").asText();
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse processor config_json, using default baseUrl");
            }
        }
        return paymentoConfig.baseUrl();
    }

    /**
     * Gets the API key from config or processor.
     * Environment variables take priority over database values for security.
     */
    private String getApiKey(PaymentProcessor processor) {
        // SECURITY: Environment variable takes priority
        if (paymentoConfig.apiKey() != null && !paymentoConfig.apiKey().isBlank()) {
            return paymentoConfig.apiKey();
        }
        // Fall back to database (for admin-configured values)
        return processor.getApiKey();
    }

    /**
     * Gets the return URL from processor config or defaults.
     */
    private String getReturnUrl(PaymentProcessor processor) {
        if (processor.getConfigJson() != null && !processor.getConfigJson().isBlank()) {
            try {
                JsonNode config = objectMapper.readTree(processor.getConfigJson());
                if (config.has("returnUrl") && !config.get("returnUrl").asText().isBlank()) {
                    return config.get("returnUrl").asText();
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse processor config_json, using default returnUrl");
            }
        }
        return paymentoConfig.returnUrl();
    }

    /**
     * Gets the transaction speed setting from processor config or defaults.
     */
    private int getSpeed(PaymentProcessor processor) {
        if (processor.getConfigJson() != null && !processor.getConfigJson().isBlank()) {
            try {
                JsonNode config = objectMapper.readTree(processor.getConfigJson());
                if (config.has("speed")) {
                    return config.get("speed").asInt();
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse processor config_json, using default speed");
            }
        }
        return paymentoConfig.defaultSpeed();
    }

    /**
     * Builds additional data for the payment request.
     * Paymento expects an array of key-value pairs.
     */
    private List<PaymentoAdditionalData> buildAdditionalData(Invoice invoice) {
        return List.of(
            new PaymentoAdditionalData("user_id", invoice.getUser().getId().toString()),
            new PaymentoAdditionalData("invoice_id", invoice.getId().toString())
        );
    }
}
