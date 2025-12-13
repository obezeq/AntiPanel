package com.antipanel.backend.repository;

import com.antipanel.backend.entity.ProviderService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ProviderService entity.
 * Handles database operations for services offered by external providers.
 */
@Repository
public interface ProviderServiceRepository extends JpaRepository<ProviderService, Integer> {

    // ============ BY PROVIDER ============

    /**
     * Find active provider services by provider, sorted by name
     *
     * @param providerId Provider ID
     * @return List of active provider services
     */
    List<ProviderService> findByProviderIdAndIsActiveTrueOrderByNameAsc(Integer providerId);

    /**
     * Find all provider services by provider, sorted by name
     *
     * @param providerId Provider ID
     * @return List of provider services
     */
    List<ProviderService> findByProviderIdOrderByNameAsc(Integer providerId);

    // ============ UNIQUE CONSTRAINT CHECK ============

    /**
     * Find provider service by provider and provider service ID
     *
     * @param providerId        Provider ID
     * @param providerServiceId Service ID in provider's system
     * @return Optional provider service
     */
    Optional<ProviderService> findByProviderIdAndProviderServiceId(
            Integer providerId, String providerServiceId);

    /**
     * Check if provider service exists by provider and provider service ID
     *
     * @param providerId        Provider ID
     * @param providerServiceId Service ID in provider's system
     * @return true if exists
     */
    boolean existsByProviderIdAndProviderServiceId(
            Integer providerId, String providerServiceId);

    // ============ ACTIVE SERVICES ============

    /**
     * Find all active provider services from active providers
     *
     * @return List of active provider services
     */
    @Query("SELECT ps FROM ProviderService ps WHERE ps.isActive = true " +
           "AND ps.provider.isActive = true ORDER BY ps.name ASC")
    List<ProviderService> findAllActiveProviderServices();

    // ============ COST ANALYSIS ============

    /**
     * Find active provider services with cost below threshold
     *
     * @param maxCost Maximum cost per K
     * @return List of provider services sorted by cost
     */
    @Query("SELECT ps FROM ProviderService ps WHERE ps.costPerK < :maxCost " +
           "AND ps.isActive = true ORDER BY ps.costPerK ASC")
    List<ProviderService> findByCostLessThan(@Param("maxCost") BigDecimal maxCost);

    /**
     * Calculate average cost per K across all active provider services
     *
     * @return Average cost per K
     */
    @Query("SELECT AVG(ps.costPerK) FROM ProviderService ps WHERE ps.isActive = true")
    BigDecimal getAverageCostPerK();

    // ============ SYNC STATUS ============

    /**
     * Find provider services that need synchronization (last synced before threshold)
     *
     * @param before Threshold timestamp
     * @return List of provider services needing sync
     */
    @Query("SELECT ps FROM ProviderService ps WHERE ps.lastSyncedAt < :before " +
           "OR ps.lastSyncedAt IS NULL ORDER BY ps.lastSyncedAt ASC NULLS FIRST")
    List<ProviderService> findServicesNeedingSync(@Param("before") LocalDateTime before);

    // ============ STATISTICS ============

    /**
     * Count active provider services by provider
     *
     * @param providerId Provider ID
     * @return Number of active provider services
     */
    @Query("SELECT COUNT(ps) FROM ProviderService ps WHERE ps.provider.id = :providerId " +
           "AND ps.isActive = true")
    long countActiveByProvider(@Param("providerId") Integer providerId);
}
