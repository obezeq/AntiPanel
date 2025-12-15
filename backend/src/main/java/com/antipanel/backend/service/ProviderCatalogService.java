package com.antipanel.backend.service;

import com.antipanel.backend.dto.providerservice.ProviderServiceCreateRequest;
import com.antipanel.backend.dto.providerservice.ProviderServiceResponse;
import com.antipanel.backend.dto.providerservice.ProviderServiceSummary;
import com.antipanel.backend.dto.providerservice.ProviderServiceUpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for ProviderService entity operations.
 * Named ProviderCatalogService to avoid confusion with ProviderService interface.
 */
public interface ProviderCatalogService {

    // ============ CRUD OPERATIONS ============

    /**
     * Create a new provider service.
     *
     * @param request Provider service creation data
     * @return Created provider service response
     */
    ProviderServiceResponse create(ProviderServiceCreateRequest request);

    /**
     * Get provider service by ID.
     *
     * @param id Provider service ID
     * @return Provider service response
     */
    ProviderServiceResponse getById(Integer id);

    /**
     * Get provider service by provider ID and provider service ID.
     *
     * @param providerId        Provider ID
     * @param providerServiceId Service ID in provider's system
     * @return Provider service response
     */
    ProviderServiceResponse getByProviderIdAndServiceId(Integer providerId, String providerServiceId);

    /**
     * Update provider service.
     *
     * @param id      Provider service ID
     * @param request Update data
     * @return Updated provider service response
     */
    ProviderServiceResponse update(Integer id, ProviderServiceUpdateRequest request);

    /**
     * Delete provider service by ID.
     *
     * @param id Provider service ID
     */
    void delete(Integer id);

    // ============ LISTING BY PROVIDER ============

    /**
     * Get all provider services for a provider.
     *
     * @param providerId Provider ID
     * @return List of provider services
     */
    List<ProviderServiceResponse> getAllByProvider(Integer providerId);

    /**
     * Get all active provider services for a provider.
     *
     * @param providerId Provider ID
     * @return List of active provider services
     */
    List<ProviderServiceResponse> getActiveByProvider(Integer providerId);

    /**
     * Get all active provider services from active providers.
     *
     * @return List of all active provider services
     */
    List<ProviderServiceResponse> getAllActive();

    /**
     * Get all provider service summaries for a provider.
     *
     * @param providerId Provider ID
     * @return List of provider service summaries
     */
    List<ProviderServiceSummary> getSummariesByProvider(Integer providerId);

    // ============ STATUS OPERATIONS ============

    /**
     * Toggle provider service active status.
     *
     * @param id Provider service ID
     * @return Updated provider service response
     */
    ProviderServiceResponse toggleActive(Integer id);

    /**
     * Activate a provider service.
     *
     * @param id Provider service ID
     * @return Updated provider service response
     */
    ProviderServiceResponse activate(Integer id);

    /**
     * Deactivate a provider service.
     *
     * @param id Provider service ID
     * @return Updated provider service response
     */
    ProviderServiceResponse deactivate(Integer id);

    // ============ COST OPERATIONS ============

    /**
     * Update cost per K for a provider service.
     *
     * @param id      Provider service ID
     * @param costPerK New cost per K
     * @return Updated provider service response
     */
    ProviderServiceResponse updateCost(Integer id, BigDecimal costPerK);

    /**
     * Get provider services with cost below threshold.
     *
     * @param maxCost Maximum cost per K
     * @return List of provider services sorted by cost
     */
    List<ProviderServiceResponse> getByCostLessThan(BigDecimal maxCost);

    /**
     * Get average cost per K across all active provider services.
     *
     * @return Average cost per K
     */
    BigDecimal getAverageCostPerK();

    // ============ SYNC OPERATIONS ============

    /**
     * Update last synced timestamp.
     *
     * @param id Provider service ID
     * @return Updated provider service response
     */
    ProviderServiceResponse updateLastSynced(Integer id);

    /**
     * Get provider services that need synchronization.
     *
     * @param before Threshold timestamp
     * @return List of provider services needing sync
     */
    List<ProviderServiceResponse> getServicesNeedingSync(LocalDateTime before);

    // ============ STATISTICS ============

    /**
     * Count active provider services for a provider.
     *
     * @param providerId Provider ID
     * @return Number of active provider services
     */
    long countActiveByProvider(Integer providerId);

    // ============ VALIDATION ============

    /**
     * Check if provider service ID already exists for a provider.
     *
     * @param providerId        Provider ID
     * @param providerServiceId Service ID in provider's system
     * @return true if exists
     */
    boolean existsByProviderIdAndServiceId(Integer providerId, String providerServiceId);
}
