package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.provider.ProviderCreateRequest;
import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.provider.ProviderSummary;
import com.antipanel.backend.dto.provider.ProviderUpdateRequest;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.ProviderMapper;
import com.antipanel.backend.repository.ProviderRepository;
import com.antipanel.backend.service.ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ProviderService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;

    // ============ CRUD OPERATIONS ============

    @Override
    @Transactional
    public ProviderResponse create(ProviderCreateRequest request) {
        log.debug("Creating provider with name: {}", request.getName());

        if (providerRepository.existsByName(request.getName())) {
            throw new ConflictException("Provider name already exists: " + request.getName());
        }

        Provider provider = providerMapper.toEntity(request);
        provider.setBalance(BigDecimal.ZERO);

        Provider saved = providerRepository.save(provider);
        log.info("Created provider with ID: {}", saved.getId());

        return providerMapper.toResponse(saved);
    }

    @Override
    public ProviderResponse getById(Integer id) {
        log.debug("Getting provider by ID: {}", id);
        Provider provider = findProviderById(id);
        return providerMapper.toResponse(provider);
    }

    @Override
    public ProviderResponse getByName(String name) {
        log.debug("Getting provider by name: {}", name);
        Provider provider = providerRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "name", name));
        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional
    public ProviderResponse update(Integer id, ProviderUpdateRequest request) {
        log.debug("Updating provider with ID: {}", id);

        Provider provider = findProviderById(id);

        // Check name uniqueness if name is being changed
        if (request.getName() != null && !request.getName().equals(provider.getName())) {
            if (providerRepository.existsByName(request.getName())) {
                throw new ConflictException("Provider name already exists: " + request.getName());
            }
        }

        providerMapper.updateEntityFromDto(request, provider);

        Provider saved = providerRepository.save(provider);
        log.info("Updated provider with ID: {}", saved.getId());

        return providerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.debug("Deleting provider with ID: {}", id);
        Provider provider = findProviderById(id);
        providerRepository.delete(provider);
        log.info("Deleted provider with ID: {}", id);
    }

    // ============ LISTING ============

    @Override
    public List<ProviderResponse> getAll() {
        log.debug("Getting all providers");
        List<Provider> providers = providerRepository.findAll();
        return providerMapper.toResponseList(providers);
    }

    @Override
    public List<ProviderResponse> getAllActive() {
        log.debug("Getting all active providers");
        List<Provider> providers = providerRepository.findAllActiveProviders();
        return providerMapper.toResponseList(providers);
    }

    @Override
    public List<ProviderResponse> getActiveProvidersWithServiceCount() {
        log.debug("Getting active providers with service count");
        List<Object[]> results = providerRepository.findActiveProvidersWithServiceCount();
        return results.stream()
                .map(row -> {
                    Provider provider = (Provider) row[0];
                    Long serviceCount = (Long) row[1];
                    return providerMapper.toResponseWithCount(provider, serviceCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProviderSummary> getAllSummaries() {
        log.debug("Getting all provider summaries");
        List<Provider> providers = providerRepository.findAllActiveProviders();
        return providerMapper.toSummaryList(providers);
    }

    // ============ STATUS OPERATIONS ============

    @Override
    @Transactional
    public ProviderResponse toggleActive(Integer id) {
        log.debug("Toggling active status for provider ID: {}", id);
        Provider provider = findProviderById(id);
        provider.setIsActive(!provider.getIsActive());
        Provider saved = providerRepository.save(provider);
        log.info("Toggled active status for provider ID: {} to {}", id, saved.getIsActive());
        return providerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProviderResponse activate(Integer id) {
        log.debug("Activating provider ID: {}", id);
        Provider provider = findProviderById(id);
        provider.setIsActive(true);
        Provider saved = providerRepository.save(provider);
        log.info("Activated provider ID: {}", id);
        return providerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProviderResponse deactivate(Integer id) {
        log.debug("Deactivating provider ID: {}", id);
        Provider provider = findProviderById(id);
        provider.setIsActive(false);
        Provider saved = providerRepository.save(provider);
        log.info("Deactivated provider ID: {}", id);
        return providerMapper.toResponse(saved);
    }

    // ============ BALANCE OPERATIONS ============

    @Override
    @Transactional
    public ProviderResponse updateBalance(Integer id, BigDecimal balance) {
        log.debug("Updating balance for provider ID: {} to {}", id, balance);
        Provider provider = findProviderById(id);
        provider.setBalance(balance);
        Provider saved = providerRepository.save(provider);
        log.info("Updated balance for provider ID: {}", id);
        return providerMapper.toResponse(saved);
    }

    @Override
    public List<ProviderResponse> getProvidersWithLowBalance(BigDecimal threshold) {
        log.debug("Getting providers with balance below: {}", threshold);
        List<Provider> providers = providerRepository.findProvidersWithLowBalance(threshold);
        return providerMapper.toResponseList(providers);
    }

    // ============ STATISTICS ============

    @Override
    public long countActive() {
        log.debug("Counting active providers");
        return providerRepository.countActiveProviders();
    }

    @Override
    public BigDecimal getTotalProviderBalance() {
        log.debug("Getting total provider balance");
        BigDecimal total = providerRepository.getTotalProviderBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    // ============ VALIDATION ============

    @Override
    public boolean existsByName(String name) {
        return providerRepository.existsByName(name);
    }

    // ============ HELPER METHODS ============

    private Provider findProviderById(Integer id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", id));
    }
}
