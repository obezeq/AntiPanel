package com.antipanel.backend.repository;

import com.antipanel.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity.
 * Handles database operations for social media platform categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // ============ BASIC FINDERS ============

    /**
     * Find category by slug
     *
     * @param slug URL-friendly slug
     * @return Optional category
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Find category by name
     *
     * @param name Category name
     * @return Optional category
     */
    Optional<Category> findByName(String name);

    /**
     * Check if category exists by slug
     *
     * @param slug URL-friendly slug
     * @return true if exists
     */
    boolean existsBySlug(String slug);

    // ============ ACTIVE CATEGORIES ============

    /**
     * Find all active categories sorted by sort order
     *
     * @return List of active categories
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * Find all active categories sorted by sort order and name
     *
     * @return List of active categories
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findAllActiveCategories();

    /**
     * Find all categories sorted by sort order and name
     *
     * @return List of all categories
     */
    @Query("SELECT c FROM Category c ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findAllCategoriesSorted();

    // ============ WITH SERVICE COUNT ============

    /**
     * Find active categories with count of active services
     * Returns Object[] with [Category, Long serviceCount]
     *
     * @return List of Object arrays containing category and service count
     */
    @Query("SELECT c, COUNT(s) as serviceCount FROM Category c " +
           "LEFT JOIN Service s ON s.category = c AND s.isActive = true " +
           "WHERE c.isActive = true " +
           "GROUP BY c " +
           "ORDER BY c.sortOrder ASC")
    List<Object[]> findActiveCategoriesWithServiceCount();

    // ============ STATISTICS ============

    /**
     * Count active categories
     *
     * @return Number of active categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.isActive = true")
    long countActiveCategories();
}
