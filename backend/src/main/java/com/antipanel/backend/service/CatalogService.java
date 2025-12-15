package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.service.ServiceCreateRequest;
import com.antipanel.backend.dto.service.ServiceDetailResponse;
import com.antipanel.backend.dto.service.ServiceResponse;
import com.antipanel.backend.dto.service.ServiceSummary;
import com.antipanel.backend.dto.service.ServiceUpdateRequest;
import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Service entity operations.
 * Named CatalogService to avoid confusion with Spring's @Service annotation.
 */
public interface CatalogService {

    // ============ CRUD OPERATIONS ============

    /**
     * Create a new service.
     *
     * @param request Service creation data
     * @return Created service response
     */
    ServiceResponse create(ServiceCreateRequest request);

    /**
     * Get service by ID.
     *
     * @param id Service ID
     * @return Service response
     */
    ServiceResponse getById(Integer id);

    /**
     * Get service detail by ID (includes nested DTOs).
     *
     * @param id Service ID
     * @return Service detail response
     */
    ServiceDetailResponse getDetailById(Integer id);

    /**
     * Update service.
     *
     * @param id      Service ID
     * @param request Update data
     * @return Updated service response
     */
    ServiceResponse update(Integer id, ServiceUpdateRequest request);

    /**
     * Delete service by ID.
     *
     * @param id Service ID
     */
    void delete(Integer id);

    // ============ PUBLIC CATALOG QUERIES ============

    /**
     * Get all active services for the catalog.
     *
     * @return List of active catalog services
     */
    List<ServiceResponse> getActiveCatalogServices();

    /**
     * Get active services by category for the catalog.
     *
     * @param categoryId Category ID
     * @return List of active services in category
     */
    List<ServiceResponse> getActiveCatalogServicesByCategory(Integer categoryId);

    /**
     * Get active services by category and service type.
     *
     * @param categoryId    Category ID
     * @param serviceTypeId Service type ID
     * @return List of active services
     */
    List<ServiceResponse> getActiveCatalogServicesByCategoryAndType(Integer categoryId, Integer serviceTypeId);

    /**
     * Get paginated catalog services with optional filters.
     *
     * @param categoryId    Category ID (null for all)
     * @param serviceTypeId Service type ID (null for all)
     * @param quality       Service quality (null for all)
     * @param speed         Service speed (null for all)
     * @param pageable      Pagination parameters
     * @return Page of filtered services
     */
    PageResponse<ServiceResponse> getCatalogServicesFiltered(
            Integer categoryId,
            Integer serviceTypeId,
            ServiceQuality quality,
            ServiceSpeed speed,
            Pageable pageable);

    /**
     * Search catalog services by name or description.
     *
     * @param search   Search term
     * @param pageable Pagination parameters
     * @return Page of matching services
     */
    PageResponse<ServiceResponse> searchCatalogServices(String search, Pageable pageable);

    // ============ ADMIN LISTING ============

    /**
     * Get all services.
     *
     * @return List of all services
     */
    List<ServiceResponse> getAll();

    /**
     * Get all service summaries.
     *
     * @return List of all service summaries
     */
    List<ServiceSummary> getAllSummaries();

    /**
     * Get services by provider service ID.
     *
     * @param providerServiceId Provider service ID
     * @return List of services
     */
    List<ServiceResponse> getByProviderServiceId(Integer providerServiceId);

    /**
     * Get services by provider ID.
     *
     * @param providerId Provider ID
     * @return List of services
     */
    List<ServiceResponse> getByProviderId(Integer providerId);

    // ============ STATUS OPERATIONS ============

    /**
     * Toggle service active status.
     *
     * @param id Service ID
     * @return Updated service response
     */
    ServiceResponse toggleActive(Integer id);

    /**
     * Activate a service.
     *
     * @param id Service ID
     * @return Updated service response
     */
    ServiceResponse activate(Integer id);

    /**
     * Deactivate a service.
     *
     * @param id Service ID
     * @return Updated service response
     */
    ServiceResponse deactivate(Integer id);

    // ============ PRICE OPERATIONS ============

    /**
     * Update service price per K.
     *
     * @param id        Service ID
     * @param pricePerK New price per K
     * @return Updated service response
     */
    ServiceResponse updatePrice(Integer id, BigDecimal pricePerK);

    /**
     * Get services within price range.
     *
     * @param minPrice Minimum price per K
     * @param maxPrice Maximum price per K
     * @return List of services in price range
     */
    List<ServiceResponse> getByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    // ============ STATISTICS ============

    /**
     * Count all active services.
     *
     * @return Number of active services
     */
    long countActive();

    /**
     * Count active services by category.
     *
     * @param categoryId Category ID
     * @return Number of active services in category
     */
    long countActiveByCategory(Integer categoryId);
}
