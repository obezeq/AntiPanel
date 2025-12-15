package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.service.ServiceCreateRequest;
import com.antipanel.backend.dto.service.ServiceDetailResponse;
import com.antipanel.backend.dto.service.ServiceResponse;
import com.antipanel.backend.dto.service.ServiceSummary;
import com.antipanel.backend.dto.service.ServiceUpdateRequest;
import com.antipanel.backend.entity.Category;
import com.antipanel.backend.entity.ProviderService;
import com.antipanel.backend.entity.Service;
import com.antipanel.backend.entity.ServiceType;
import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.mapper.ServiceMapper;
import com.antipanel.backend.repository.CategoryRepository;
import com.antipanel.backend.repository.ProviderServiceRepository;
import com.antipanel.backend.repository.ServiceRepository;
import com.antipanel.backend.repository.ServiceTypeRepository;
import com.antipanel.backend.service.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of CatalogService.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CatalogServiceImpl implements CatalogService {

    private final ServiceRepository serviceRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final ProviderServiceRepository providerServiceRepository;
    private final ServiceMapper serviceMapper;
    private final PageMapper pageMapper;

    // ============ CRUD OPERATIONS ============

    @Override
    @Transactional
    public ServiceResponse create(ServiceCreateRequest request) {
        log.debug("Creating service with name: {}", request.getName());

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // Validate service type exists
        ServiceType serviceType = serviceTypeRepository.findById(request.getServiceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType", "id", request.getServiceTypeId()));

        // Validate provider service exists
        ProviderService providerService = providerServiceRepository.findById(request.getProviderServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("ProviderService", "id", request.getProviderServiceId()));

        Service service = serviceMapper.toEntity(request);
        service.setCategory(category);
        service.setServiceType(serviceType);
        service.setProviderService(providerService);

        Service saved = serviceRepository.save(service);
        log.info("Created service with ID: {}", saved.getId());

        return serviceMapper.enrichWithProfitMargin(saved);
    }

    @Override
    public ServiceResponse getById(Integer id) {
        log.debug("Getting service by ID: {}", id);
        Service service = findServiceById(id);
        return serviceMapper.enrichWithProfitMargin(service);
    }

    @Override
    public ServiceDetailResponse getDetailById(Integer id) {
        log.debug("Getting service detail by ID: {}", id);
        Service service = findServiceById(id);
        return serviceMapper.toDetailResponse(service);
    }

    @Override
    @Transactional
    public ServiceResponse update(Integer id, ServiceUpdateRequest request) {
        log.debug("Updating service with ID: {}", id);

        Service service = findServiceById(id);
        serviceMapper.updateEntityFromDto(request, service);

        Service saved = serviceRepository.save(service);
        log.info("Updated service with ID: {}", saved.getId());

        return serviceMapper.enrichWithProfitMargin(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.debug("Deleting service with ID: {}", id);
        Service service = findServiceById(id);
        serviceRepository.delete(service);
        log.info("Deleted service with ID: {}", id);
    }

    // ============ PUBLIC CATALOG QUERIES ============

    @Override
    public List<ServiceResponse> getActiveCatalogServices() {
        log.debug("Getting all active catalog services");
        List<Service> services = serviceRepository.findActiveCatalogServices();
        return services.stream()
                .map(serviceMapper::enrichWithProfitMargin)
                .toList();
    }

    @Override
    public List<ServiceResponse> getActiveCatalogServicesByCategory(Integer categoryId) {
        log.debug("Getting active catalog services for category ID: {}", categoryId);
        List<Service> services = serviceRepository.findActiveCatalogServicesByCategory(categoryId);
        return services.stream()
                .map(serviceMapper::enrichWithProfitMargin)
                .toList();
    }

    @Override
    public List<ServiceResponse> getActiveCatalogServicesByCategoryAndType(Integer categoryId, Integer serviceTypeId) {
        log.debug("Getting active catalog services for category ID: {} and service type ID: {}",
                categoryId, serviceTypeId);
        List<Service> services = serviceRepository.findActiveCatalogServicesByCategoryAndType(categoryId, serviceTypeId);
        return services.stream()
                .map(serviceMapper::enrichWithProfitMargin)
                .toList();
    }

    @Override
    public PageResponse<ServiceResponse> getCatalogServicesFiltered(
            Integer categoryId,
            Integer serviceTypeId,
            ServiceQuality quality,
            ServiceSpeed speed,
            Pageable pageable) {
        log.debug("Getting filtered catalog services");
        Page<Service> page = serviceRepository.findCatalogServicesFiltered(
                categoryId, serviceTypeId, quality, speed, pageable);
        List<ServiceResponse> content = page.getContent().stream()
                .map(serviceMapper::enrichWithProfitMargin)
                .toList();
        return pageMapper.toPageResponse(page, content);
    }

    @Override
    public PageResponse<ServiceResponse> searchCatalogServices(String search, Pageable pageable) {
        log.debug("Searching catalog services with term: {}", search);
        Page<Service> page = serviceRepository.searchCatalogServices(search, pageable);
        List<ServiceResponse> content = page.getContent().stream()
                .map(serviceMapper::enrichWithProfitMargin)
                .toList();
        return pageMapper.toPageResponse(page, content);
    }

    // ============ ADMIN LISTING ============

    @Override
    public List<ServiceResponse> getAll() {
        log.debug("Getting all services");
        List<Service> services = serviceRepository.findAll();
        return services.stream()
                .map(serviceMapper::enrichWithProfitMargin)
                .toList();
    }

    @Override
    public List<ServiceSummary> getAllSummaries() {
        log.debug("Getting all service summaries");
        List<Service> services = serviceRepository.findAll();
        return serviceMapper.toSummaryList(services);
    }

    @Override
    public List<ServiceResponse> getByProviderServiceId(Integer providerServiceId) {
        log.debug("Getting services for provider service ID: {}", providerServiceId);
        List<Service> services = serviceRepository.findByProviderServiceId(providerServiceId);
        return services.stream()
                .map(serviceMapper::enrichWithProfitMargin)
                .toList();
    }

    @Override
    public List<ServiceResponse> getByProviderId(Integer providerId) {
        log.debug("Getting services for provider ID: {}", providerId);
        List<Service> services = serviceRepository.findByProviderId(providerId);
        return services.stream()
                .map(serviceMapper::enrichWithProfitMargin)
                .toList();
    }

    // ============ STATUS OPERATIONS ============

    @Override
    @Transactional
    public ServiceResponse toggleActive(Integer id) {
        log.debug("Toggling active status for service ID: {}", id);
        Service service = findServiceById(id);
        service.setIsActive(!service.getIsActive());
        Service saved = serviceRepository.save(service);
        log.info("Toggled active status for service ID: {} to {}", id, saved.getIsActive());
        return serviceMapper.enrichWithProfitMargin(saved);
    }

    @Override
    @Transactional
    public ServiceResponse activate(Integer id) {
        log.debug("Activating service ID: {}", id);
        Service service = findServiceById(id);
        service.setIsActive(true);
        Service saved = serviceRepository.save(service);
        log.info("Activated service ID: {}", id);
        return serviceMapper.enrichWithProfitMargin(saved);
    }

    @Override
    @Transactional
    public ServiceResponse deactivate(Integer id) {
        log.debug("Deactivating service ID: {}", id);
        Service service = findServiceById(id);
        service.setIsActive(false);
        Service saved = serviceRepository.save(service);
        log.info("Deactivated service ID: {}", id);
        return serviceMapper.enrichWithProfitMargin(saved);
    }

    // ============ PRICE OPERATIONS ============

    @Override
    @Transactional
    public ServiceResponse updatePrice(Integer id, BigDecimal pricePerK) {
        log.debug("Updating price for service ID: {} to {}", id, pricePerK);
        Service service = findServiceById(id);
        service.setPricePerK(pricePerK);
        Service saved = serviceRepository.save(service);
        log.info("Updated price for service ID: {}", id);
        return serviceMapper.enrichWithProfitMargin(saved);
    }

    @Override
    public List<ServiceResponse> getByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Getting services with price between {} and {}", minPrice, maxPrice);
        List<Service> services = serviceRepository.findByPriceRange(minPrice, maxPrice);
        return services.stream()
                .map(serviceMapper::enrichWithProfitMargin)
                .toList();
    }

    // ============ STATISTICS ============

    @Override
    public long countActive() {
        log.debug("Counting active services");
        return serviceRepository.countActiveServices();
    }

    @Override
    public long countActiveByCategory(Integer categoryId) {
        log.debug("Counting active services for category ID: {}", categoryId);
        return serviceRepository.countActiveByCategoryId(categoryId);
    }

    // ============ HELPER METHODS ============

    private Service findServiceById(Integer id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
    }
}
