package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.servicetype.ServiceTypeCreateRequest;
import com.antipanel.backend.dto.servicetype.ServiceTypeResponse;
import com.antipanel.backend.dto.servicetype.ServiceTypeSummary;
import com.antipanel.backend.dto.servicetype.ServiceTypeUpdateRequest;
import com.antipanel.backend.entity.Category;
import com.antipanel.backend.entity.ServiceType;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.ServiceTypeMapper;
import com.antipanel.backend.repository.CategoryRepository;
import com.antipanel.backend.repository.ServiceTypeRepository;
import com.antipanel.backend.service.ServiceTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ServiceTypeService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceTypeMapper serviceTypeMapper;

    // ============ CRUD OPERATIONS ============

    @Override
    @Transactional
    public ServiceTypeResponse create(ServiceTypeCreateRequest request) {
        log.debug("Creating service type with name: {} for category ID: {}", request.getName(), request.getCategoryId());

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // Check slug uniqueness within category
        if (serviceTypeRepository.existsByCategoryIdAndSlug(request.getCategoryId(), request.getSlug())) {
            throw new ConflictException("Service type slug already exists in this category: " + request.getSlug());
        }

        ServiceType serviceType = serviceTypeMapper.toEntity(request);
        serviceType.setCategory(category);

        ServiceType saved = serviceTypeRepository.save(serviceType);
        log.info("Created service type with ID: {}", saved.getId());

        return serviceTypeMapper.toResponse(saved);
    }

    @Override
    public ServiceTypeResponse getById(Integer id) {
        log.debug("Getting service type by ID: {}", id);
        ServiceType serviceType = findServiceTypeById(id);
        return serviceTypeMapper.toResponse(serviceType);
    }

    @Override
    public ServiceTypeResponse getByCategoryIdAndSlug(Integer categoryId, String slug) {
        log.debug("Getting service type by category ID: {} and slug: {}", categoryId, slug);
        ServiceType serviceType = serviceTypeRepository.findByCategoryIdAndSlug(categoryId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType", "slug", slug));
        return serviceTypeMapper.toResponse(serviceType);
    }

    @Override
    @Transactional
    public ServiceTypeResponse update(Integer id, ServiceTypeUpdateRequest request) {
        log.debug("Updating service type with ID: {}", id);

        ServiceType serviceType = findServiceTypeById(id);
        serviceTypeMapper.updateEntityFromDto(request, serviceType);

        ServiceType saved = serviceTypeRepository.save(serviceType);
        log.info("Updated service type with ID: {}", saved.getId());

        return serviceTypeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.debug("Deleting service type with ID: {}", id);
        ServiceType serviceType = findServiceTypeById(id);
        serviceTypeRepository.delete(serviceType);
        log.info("Deleted service type with ID: {}", id);
    }

    // ============ LISTING BY CATEGORY ============

    @Override
    public List<ServiceTypeResponse> getAllByCategory(Integer categoryId) {
        log.debug("Getting all service types for category ID: {}", categoryId);
        List<ServiceType> serviceTypes = serviceTypeRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
        return serviceTypeMapper.toResponseList(serviceTypes);
    }

    @Override
    public List<ServiceTypeResponse> getActiveByCategory(Integer categoryId) {
        log.debug("Getting active service types for category ID: {}", categoryId);
        List<ServiceType> serviceTypes = serviceTypeRepository.findActiveServiceTypesByCategory(categoryId);
        return serviceTypeMapper.toResponseList(serviceTypes);
    }

    @Override
    public List<ServiceTypeResponse> getServiceTypesWithCountByCategory(Integer categoryId) {
        log.debug("Getting service types with service count for category ID: {}", categoryId);
        List<Object[]> results = serviceTypeRepository.findServiceTypesWithCountByCategory(categoryId);
        return results.stream()
                .map(row -> {
                    ServiceType serviceType = (ServiceType) row[0];
                    Long serviceCount = (Long) row[1];
                    return serviceTypeMapper.toResponseWithCount(serviceType, serviceCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceTypeSummary> getSummariesByCategory(Integer categoryId) {
        log.debug("Getting service type summaries for category ID: {}", categoryId);
        List<ServiceType> serviceTypes = serviceTypeRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
        return serviceTypeMapper.toSummaryList(serviceTypes);
    }

    // ============ STATUS OPERATIONS ============

    @Override
    @Transactional
    public ServiceTypeResponse toggleActive(Integer id) {
        log.debug("Toggling active status for service type ID: {}", id);
        ServiceType serviceType = findServiceTypeById(id);
        serviceType.setIsActive(!serviceType.getIsActive());
        ServiceType saved = serviceTypeRepository.save(serviceType);
        log.info("Toggled active status for service type ID: {} to {}", id, saved.getIsActive());
        return serviceTypeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ServiceTypeResponse activate(Integer id) {
        log.debug("Activating service type ID: {}", id);
        ServiceType serviceType = findServiceTypeById(id);
        serviceType.setIsActive(true);
        ServiceType saved = serviceTypeRepository.save(serviceType);
        log.info("Activated service type ID: {}", id);
        return serviceTypeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ServiceTypeResponse deactivate(Integer id) {
        log.debug("Deactivating service type ID: {}", id);
        ServiceType serviceType = findServiceTypeById(id);
        serviceType.setIsActive(false);
        ServiceType saved = serviceTypeRepository.save(serviceType);
        log.info("Deactivated service type ID: {}", id);
        return serviceTypeMapper.toResponse(saved);
    }

    // ============ STATISTICS ============

    @Override
    public long countActiveByCategory(Integer categoryId) {
        log.debug("Counting active service types for category ID: {}", categoryId);
        return serviceTypeRepository.countActiveByCategory(categoryId);
    }

    // ============ VALIDATION ============

    @Override
    public boolean existsByCategoryIdAndSlug(Integer categoryId, String slug) {
        return serviceTypeRepository.existsByCategoryIdAndSlug(categoryId, slug);
    }

    @Override
    public boolean existsByCategoryIdAndSlugExcluding(Integer categoryId, String slug, Integer serviceTypeId) {
        return serviceTypeRepository.findByCategoryIdAndSlug(categoryId, slug)
                .map(serviceType -> !serviceType.getId().equals(serviceTypeId))
                .orElse(false);
    }

    // ============ HELPER METHODS ============

    private ServiceType findServiceTypeById(Integer id) {
        return serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType", "id", id));
    }
}
