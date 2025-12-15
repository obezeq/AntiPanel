package com.antipanel.backend.repository;

import com.antipanel.backend.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ServiceType entity.
 * Handles database operations for service types within categories.
 */
@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Integer> {

    // ============ BY CATEGORY ============

    /**
     * Find active service types by category, sorted by sort order
     *
     * @param categoryId Category ID
     * @return List of active service types
     */
    List<ServiceType> findByCategoryIdAndIsActiveTrueOrderBySortOrderAsc(Integer categoryId);

    /**
     * Find all service types by category, sorted by sort order
     *
     * @param categoryId Category ID
     * @return List of service types
     */
    List<ServiceType> findByCategoryIdOrderBySortOrderAsc(Integer categoryId);

    /**
     * Find active service types by category with explicit query
     *
     * @param categoryId Category ID
     * @return List of active service types
     */
    @Query("SELECT st FROM ServiceType st WHERE st.category.id = :categoryId " +
           "AND st.isActive = true ORDER BY st.sortOrder ASC")
    List<ServiceType> findActiveServiceTypesByCategory(@Param("categoryId") Integer categoryId);

    // ============ SLUG-BASED ============

    /**
     * Find service type by category and slug
     *
     * @param categoryId Category ID
     * @param slug       Service type slug
     * @return Optional service type
     */
    Optional<ServiceType> findByCategoryIdAndSlug(Integer categoryId, String slug);

    /**
     * Check if service type exists by category and slug
     *
     * @param categoryId Category ID
     * @param slug       Service type slug
     * @return true if exists
     */
    boolean existsByCategoryIdAndSlug(Integer categoryId, String slug);

    // ============ WITH SERVICE COUNT ============

    /**
     * Find service types with count of active services for a category
     * Returns Object[] with [ServiceType, Long serviceCount]
     *
     * @param categoryId Category ID
     * @return List of Object arrays containing service type and service count
     */
    @Query("SELECT st, COUNT(s) as serviceCount FROM ServiceType st " +
           "LEFT JOIN Service s ON s.serviceType = st AND s.isActive = true " +
           "WHERE st.category.id = :categoryId AND st.isActive = true " +
           "GROUP BY st " +
           "ORDER BY st.sortOrder ASC")
    List<Object[]> findServiceTypesWithCountByCategory(@Param("categoryId") Integer categoryId);

    // ============ STATISTICS ============

    /**
     * Count active service types by category
     *
     * @param categoryId Category ID
     * @return Number of active service types
     */
    @Query("SELECT COUNT(st) FROM ServiceType st WHERE st.category.id = :categoryId AND st.isActive = true")
    long countActiveByCategory(@Param("categoryId") Integer categoryId);
}
