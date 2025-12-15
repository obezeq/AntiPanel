package com.antipanel.backend.repository;

import com.antipanel.backend.entity.Service;
import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Service entity.
 * PERFORMANCE CRITICAL - Powers the public service catalog.
 * All queries are optimized with indexed columns.
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {

    // ============ PUBLIC CATALOG QUERIES (PERFORMANCE CRITICAL) ============

    /**
     * Get active services for catalog display.
     * Filters out inactive services, categories, and service types.
     * Optimized for fast page load.
     *
     * @return List of active services sorted by sort order
     */
    @Query("SELECT s FROM Service s " +
           "WHERE s.isActive = true " +
           "AND s.category.isActive = true " +
           "AND s.serviceType.isActive = true " +
           "ORDER BY s.sortOrder ASC, s.name ASC")
    List<Service> findActiveCatalogServices();

    /**
     * Get active services by category for catalog
     *
     * @param categoryId Category ID
     * @return List of active services in category
     */
    @Query("SELECT s FROM Service s " +
           "WHERE s.category.id = :categoryId " +
           "AND s.isActive = true " +
           "AND s.category.isActive = true " +
           "AND s.serviceType.isActive = true " +
           "ORDER BY s.sortOrder ASC, s.name ASC")
    List<Service> findActiveCatalogServicesByCategory(@Param("categoryId") Integer categoryId);

    /**
     * Get active services by category and service type
     *
     * @param categoryId    Category ID
     * @param serviceTypeId Service type ID
     * @return List of active services
     */
    @Query("SELECT s FROM Service s " +
           "WHERE s.category.id = :categoryId " +
           "AND s.serviceType.id = :serviceTypeId " +
           "AND s.isActive = true " +
           "AND s.category.isActive = true " +
           "AND s.serviceType.isActive = true " +
           "ORDER BY s.sortOrder ASC, s.name ASC")
    List<Service> findActiveCatalogServicesByCategoryAndType(
            @Param("categoryId") Integer categoryId,
            @Param("serviceTypeId") Integer serviceTypeId);

    /**
     * Paginated catalog services with optional filters
     *
     * @param categoryId    Category ID (null for all)
     * @param serviceTypeId Service type ID (null for all)
     * @param quality       Service quality (null for all)
     * @param speed         Service speed (null for all)
     * @param pageable      Pagination parameters
     * @return Page of filtered services
     */
    @Query("SELECT s FROM Service s " +
           "WHERE s.isActive = true " +
           "AND s.category.isActive = true " +
           "AND (:categoryId IS NULL OR s.category.id = :categoryId) " +
           "AND (:serviceTypeId IS NULL OR s.serviceType.id = :serviceTypeId) " +
           "AND (:quality IS NULL OR s.quality = :quality) " +
           "AND (:speed IS NULL OR s.speed = :speed) " +
           "ORDER BY s.sortOrder ASC, s.name ASC")
    Page<Service> findCatalogServicesFiltered(
            @Param("categoryId") Integer categoryId,
            @Param("serviceTypeId") Integer serviceTypeId,
            @Param("quality") ServiceQuality quality,
            @Param("speed") ServiceSpeed speed,
            Pageable pageable);

    /**
     * Search services by name or description (case-insensitive)
     *
     * @param search   Search term
     * @param pageable Pagination parameters
     * @return Page of matching services
     */
    @Query("SELECT s FROM Service s " +
           "WHERE s.isActive = true " +
           "AND s.category.isActive = true " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY s.sortOrder ASC, s.name ASC")
    Page<Service> searchCatalogServices(@Param("search") String search, Pageable pageable);

    // ============ ADMIN QUERIES ============

    /**
     * Find services by provider service
     *
     * @param providerServiceId Provider service ID
     * @return List of services
     */
    List<Service> findByProviderServiceId(Integer providerServiceId);

    /**
     * Find services by provider
     *
     * @param providerId Provider ID
     * @return List of services
     */
    @Query("SELECT s FROM Service s WHERE s.providerService.provider.id = :providerId")
    List<Service> findByProviderId(@Param("providerId") Integer providerId);

    /**
     * Find services with low profit margin
     * Returns Object[] with [Service, BigDecimal costPerK, BigDecimal profit]
     *
     * @param minMargin Minimum profit margin percentage
     * @return List of services with low profit margin
     */
    @Query("SELECT s, ps.costPerK, (s.pricePerK - ps.costPerK) as profit FROM Service s " +
           "JOIN s.providerService ps " +
           "WHERE s.isActive = true " +
           "AND ((s.pricePerK - ps.costPerK) / s.pricePerK * 100) < :minMargin " +
           "ORDER BY profit ASC")
    List<Object[]> findServicesWithLowProfitMargin(@Param("minMargin") BigDecimal minMargin);

    // ============ PRICE ANALYSIS ============

    /**
     * Get price range (min, max, avg) for a category
     * Returns Object[] with [BigDecimal min, BigDecimal max, BigDecimal avg]
     *
     * @param categoryId Category ID
     * @return Array containing min, max, and average price
     */
    @Query("SELECT MIN(s.pricePerK), MAX(s.pricePerK), AVG(s.pricePerK) " +
           "FROM Service s WHERE s.category.id = :categoryId AND s.isActive = true")
    Object[] getPriceRangeByCategory(@Param("categoryId") Integer categoryId);

    /**
     * Find services within price range
     *
     * @param minPrice Minimum price per K
     * @param maxPrice Maximum price per K
     * @return List of services in price range
     */
    @Query("SELECT s FROM Service s " +
           "WHERE s.isActive = true " +
           "AND s.pricePerK BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY s.pricePerK ASC")
    List<Service> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    // ============ STATISTICS ============

    /**
     * Count all active services
     *
     * @return Number of active services
     */
    @Query("SELECT COUNT(s) FROM Service s WHERE s.isActive = true")
    long countActiveServices();

    /**
     * Count active services by category
     *
     * @param categoryId Category ID
     * @return Number of active services in category
     */
    @Query("SELECT COUNT(s) FROM Service s WHERE s.category.id = :categoryId AND s.isActive = true")
    long countActiveByCategoryId(@Param("categoryId") Integer categoryId);

    /**
     * Count services grouped by category
     * Returns Object[] with [String categoryName, Long count]
     *
     * @return List of category names and service counts
     */
    @Query("SELECT s.category.name, COUNT(s) FROM Service s " +
           "WHERE s.isActive = true " +
           "GROUP BY s.category.name " +
           "ORDER BY COUNT(s) DESC")
    List<Object[]> countServicesByCategory();

    /**
     * Count services grouped by quality
     * Returns Object[] with [ServiceQuality quality, Long count]
     *
     * @return List of quality levels and service counts
     */
    @Query("SELECT s.quality, COUNT(s) FROM Service s " +
           "WHERE s.isActive = true " +
           "GROUP BY s.quality")
    List<Object[]> countServicesByQuality();

    /**
     * Count services grouped by speed
     * Returns Object[] with [ServiceSpeed speed, Long count]
     *
     * @return List of speed levels and service counts
     */
    @Query("SELECT s.speed, COUNT(s) FROM Service s " +
           "WHERE s.isActive = true " +
           "GROUP BY s.speed")
    List<Object[]> countServicesBySpeed();
}
