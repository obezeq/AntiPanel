package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.providerservice.ProviderServiceCreateRequest;
import com.antipanel.backend.dto.providerservice.ProviderServiceResponse;
import com.antipanel.backend.dto.providerservice.ProviderServiceSummary;
import com.antipanel.backend.dto.providerservice.ProviderServiceUpdateRequest;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.entity.ProviderService;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.ProviderServiceMapper;
import com.antipanel.backend.repository.ProviderRepository;
import com.antipanel.backend.repository.ProviderServiceRepository;
import com.antipanel.backend.service.ProviderCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ProviderCatalogService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProviderCatalogServiceImpl implements ProviderCatalogService {

    private final ProviderServiceRepository providerServiceRepository;
    private final ProviderRepository providerRepository;
    private final ProviderServiceMapper providerServiceMapper;

    // ============ CRUD OPERATIONS ============

    @Override
    @Transactional
    public ProviderServiceResponse create(ProviderServiceCreateRequest request) {
        log.debug("Creating provider service with name: {} for provider ID: {}",
                request.getName(), request.getProviderId());

        // Validate provider exists
        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", request.getProviderId()));

        // Check uniqueness within provider
        if (providerServiceRepository.existsByProviderIdAndProviderServiceId(
                request.getProviderId(), request.getProviderServiceId())) {
            throw new ConflictException("Provider service ID already exists for this provider: "
                    + request.getProviderServiceId());
        }

        ProviderService providerService = providerServiceMapper.toEntity(request);
        providerService.setProvider(provider);

        ProviderService saved = providerServiceRepository.save(providerService);
        log.info("Created provider service with ID: {}", saved.getId());

        return providerServiceMapper.toResponse(saved);
    }

    @Override
    public ProviderServiceResponse getById(Integer id) {
        log.debug("Getting provider service by ID: {}", id);
        ProviderService providerService = findProviderServiceById(id);
        return providerServiceMapper.toResponse(providerService);
    }

    @Override
    public ProviderServiceResponse getByProviderIdAndServiceId(Integer providerId, String providerServiceId) {
        log.debug("Getting provider service by provider ID: {} and service ID: {}", providerId, providerServiceId);
        ProviderService providerService = providerServiceRepository
                .findByProviderIdAndProviderServiceId(providerId, providerServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("ProviderService", "providerServiceId", providerServiceId));
        return providerServiceMapper.toResponse(providerService);
    }

    @Override
    @Transactional
    public ProviderServiceResponse update(Integer id, ProviderServiceUpdateRequest request) {
        log.debug("Updating provider service with ID: {}", id);

        ProviderService providerService = findProviderServiceById(id);
        providerServiceMapper.updateEntityFromDto(request, providerService);

        ProviderService saved = providerServiceRepository.save(providerService);
        log.info("Updated provider service with ID: {}", saved.getId());

        return providerServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.debug("Deleting provider service with ID: {}", id);
        ProviderService providerService = findProviderServiceById(id);
        providerServiceRepository.delete(providerService);
        log.info("Deleted provider service with ID: {}", id);
    }

    // ============ LISTING BY PROVIDER ============

    @Override
    public List<ProviderServiceResponse> getAllByProvider(Integer providerId) {
        log.debug("Getting all provider services for provider ID: {}", providerId);
        List<ProviderService> providerServices = providerServiceRepository.findByProviderIdOrderByNameAsc(providerId);
        return providerServiceMapper.toResponseList(providerServices);
    }

    @Override
    public List<ProviderServiceResponse> getActiveByProvider(Integer providerId) {
        log.debug("Getting active provider services for provider ID: {}", providerId);
        List<ProviderService> providerServices = providerServiceRepository
                .findByProviderIdAndIsActiveTrueOrderByNameAsc(providerId);
        return providerServiceMapper.toResponseList(providerServices);
    }

    @Override
    public List<ProviderServiceResponse> getAllActive() {
        log.debug("Getting all active provider services");
        List<ProviderService> providerServices = providerServiceRepository.findAllActiveProviderServices();
        return providerServiceMapper.toResponseList(providerServices);
    }

    @Override
    public List<ProviderServiceSummary> getSummariesByProvider(Integer providerId) {
        log.debug("Getting provider service summaries for provider ID: {}", providerId);
        List<ProviderService> providerServices = providerServiceRepository.findByProviderIdOrderByNameAsc(providerId);
        return providerServiceMapper.toSummaryList(providerServices);
    }

    // ============ STATUS OPERATIONS ============

    @Override
    @Transactional
    public ProviderServiceResponse toggleActive(Integer id) {
        log.debug("Toggling active status for provider service ID: {}", id);
        ProviderService providerService = findProviderServiceById(id);
        providerService.setIsActive(!providerService.getIsActive());
        ProviderService saved = providerServiceRepository.save(providerService);
        log.info("Toggled active status for provider service ID: {} to {}", id, saved.getIsActive());
        return providerServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProviderServiceResponse activate(Integer id) {
        log.debug("Activating provider service ID: {}", id);
        ProviderService providerService = findProviderServiceById(id);
        providerService.setIsActive(true);
        ProviderService saved = providerServiceRepository.save(providerService);
        log.info("Activated provider service ID: {}", id);
        return providerServiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProviderServiceResponse deactivate(Integer id) {
        log.debug("Deactivating provider service ID: {}", id);
        ProviderService providerService = findProviderServiceById(id);
        providerService.setIsActive(false);
        ProviderService saved = providerServiceRepository.save(providerService);
        log.info("Deactivated provider service ID: {}", id);
        return providerServiceMapper.toResponse(saved);
    }

    // ============ COST OPERATIONS ============

    @Override
    @Transactional
    public ProviderServiceResponse updateCost(Integer id, BigDecimal costPerK) {
        log.debug("Updating cost for provider service ID: {} to {}", id, costPerK);
        ProviderService providerService = findProviderServiceById(id);
        providerService.setCostPerK(costPerK);
        ProviderService saved = providerServiceRepository.save(providerService);
        log.info("Updated cost for provider service ID: {}", id);
        return providerServiceMapper.toResponse(saved);
    }

    @Override
    public List<ProviderServiceResponse> getByCostLessThan(BigDecimal maxCost) {
        log.debug("Getting provider services with cost less than: {}", maxCost);
        List<ProviderService> providerServices = providerServiceRepository.findByCostLessThan(maxCost);
        return providerServiceMapper.toResponseList(providerServices);
    }

    @Override
    public BigDecimal getAverageCostPerK() {
        log.debug("Getting average cost per K");
        BigDecimal avg = providerServiceRepository.getAverageCostPerK();
        return avg != null ? avg : BigDecimal.ZERO;
    }

    // ============ SYNC OPERATIONS ============

    @Override
    @Transactional
    public ProviderServiceResponse updateLastSynced(Integer id) {
        log.debug("Updating last synced for provider service ID: {}", id);
        ProviderService providerService = findProviderServiceById(id);
        providerService.setLastSyncedAt(LocalDateTime.now());
        ProviderService saved = providerServiceRepository.save(providerService);
        log.info("Updated last synced for provider service ID: {}", id);
        return providerServiceMapper.toResponse(saved);
    }

    @Override
    public List<ProviderServiceResponse> getServicesNeedingSync(LocalDateTime before) {
        log.debug("Getting provider services needing sync before: {}", before);
        List<ProviderService> providerServices = providerServiceRepository.findServicesNeedingSync(before);
        return providerServiceMapper.toResponseList(providerServices);
    }

    // ============ STATISTICS ============

    @Override
    public long countActiveByProvider(Integer providerId) {
        log.debug("Counting active provider services for provider ID: {}", providerId);
        return providerServiceRepository.countActiveByProvider(providerId);
    }

    // ============ VALIDATION ============

    @Override
    public boolean existsByProviderIdAndServiceId(Integer providerId, String providerServiceId) {
        return providerServiceRepository.existsByProviderIdAndProviderServiceId(providerId, providerServiceId);
    }

    // ============ HELPER METHODS ============

    private ProviderService findProviderServiceById(Integer id) {
        return providerServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProviderService", "id", id));
    }
}
