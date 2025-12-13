package com.antipanel.backend.repository;

import com.antipanel.backend.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Provider entity.
 * Handles database operations for external SMM service providers.
 */
@Repository
public interface ProviderRepository extends JpaRepository<Provider, Integer> {

    // ============ ACTIVE PROVIDERS ============

    /**
     * Find all active providers sorted by name
     *
     * @return List of active providers
     */
    List<Provider> findByIsActiveTrueOrderByNameAsc();

    /**
     * Find all active providers with explicit query
     *
     * @return List of active providers
     */
    @Query("SELECT p FROM Provider p WHERE p.isActive = true ORDER BY p.name ASC")
    List<Provider> findAllActiveProviders();

    // ============ NAME-BASED ============

    /**
     * Find provider by name
     *
     * @param name Provider name
     * @return Optional provider
     */
    Optional<Provider> findByName(String name);

    /**
     * Check if provider exists by name
     *
     * @param name Provider name
     * @return true if exists
     */
    boolean existsByName(String name);

    // ============ BALANCE MONITORING ============

    /**
     * Find active providers with balance below threshold
     *
     * @param threshold Minimum balance threshold
     * @return List of providers with low balance
     */
    @Query("SELECT p FROM Provider p WHERE p.balance IS NOT NULL AND p.balance < :threshold " +
           "AND p.isActive = true ORDER BY p.balance ASC")
    List<Provider> findProvidersWithLowBalance(@Param("threshold") BigDecimal threshold);

    /**
     * Find active providers without balance tracking
     *
     * @return List of providers with null balance
     */
    @Query("SELECT p FROM Provider p WHERE p.balance IS NULL AND p.isActive = true")
    List<Provider> findProvidersWithoutBalance();

    // ============ WITH SERVICE COUNT ============

    /**
     * Find active providers with count of active provider services
     * Returns Object[] with [Provider, Long serviceCount]
     *
     * @return List of Object arrays containing provider and service count
     */
    @Query("SELECT p, COUNT(ps) as serviceCount FROM Provider p " +
           "LEFT JOIN ProviderService ps ON ps.provider = p AND ps.isActive = true " +
           "WHERE p.isActive = true " +
           "GROUP BY p " +
           "ORDER BY p.name ASC")
    List<Object[]> findActiveProvidersWithServiceCount();

    // ============ STATISTICS ============

    /**
     * Count active providers
     *
     * @return Number of active providers
     */
    @Query("SELECT COUNT(p) FROM Provider p WHERE p.isActive = true")
    long countActiveProviders();

    /**
     * Calculate total balance across all active providers
     *
     * @return Sum of all provider balances
     */
    @Query("SELECT SUM(p.balance) FROM Provider p WHERE p.isActive = true AND p.balance IS NOT NULL")
    BigDecimal getTotalProviderBalance();
}
