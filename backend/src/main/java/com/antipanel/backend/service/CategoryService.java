package com.antipanel.backend.service;

import com.antipanel.backend.dto.category.CategoryCreateRequest;
import com.antipanel.backend.dto.category.CategoryResponse;
import com.antipanel.backend.dto.category.CategorySummary;
import com.antipanel.backend.dto.category.CategoryUpdateRequest;

import java.util.List;

/**
 * Service interface for Category operations.
 */
public interface CategoryService {

    // ============ CRUD OPERATIONS ============

    /**
     * Create a new category.
     *
     * @param request Category creation data
     * @return Created category response
     */
    CategoryResponse create(CategoryCreateRequest request);

    /**
     * Get category by ID.
     *
     * @param id Category ID
     * @return Category response
     */
    CategoryResponse getById(Integer id);

    /**
     * Get category by slug.
     *
     * @param slug Category slug
     * @return Category response
     */
    CategoryResponse getBySlug(String slug);

    /**
     * Update category.
     *
     * @param id      Category ID
     * @param request Update data
     * @return Updated category response
     */
    CategoryResponse update(Integer id, CategoryUpdateRequest request);

    /**
     * Delete category by ID.
     *
     * @param id Category ID
     */
    void delete(Integer id);

    // ============ LISTING ============

    /**
     * Get all categories sorted by sort order.
     *
     * @return List of all categories
     */
    List<CategoryResponse> getAll();

    /**
     * Get all active categories sorted by sort order.
     *
     * @return List of active categories
     */
    List<CategoryResponse> getAllActive();

    /**
     * Get all active categories with service count.
     *
     * @return List of active categories with service count
     */
    List<CategoryResponse> getActiveCategoriesWithServiceCount();

    /**
     * Get all category summaries.
     *
     * @return List of category summaries
     */
    List<CategorySummary> getAllSummaries();

    // ============ STATUS OPERATIONS ============

    /**
     * Toggle category active status.
     *
     * @param id Category ID
     * @return Updated category response
     */
    CategoryResponse toggleActive(Integer id);

    /**
     * Activate a category.
     *
     * @param id Category ID
     * @return Updated category response
     */
    CategoryResponse activate(Integer id);

    /**
     * Deactivate a category.
     *
     * @param id Category ID
     * @return Updated category response
     */
    CategoryResponse deactivate(Integer id);

    // ============ STATISTICS ============

    /**
     * Count active categories.
     *
     * @return Number of active categories
     */
    long countActive();

    // ============ VALIDATION ============

    /**
     * Check if slug already exists.
     *
     * @param slug Slug to check
     * @return true if slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Check if slug already exists excluding a specific category.
     *
     * @param slug       Slug to check
     * @param categoryId Category ID to exclude
     * @return true if slug exists for another category
     */
    boolean existsBySlugExcludingCategory(String slug, Integer categoryId);
}
