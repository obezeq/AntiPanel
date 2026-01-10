package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.provider.api.DripfeedBalanceResponse;
import com.antipanel.backend.dto.provider.api.DripfeedServiceDto;
import com.antipanel.backend.dto.providerservice.ProviderServiceCreateRequest;
import com.antipanel.backend.dto.providerservice.ProviderServiceResponse;
import com.antipanel.backend.dto.providerservice.ProviderServiceUpdateRequest;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.repository.ProviderRepository;
import com.antipanel.backend.repository.ProviderServiceRepository;
import com.antipanel.backend.service.ProviderCatalogService;
import com.antipanel.backend.service.ProviderService;
import com.antipanel.backend.service.ProviderSyncService;
import com.antipanel.backend.service.provider.ProviderApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ProviderSyncService.
 * Handles synchronization of services and balance from external provider APIs.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProviderSyncServiceImpl implements ProviderSyncService {

    private final ProviderRepository providerRepository;
    private final ProviderServiceRepository providerServiceRepository;
    private final ProviderApiClient providerApiClient;
    private final ProviderService providerService;
    private final ProviderCatalogService providerCatalogService;

    @Override
    @Transactional
    public List<ProviderServiceResponse> syncServices(Integer providerId) {
        log.info("Starting service sync for provider ID: {}", providerId);

        Provider provider = findProviderById(providerId);

        // Fetch services from provider API
        List<DripfeedServiceDto> externalServices = providerApiClient.getServices(provider);
        log.info("Fetched {} services from provider: {}", externalServices.size(), provider.getName());

        // Get existing services for this provider
        Map<String, com.antipanel.backend.entity.ProviderService> existingServices =
                providerServiceRepository.findByProviderIdOrderByNameAsc(providerId)
                        .stream()
                        .collect(Collectors.toMap(
                                com.antipanel.backend.entity.ProviderService::getProviderServiceId,
                                s -> s
                        ));

        List<ProviderServiceResponse> syncedServices = new ArrayList<>();
        Set<String> processedServiceIds = new HashSet<>();

        // Process each external service
        for (DripfeedServiceDto externalService : externalServices) {
            String serviceId = externalService.getServiceId().toString();
            processedServiceIds.add(serviceId);

            com.antipanel.backend.entity.ProviderService existingService = existingServices.get(serviceId);

            if (existingService != null) {
                // Update existing service
                ProviderServiceResponse updated = updateExistingService(existingService, externalService);
                syncedServices.add(updated);
            } else {
                // Create new service
                ProviderServiceResponse created = createNewService(providerId, externalService);
                syncedServices.add(created);
            }
        }

        // Deactivate services that no longer exist in the provider
        int deactivatedCount = deactivateRemovedServices(existingServices, processedServiceIds);
        if (deactivatedCount > 0) {
            log.info("Deactivated {} services that are no longer available from provider: {}",
                    deactivatedCount, provider.getName());
        }

        log.info("Service sync completed for provider: {}. Total: {}, New: {}, Updated: {}, Deactivated: {}",
                provider.getName(),
                syncedServices.size(),
                syncedServices.size() - (existingServices.size() - deactivatedCount),
                existingServices.size() - deactivatedCount,
                deactivatedCount);

        return syncedServices;
    }

    @Override
    @Transactional
    public ProviderResponse syncBalance(Integer providerId) {
        log.info("Starting balance sync for provider ID: {}", providerId);

        Provider provider = findProviderById(providerId);

        // Fetch balance from provider API
        DripfeedBalanceResponse balanceResponse = providerApiClient.getBalance(provider);
        BigDecimal newBalance = balanceResponse.getBalanceAsDecimal();

        // Update provider balance
        ProviderResponse response = providerService.updateBalance(providerId, newBalance);

        log.info("Balance sync completed for provider: {}. Balance: {} {}",
                provider.getName(), newBalance, balanceResponse.getCurrency());

        return response;
    }

    @Override
    @Transactional
    public ProviderResponse syncAll(Integer providerId) {
        log.info("Starting full sync for provider ID: {}", providerId);

        // Sync services first
        syncServices(providerId);

        // Then sync balance
        return syncBalance(providerId);
    }

    @Override
    @Transactional
    public int syncAllProviderServices() {
        log.info("Starting service sync for all active providers");

        List<Provider> activeProviders = providerRepository.findAllActiveProviders();
        int totalSynced = 0;

        for (Provider provider : activeProviders) {
            try {
                List<ProviderServiceResponse> synced = syncServices(provider.getId());
                totalSynced += synced.size();
            } catch (Exception e) {
                log.error("Failed to sync services for provider {}: {}", provider.getName(), e.getMessage());
            }
        }

        log.info("Completed service sync for all providers. Total services synced: {}", totalSynced);
        return totalSynced;
    }

    @Override
    @Transactional
    public int syncAllProviderBalances() {
        log.info("Starting balance sync for all active providers");

        List<Provider> activeProviders = providerRepository.findAllActiveProviders();
        int syncedCount = 0;

        for (Provider provider : activeProviders) {
            try {
                syncBalance(provider.getId());
                syncedCount++;
            } catch (Exception e) {
                log.error("Failed to sync balance for provider {}: {}", provider.getName(), e.getMessage());
            }
        }

        log.info("Completed balance sync for all providers. Synced: {}/{}", syncedCount, activeProviders.size());
        return syncedCount;
    }

    /**
     * Finds a provider by ID or throws ResourceNotFoundException.
     */
    private Provider findProviderById(Integer providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", providerId));
    }

    /**
     * Creates a new ProviderService from external service data.
     */
    private ProviderServiceResponse createNewService(Integer providerId, DripfeedServiceDto externalService) {
        ProviderServiceCreateRequest request = ProviderServiceCreateRequest.builder()
                .providerId(providerId)
                .providerServiceId(externalService.getServiceId().toString())
                .name(externalService.getName())
                .minQuantity(externalService.getMinAsInteger())
                .maxQuantity(externalService.getMaxAsInteger())
                .costPerK(externalService.getRateAsDecimal())
                .refillDays(Boolean.TRUE.equals(externalService.getRefill()) ? 30 : 0) // Default 30 days if refill supported
                .isActive(true)
                .build();

        log.debug("Creating new provider service: {} (ID: {})",
                externalService.getName(), externalService.getServiceId());

        return providerCatalogService.create(request);
    }

    /**
     * Updates an existing ProviderService with external service data.
     */
    private ProviderServiceResponse updateExistingService(
            com.antipanel.backend.entity.ProviderService existingService,
            DripfeedServiceDto externalService) {

        ProviderServiceUpdateRequest request = ProviderServiceUpdateRequest.builder()
                .name(externalService.getName())
                .minQuantity(externalService.getMinAsInteger())
                .maxQuantity(externalService.getMaxAsInteger())
                .costPerK(externalService.getRateAsDecimal())
                .refillDays(Boolean.TRUE.equals(externalService.getRefill()) ?
                        (existingService.getRefillDays() > 0 ? existingService.getRefillDays() : 30) : 0)
                .isActive(true) // Re-activate if it was deactivated
                .build();

        log.debug("Updating provider service: {} (ID: {})",
                externalService.getName(), externalService.getServiceId());

        // Update and mark as synced
        ProviderServiceResponse updated = providerCatalogService.update(existingService.getId(), request);
        providerCatalogService.updateLastSynced(existingService.getId());

        return updated;
    }

    /**
     * Deactivates services that are no longer available from the provider.
     */
    private int deactivateRemovedServices(
            Map<String, com.antipanel.backend.entity.ProviderService> existingServices,
            Set<String> processedServiceIds) {

        int deactivatedCount = 0;

        for (Map.Entry<String, com.antipanel.backend.entity.ProviderService> entry : existingServices.entrySet()) {
            if (!processedServiceIds.contains(entry.getKey()) && entry.getValue().getIsActive()) {
                providerCatalogService.deactivate(entry.getValue().getId());
                deactivatedCount++;
                log.debug("Deactivated removed service: {} (ID: {})",
                        entry.getValue().getName(), entry.getKey());
            }
        }

        return deactivatedCount;
    }
}
