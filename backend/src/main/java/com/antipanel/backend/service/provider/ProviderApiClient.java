package com.antipanel.backend.service.provider;

import com.antipanel.backend.dto.provider.api.*;
import com.antipanel.backend.entity.Provider;

import java.util.List;
import java.util.Map;

/**
 * Interface for external provider API clients.
 * Implementations handle communication with specific SMM panel APIs.
 */
public interface ProviderApiClient {

    /**
     * Gets all available services from the provider.
     *
     * @param provider the provider entity with API credentials
     * @return list of available services
     */
    List<DripfeedServiceDto> getServices(Provider provider);

    /**
     * Gets the current account balance.
     *
     * @param provider the provider entity with API credentials
     * @return balance response with amount and currency
     */
    DripfeedBalanceResponse getBalance(Provider provider);

    /**
     * Creates a new order at the provider.
     *
     * @param provider the provider entity with API credentials
     * @param request the order request details
     * @return order response with provider order ID
     */
    DripfeedOrderResponse createOrder(Provider provider, DripfeedOrderRequest request);

    /**
     * Gets the status of a single order.
     *
     * @param provider the provider entity with API credentials
     * @param providerOrderId the order ID in the provider system
     * @return status response with order details
     */
    DripfeedStatusResponse getOrderStatus(Provider provider, String providerOrderId);

    /**
     * Gets the status of multiple orders.
     *
     * @param provider the provider entity with API credentials
     * @param providerOrderIds list of order IDs (max 100)
     * @return map of order ID to status response
     */
    Map<String, DripfeedStatusResponse> getMultipleOrderStatus(Provider provider, List<String> providerOrderIds);

    /**
     * Requests a refill for an order.
     *
     * @param provider the provider entity with API credentials
     * @param providerOrderId the order ID to refill
     * @return refill response with refill ID
     */
    DripfeedRefillResponse requestRefill(Provider provider, String providerOrderId);

    /**
     * Gets the status of a refill request.
     *
     * @param provider the provider entity with API credentials
     * @param refillId the refill ID to check
     * @return refill status response
     */
    DripfeedRefillStatusResponse getRefillStatus(Provider provider, String refillId);

    /**
     * Cancels one or more orders.
     *
     * @param provider the provider entity with API credentials
     * @param providerOrderIds list of order IDs to cancel (max 100)
     * @return list of cancel responses
     */
    List<DripfeedCancelResponse> cancelOrders(Provider provider, List<String> providerOrderIds);
}
