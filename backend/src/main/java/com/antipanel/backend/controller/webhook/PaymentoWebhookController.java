package com.antipanel.backend.controller.webhook;

import com.antipanel.backend.config.PaymentoConfig;
import com.antipanel.backend.dto.paymento.PaymentoWebhookPayload;
import com.antipanel.backend.entity.PaymentProcessor;
import com.antipanel.backend.repository.PaymentProcessorRepository;
import com.antipanel.backend.service.payment.PaymentoWebhookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Controller for Paymento webhook (IPN) callbacks.
 * Receives payment status notifications from Paymento.
 *
 * Endpoint: POST /api/v1/webhooks/paymento
 * This endpoint is public but protected by HMAC signature verification.
 */
@RestController
@RequestMapping("/api/v1/webhooks/paymento")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Payment gateway webhook endpoints")
public class PaymentoWebhookController {

    private static final String HMAC_HEADER = "X-HMAC-SHA256-SIGNATURE";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String PAYMENTO_PROCESSOR_CODE = "paymento";

    private final PaymentoWebhookService webhookService;
    private final PaymentProcessorRepository processorRepository;
    private final PaymentoConfig paymentoConfig;
    private final ObjectMapper objectMapper;

    @PostMapping
    @Operation(
            summary = "Paymento IPN callback",
            description = "Receives payment notifications from Paymento. Validates HMAC signature before processing."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "403", description = "Invalid signature"),
            @ApiResponse(responseCode = "500", description = "Processing error")
    })
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = HMAC_HEADER, required = false) String signature,
            @RequestBody String rawPayload) {

        log.info("Received Paymento webhook callback");
        // SECURITY: Do not log signature or payload contents (sensitive data)

        try {
            // Get Paymento processor for API secret
            PaymentProcessor processor = processorRepository.findByCode(PAYMENTO_PROCESSOR_CODE)
                    .orElse(null);

            if (processor == null) {
                log.error("Paymento processor not configured in database");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Processor not configured");
            }

            // Verify HMAC signature
            // SECURITY: Environment variable takes priority over database
            String apiSecret = getApiSecret(processor);

            // SECURITY FIX: Fail-secure - reject webhooks if signature verification is not possible
            if (apiSecret == null || apiSecret.isBlank()) {
                log.error("SECURITY: No API secret configured for Paymento - rejecting webhook (fail-secure)");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Webhook verification not configured");
            }

            if (!verifySignature(rawPayload, signature, apiSecret)) {
                log.warn("Invalid webhook signature received - possible attack attempt");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Invalid signature");
            }
            log.debug("Webhook signature verified successfully");

            // Parse payload
            PaymentoWebhookPayload payload = objectMapper.readValue(
                    rawPayload, PaymentoWebhookPayload.class);

            // Process the webhook
            webhookService.processWebhook(payload);

            return ResponseEntity.ok("OK");

        } catch (JsonProcessingException e) {
            log.error("Failed to parse webhook payload: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid payload");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Processing error");
        }
    }

    /**
     * Verifies HMAC-SHA256 signature.
     * Paymento sends uppercase hexadecimal signature.
     *
     * @param payload Raw request payload
     * @param receivedSignature Signature from header
     * @param secret API secret for HMAC
     * @return true if signature is valid
     */
    private boolean verifySignature(String payload, String receivedSignature, String secret) {
        if (receivedSignature == null || receivedSignature.isBlank()) {
            log.warn("Missing signature header");
            return false;
        }

        try {
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            hmac.init(keySpec);

            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(hash).toUpperCase();

            boolean valid = calculatedSignature.equals(receivedSignature);
            if (!valid) {
                log.debug("Signature mismatch - calculated: {}, received: {}",
                        calculatedSignature, receivedSignature);
            }
            return valid;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error calculating HMAC: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Converts byte array to hexadecimal string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    /**
     * Gets API secret from config or processor.
     * Environment variables take priority over database values for security.
     */
    private String getApiSecret(PaymentProcessor processor) {
        // SECURITY: Environment variable takes priority
        if (paymentoConfig.apiSecret() != null && !paymentoConfig.apiSecret().isBlank()) {
            return paymentoConfig.apiSecret();
        }
        // Fall back to database (for admin-configured values)
        return processor.getApiSecret();
    }
}
