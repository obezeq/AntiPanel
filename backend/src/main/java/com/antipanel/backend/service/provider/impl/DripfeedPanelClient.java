package com.antipanel.backend.service.provider.impl;

import com.antipanel.backend.dto.provider.api.*;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.exception.ProviderApiException;
import com.antipanel.backend.service.provider.ProviderApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.*;

/**
 * Implementation of ProviderApiClient for Dripfeed Panel API.
 * Handles all HTTP communication with the Dripfeed Panel SMM service.
 *
 * API Endpoint: https://dripfeedpanel.com/api/v2
 * Method: POST with form-urlencoded body
 * Response: JSON
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DripfeedPanelClient implements ProviderApiClient {

    private static final String PROVIDER_NAME = "DripfeedPanel";
    private static final String API_PATH = "/api/v2";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public List<DripfeedServiceDto> getServices(Provider provider) {
        log.debug("Fetching services from provider: {}", provider.getName());

        MultiValueMap<String, String> formData = createBaseForm(provider);
        formData.add("action", "services");

        String response = executeRequest(provider, formData, "services");

        try {
            List<DripfeedServiceDto> services = objectMapper.readValue(
                    response, new TypeReference<List<DripfeedServiceDto>>() {}
            );
            log.info("Fetched {} services from provider: {}", services.size(), provider.getName());
            return services;
        } catch (JsonProcessingException e) {
            throw new ProviderApiException(PROVIDER_NAME, "services", "Failed to parse services response", e);
        }
    }

    @Override
    public DripfeedBalanceResponse getBalance(Provider provider) {
        log.debug("Fetching balance from provider: {}", provider.getName());

        MultiValueMap<String, String> formData = createBaseForm(provider);
        formData.add("action", "balance");

        String response = executeRequest(provider, formData, "balance");

        try {
            DripfeedBalanceResponse balanceResponse = objectMapper.readValue(response, DripfeedBalanceResponse.class);

            if (balanceResponse.hasError()) {
                throw new ProviderApiException(PROVIDER_NAME, "balance", balanceResponse.getError());
            }

            log.info("Provider {} balance: {} {}", provider.getName(),
                    balanceResponse.getBalance(), balanceResponse.getCurrency());
            return balanceResponse;
        } catch (JsonProcessingException e) {
            throw new ProviderApiException(PROVIDER_NAME, "balance", "Failed to parse balance response", e);
        }
    }

    @Override
    public DripfeedOrderResponse createOrder(Provider provider, DripfeedOrderRequest request) {
        log.debug("Creating order at provider: {} for service: {}", provider.getName(), request.getServiceId());

        MultiValueMap<String, String> formData = createBaseForm(provider);
        formData.add("action", "add");
        formData.add("service", request.getServiceId().toString());
        formData.add("link", request.getLink());

        // Add quantity if present
        if (request.getQuantity() != null) {
            formData.add("quantity", request.getQuantity().toString());
        }

        // Drip-feed parameters
        if (request.getRuns() != null) {
            formData.add("runs", request.getRuns().toString());
        }
        if (request.getInterval() != null) {
            formData.add("interval", request.getInterval().toString());
        }

        // Custom comments
        if (request.getComments() != null && !request.getComments().isBlank()) {
            formData.add("comments", request.getComments());
        }

        // Mentions
        if (request.getUsernames() != null && !request.getUsernames().isBlank()) {
            formData.add("usernames", request.getUsernames());
        }

        // Keywords
        if (request.getKeywords() != null && !request.getKeywords().isBlank()) {
            formData.add("keywords", request.getKeywords());
        }

        // Hashtag mentions
        if (request.getHashtag() != null && !request.getHashtag().isBlank()) {
            formData.add("hashtag", request.getHashtag());
        }

        // Subscription parameters
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            formData.add("username", request.getUsername());
        }
        if (request.getMin() != null) {
            formData.add("min", request.getMin().toString());
        }
        if (request.getMax() != null) {
            formData.add("max", request.getMax().toString());
        }
        if (request.getPosts() != null) {
            formData.add("posts", request.getPosts().toString());
        }
        if (request.getOldPosts() != null) {
            formData.add("old_posts", request.getOldPosts().toString());
        }
        if (request.getDelay() != null) {
            formData.add("delay", request.getDelay().toString());
        }
        if (request.getExpiry() != null && !request.getExpiry().isBlank()) {
            formData.add("expiry", request.getExpiry());
        }

        // Web traffic parameters
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            formData.add("country", request.getCountry());
        }
        if (request.getDevice() != null) {
            formData.add("device", request.getDevice().toString());
        }
        if (request.getTypeOfTraffic() != null) {
            formData.add("type_of_traffic", request.getTypeOfTraffic().toString());
        }
        if (request.getGoogleKeyword() != null && !request.getGoogleKeyword().isBlank()) {
            formData.add("google_keyword", request.getGoogleKeyword());
        }
        if (request.getReferringUrl() != null && !request.getReferringUrl().isBlank()) {
            formData.add("referring_url", request.getReferringUrl());
        }

        // Poll parameters
        if (request.getAnswerNumber() != null) {
            formData.add("answer_number", request.getAnswerNumber().toString());
        }

        // Group parameters
        if (request.getGroups() != null && !request.getGroups().isBlank()) {
            formData.add("groups", request.getGroups());
        }

        // Media likers
        if (request.getMedia() != null && !request.getMedia().isBlank()) {
            formData.add("media", request.getMedia());
        }

        String response = executeRequest(provider, formData, "add");

        try {
            DripfeedOrderResponse orderResponse = objectMapper.readValue(response, DripfeedOrderResponse.class);

            if (!orderResponse.isSuccess()) {
                throw new ProviderApiException(PROVIDER_NAME, "add",
                        orderResponse.getError() != null ? orderResponse.getError() : "Unknown error");
            }

            log.info("Created order {} at provider: {}", orderResponse.getOrder(), provider.getName());
            return orderResponse;
        } catch (JsonProcessingException e) {
            throw new ProviderApiException(PROVIDER_NAME, "add", "Failed to parse order response", e);
        }
    }

    @Override
    public DripfeedStatusResponse getOrderStatus(Provider provider, String providerOrderId) {
        log.debug("Fetching order status from provider: {} for order: {}", provider.getName(), providerOrderId);

        MultiValueMap<String, String> formData = createBaseForm(provider);
        formData.add("action", "status");
        formData.add("order", providerOrderId);

        String response = executeRequest(provider, formData, "status");

        try {
            DripfeedStatusResponse statusResponse = objectMapper.readValue(response, DripfeedStatusResponse.class);

            if (statusResponse.hasError()) {
                throw new ProviderApiException(PROVIDER_NAME, "status", statusResponse.getError());
            }

            log.debug("Order {} status: {}", providerOrderId, statusResponse.getStatus());
            return statusResponse;
        } catch (JsonProcessingException e) {
            throw new ProviderApiException(PROVIDER_NAME, "status", "Failed to parse status response", e);
        }
    }

    @Override
    public Map<String, DripfeedStatusResponse> getMultipleOrderStatus(Provider provider, List<String> providerOrderIds) {
        if (providerOrderIds == null || providerOrderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        if (providerOrderIds.size() > 100) {
            throw new ProviderApiException(PROVIDER_NAME, "status", "Maximum 100 orders per request");
        }

        log.debug("Fetching {} order statuses from provider: {}", providerOrderIds.size(), provider.getName());

        MultiValueMap<String, String> formData = createBaseForm(provider);
        formData.add("action", "status");
        formData.add("orders", String.join(",", providerOrderIds));

        String response = executeRequest(provider, formData, "status");

        try {
            Map<String, DripfeedStatusResponse> statusMap = objectMapper.readValue(
                    response, new TypeReference<Map<String, DripfeedStatusResponse>>() {}
            );

            log.debug("Fetched {} order statuses from provider: {}", statusMap.size(), provider.getName());
            return statusMap;
        } catch (JsonProcessingException e) {
            throw new ProviderApiException(PROVIDER_NAME, "status", "Failed to parse multi-status response", e);
        }
    }

    @Override
    public DripfeedRefillResponse requestRefill(Provider provider, String providerOrderId) {
        log.debug("Requesting refill from provider: {} for order: {}", provider.getName(), providerOrderId);

        MultiValueMap<String, String> formData = createBaseForm(provider);
        formData.add("action", "refill");
        formData.add("order", providerOrderId);

        String response = executeRequest(provider, formData, "refill");

        try {
            DripfeedRefillResponse refillResponse = objectMapper.readValue(response, DripfeedRefillResponse.class);

            if (!refillResponse.isSuccess()) {
                throw new ProviderApiException(PROVIDER_NAME, "refill",
                        refillResponse.getError() != null ? refillResponse.getError() : "Unknown error");
            }

            log.info("Refill {} requested for order {} at provider: {}",
                    refillResponse.getRefill(), providerOrderId, provider.getName());
            return refillResponse;
        } catch (JsonProcessingException e) {
            throw new ProviderApiException(PROVIDER_NAME, "refill", "Failed to parse refill response", e);
        }
    }

    @Override
    public DripfeedRefillStatusResponse getRefillStatus(Provider provider, String refillId) {
        log.debug("Fetching refill status from provider: {} for refill: {}", provider.getName(), refillId);

        MultiValueMap<String, String> formData = createBaseForm(provider);
        formData.add("action", "refill_status");
        formData.add("refill", refillId);

        String response = executeRequest(provider, formData, "refill_status");

        try {
            DripfeedRefillStatusResponse statusResponse = objectMapper.readValue(
                    response, DripfeedRefillStatusResponse.class
            );

            if (statusResponse.hasError()) {
                throw new ProviderApiException(PROVIDER_NAME, "refill_status", statusResponse.getError());
            }

            log.debug("Refill {} status: {}", refillId, statusResponse.getStatus());
            return statusResponse;
        } catch (JsonProcessingException e) {
            throw new ProviderApiException(PROVIDER_NAME, "refill_status", "Failed to parse refill status response", e);
        }
    }

    @Override
    public List<DripfeedCancelResponse> cancelOrders(Provider provider, List<String> providerOrderIds) {
        if (providerOrderIds == null || providerOrderIds.isEmpty()) {
            return Collections.emptyList();
        }

        if (providerOrderIds.size() > 100) {
            throw new ProviderApiException(PROVIDER_NAME, "cancel", "Maximum 100 orders per request");
        }

        log.debug("Cancelling {} orders at provider: {}", providerOrderIds.size(), provider.getName());

        MultiValueMap<String, String> formData = createBaseForm(provider);
        formData.add("action", "cancel");
        formData.add("orders", String.join(",", providerOrderIds));

        String response = executeRequest(provider, formData, "cancel");

        try {
            List<DripfeedCancelResponse> cancelResponses = objectMapper.readValue(
                    response, new TypeReference<List<DripfeedCancelResponse>>() {}
            );

            long successCount = cancelResponses.stream().filter(DripfeedCancelResponse::isSuccess).count();
            log.info("Cancelled {}/{} orders at provider: {}", successCount, providerOrderIds.size(), provider.getName());

            return cancelResponses;
        } catch (JsonProcessingException e) {
            throw new ProviderApiException(PROVIDER_NAME, "cancel", "Failed to parse cancel response", e);
        }
    }

    /**
     * Creates the base form data with API key.
     */
    private MultiValueMap<String, String> createBaseForm(Provider provider) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("key", provider.getApiKey());
        return formData;
    }

    /**
     * Executes an HTTP POST request to the provider API.
     */
    private String executeRequest(Provider provider, MultiValueMap<String, String> formData, String action) {
        String baseUrl = normalizeApiUrl(provider.getApiUrl());

        try {
            RestClient client = restClientBuilder.baseUrl(baseUrl).build();

            String response = client.post()
                    .uri(API_PATH)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new ProviderApiException(PROVIDER_NAME, action, "Empty response from provider");
            }

            log.trace("Provider {} response for {}: {}", provider.getName(), action, response);
            return response;

        } catch (RestClientException e) {
            log.error("HTTP error calling provider {} action {}: {}", provider.getName(), action, e.getMessage());
            throw new ProviderApiException(PROVIDER_NAME, action, "HTTP error: " + e.getMessage(), e);
        }
    }

    /**
     * Normalizes the API URL to ensure it doesn't end with /api/v2.
     */
    private String normalizeApiUrl(String apiUrl) {
        if (apiUrl == null) {
            return "";
        }
        // Remove trailing slash
        String normalized = apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
        // Remove /api/v2 suffix if present (we add it ourselves)
        if (normalized.endsWith("/api/v2")) {
            normalized = normalized.substring(0, normalized.length() - 7);
        }
        if (normalized.endsWith("/api")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        return normalized;
    }
}
