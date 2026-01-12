package com.antipanel.backend.service.payment;

import com.antipanel.backend.dto.paymento.*;
import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.PaymentProcessor;

import java.util.List;

/**
 * Client interface for Paymento cryptocurrency payment gateway.
 * Handles all API communication with Paymento.
 */
public interface PaymentoClient {

    /**
     * Creates a payment invoice with Paymento.
     *
     * @param processor PaymentProcessor with API credentials
     * @param invoice Internal invoice to create payment for
     * @return Payment response with token and payment URL
     */
    PaymentoPaymentResponse createPayment(PaymentProcessor processor, Invoice invoice);

    /**
     * Verifies a payment status.
     *
     * @param processor PaymentProcessor with API credentials
     * @param token Payment token to verify
     * @return Verification response with order details
     */
    PaymentoVerifyResponse verifyPayment(PaymentProcessor processor, String token);

    /**
     * Gets available cryptocurrencies for the merchant.
     *
     * @param processor PaymentProcessor with API credentials
     * @return List of available coins
     */
    List<PaymentoCoinDto> getCoins(PaymentProcessor processor);

    /**
     * Configures webhook (IPN) URL for payment notifications.
     *
     * @param processor PaymentProcessor with API credentials
     * @param webhookUrl URL for IPN callbacks
     * @return Settings response confirming configuration
     */
    PaymentoSettingsResponse configureWebhook(PaymentProcessor processor, String webhookUrl);

    /**
     * Gets current webhook settings.
     *
     * @param processor PaymentProcessor with API credentials
     * @return Current settings
     */
    PaymentoSettingsResponse getSettings(PaymentProcessor processor);
}
