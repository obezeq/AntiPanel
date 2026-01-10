package com.antipanel.backend.service;

import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.providerservice.ProviderServiceResponse;

import java.util.List;

/**
 * Service interface for synchronizing data from external provider APIs.
 * Handles syncing services and balance from providers like Dripfeed Panel.
 */
public interface ProviderSyncService {

    /**
     * Synchronizes all services from a provider's API.
     * Creates new services, updates existing ones, and optionally deactivates removed ones.
     *
     * @param providerId the provider ID to sync
     * @return list of synced provider services
     */
    List<ProviderServiceResponse> syncServices(Integer providerId);

    /**
     * Synchronizes the balance from a provider's API.
     *
     * @param providerId the provider ID to sync balance for
     * @return updated provider response with new balance
     */
    ProviderResponse syncBalance(Integer providerId);

    /**
     * Synchronizes both services and balance from a provider's API.
     *
     * @param providerId the provider ID to sync
     * @return updated provider response
     */
    ProviderResponse syncAll(Integer providerId);

    /**
     * Synchronizes services from all active providers.
     *
     * @return total number of services synced
     */
    int syncAllProviderServices();

    /**
     * Synchronizes balance from all active providers.
     *
     * @return number of providers synced
     */
    int syncAllProviderBalances();
}
