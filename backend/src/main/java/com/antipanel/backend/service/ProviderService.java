package com.antipanel.backend.service;

import com.antipanel.backend.dto.provider.ProviderCreateRequest;
import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.provider.ProviderSummary;
import com.antipanel.backend.dto.provider.ProviderUpdateRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Provider operations.
 */
public interface ProviderService {

    // ============ CRUD OPERATIONS ============

    /**
     * Create a new provider.
     *
     * @param request Provider creation data
     * @return Created provider response
     */
    ProviderResponse create(ProviderCreateRequest request);

    /**
     * Get provider by ID.
     *
     * @param id Provider ID
     * @return Provider response
     */
    ProviderResponse getById(Integer id);

    /**
     * Get provider by name.
     *
     * @param name Provider name
     * @return Provider response
     */
    ProviderResponse getByName(String name);

    /**
     * Update provider.
     *
     * @param id      Provider ID
     * @param request Update data
     * @return Updated provider response
     */
    ProviderResponse update(Integer id, ProviderUpdateRequest request);

    /**
     * Delete provider by ID.
     *
     * @param id Provider ID
     */
    void delete(Integer id);

    // ============ LISTING ============

    /**
     * Get all providers.
     *
     * @return List of all providers
     */
    List<ProviderResponse> getAll();

    /**
     * Get all active providers.
     *
     * @return List of active providers
     */
    List<ProviderResponse> getAllActive();

    /**
     * Get all active providers with service count.
     *
     * @return List of active providers with service count
     */
    List<ProviderResponse> getActiveProvidersWithServiceCount();

    /**
     * Get all provider summaries.
     *
     * @return List of provider summaries
     */
    List<ProviderSummary> getAllSummaries();

    // ============ STATUS OPERATIONS ============

    /**
     * Toggle provider active status.
     *
     * @param id Provider ID
     * @return Updated provider response
     */
    ProviderResponse toggleActive(Integer id);

    /**
     * Activate a provider.
     *
     * @param id Provider ID
     * @return Updated provider response
     */
    ProviderResponse activate(Integer id);

    /**
     * Deactivate a provider.
     *
     * @param id Provider ID
     * @return Updated provider response
     */
    ProviderResponse deactivate(Integer id);

    // ============ BALANCE OPERATIONS ============

    /**
     * Update provider balance.
     *
     * @param id      Provider ID
     * @param balance New balance
     * @return Updated provider response
     */
    ProviderResponse updateBalance(Integer id, BigDecimal balance);

    /**
     * Get providers with low balance.
     *
     * @param threshold Minimum balance threshold
     * @return List of providers with low balance
     */
    List<ProviderResponse> getProvidersWithLowBalance(BigDecimal threshold);

    // ============ STATISTICS ============

    /**
     * Count active providers.
     *
     * @return Number of active providers
     */
    long countActive();

    /**
     * Get total balance across all active providers.
     *
     * @return Total balance
     */
    BigDecimal getTotalProviderBalance();

    // ============ VALIDATION ============

    /**
     * Check if name already exists.
     *
     * @param name Name to check
     * @return true if name exists
     */
    boolean existsByName(String name);
}
