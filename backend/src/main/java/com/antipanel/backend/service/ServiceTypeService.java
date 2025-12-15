package com.antipanel.backend.service;

import com.antipanel.backend.dto.servicetype.ServiceTypeCreateRequest;
import com.antipanel.backend.dto.servicetype.ServiceTypeResponse;
import com.antipanel.backend.dto.servicetype.ServiceTypeSummary;
import com.antipanel.backend.dto.servicetype.ServiceTypeUpdateRequest;

import java.util.List;

/**
 * Service interface for ServiceType operations.
 */
public interface ServiceTypeService {

    // ============ CRUD OPERATIONS ============

    /**
     * Create a new service type.
     *
     * @param request Service type creation data
     * @return Created service type response
     */
    ServiceTypeResponse create(ServiceTypeCreateRequest request);

    /**
     * Get service type by ID.
     *
     * @param id Service type ID
     * @return Service type response
     */
    ServiceTypeResponse getById(Integer id);

    /**
     * Get service type by category ID and slug.
     *
     * @param categoryId Category ID
     * @param slug       Service type slug
     * @return Service type response
     */
    ServiceTypeResponse getByCategoryIdAndSlug(Integer categoryId, String slug);

    /**
     * Update service type.
     *
     * @param id      Service type ID
     * @param request Update data
     * @return Updated service type response
     */
    ServiceTypeResponse update(Integer id, ServiceTypeUpdateRequest request);

    /**
     * Delete service type by ID.
     *
     * @param id Service type ID
     */
    void delete(Integer id);

    // ============ LISTING BY CATEGORY ============

    /**
     * Get all service types for a category sorted by sort order.
     *
     * @param categoryId Category ID
     * @return List of service types
     */
    List<ServiceTypeResponse> getAllByCategory(Integer categoryId);

    /**
     * Get all active service types for a category sorted by sort order.
     *
     * @param categoryId Category ID
     * @return List of active service types
     */
    List<ServiceTypeResponse> getActiveByCategory(Integer categoryId);

    /**
     * Get service types with service count for a category.
     *
     * @param categoryId Category ID
     * @return List of service types with service count
     */
    List<ServiceTypeResponse> getServiceTypesWithCountByCategory(Integer categoryId);

    /**
     * Get all service type summaries for a category.
     *
     * @param categoryId Category ID
     * @return List of service type summaries
     */
    List<ServiceTypeSummary> getSummariesByCategory(Integer categoryId);

    // ============ STATUS OPERATIONS ============

    /**
     * Toggle service type active status.
     *
     * @param id Service type ID
     * @return Updated service type response
     */
    ServiceTypeResponse toggleActive(Integer id);

    /**
     * Activate a service type.
     *
     * @param id Service type ID
     * @return Updated service type response
     */
    ServiceTypeResponse activate(Integer id);

    /**
     * Deactivate a service type.
     *
     * @param id Service type ID
     * @return Updated service type response
     */
    ServiceTypeResponse deactivate(Integer id);

    // ============ STATISTICS ============

    /**
     * Count active service types for a category.
     *
     * @param categoryId Category ID
     * @return Number of active service types
     */
    long countActiveByCategory(Integer categoryId);

    // ============ VALIDATION ============

    /**
     * Check if slug already exists for a category.
     *
     * @param categoryId Category ID
     * @param slug       Slug to check
     * @return true if slug exists
     */
    boolean existsByCategoryIdAndSlug(Integer categoryId, String slug);

    /**
     * Check if slug already exists for a category excluding a specific service type.
     *
     * @param categoryId    Category ID
     * @param slug          Slug to check
     * @param serviceTypeId Service type ID to exclude
     * @return true if slug exists for another service type
     */
    boolean existsByCategoryIdAndSlugExcluding(Integer categoryId, String slug, Integer serviceTypeId);
}
