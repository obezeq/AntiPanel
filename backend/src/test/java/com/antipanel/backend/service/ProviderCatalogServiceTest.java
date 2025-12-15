package com.antipanel.backend.service;

import com.antipanel.backend.dto.provider.ProviderSummary;
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
import com.antipanel.backend.service.impl.ProviderCatalogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProviderCatalogServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ProviderCatalogServiceTest {

    @Mock
    private ProviderServiceRepository providerServiceRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderServiceMapper providerServiceMapper;

    @InjectMocks
    private ProviderCatalogServiceImpl providerCatalogService;

    private Provider testProvider;
    private ProviderService testProviderService;
    private ProviderServiceResponse testProviderServiceResponse;
    private ProviderServiceCreateRequest createRequest;
    private ProviderServiceUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testProvider = Provider.builder()
                .id(1)
                .name("SMM Provider")
                .apiUrl("https://api.smmprovider.com")
                .apiKey("test-api-key")
                .isActive(true)
                .build();

        testProviderService = ProviderService.builder()
                .id(1)
                .provider(testProvider)
                .providerServiceId("SVC001")
                .name("Instagram Followers")
                .minQuantity(100)
                .maxQuantity(10000)
                .costPerK(new BigDecimal("1.50"))
                .refillDays(30)
                .isActive(true)
                .lastSyncedAt(LocalDateTime.now())
                .build();

        ProviderSummary providerSummary = ProviderSummary.builder()
                .id(1)
                .name("SMM Provider")
                .isActive(true)
                .build();

        testProviderServiceResponse = ProviderServiceResponse.builder()
                .id(1)
                .provider(providerSummary)
                .providerServiceId("SVC001")
                .name("Instagram Followers")
                .minQuantity(100)
                .maxQuantity(10000)
                .costPerK(new BigDecimal("1.50"))
                .refillDays(30)
                .isActive(true)
                .build();

        createRequest = ProviderServiceCreateRequest.builder()
                .providerId(1)
                .providerServiceId("SVC001")
                .name("Instagram Followers")
                .minQuantity(100)
                .maxQuantity(10000)
                .costPerK(new BigDecimal("1.50"))
                .refillDays(30)
                .isActive(true)
                .build();

        updateRequest = ProviderServiceUpdateRequest.builder()
                .name("Updated Service")
                .costPerK(new BigDecimal("2.00"))
                .build();
    }

    // ============ CREATE TESTS ============

    @Test
    void create_Success() {
        // Given
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        when(providerServiceRepository.existsByProviderIdAndProviderServiceId(1, "SVC001")).thenReturn(false);
        when(providerServiceMapper.toEntity(any(ProviderServiceCreateRequest.class))).thenReturn(testProviderService);
        when(providerServiceRepository.save(any(ProviderService.class))).thenReturn(testProviderService);
        when(providerServiceMapper.toResponse(any(ProviderService.class))).thenReturn(testProviderServiceResponse);

        // When
        ProviderServiceResponse result = providerCatalogService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Instagram Followers");
        verify(providerRepository).findById(1);
        verify(providerServiceRepository).existsByProviderIdAndProviderServiceId(1, "SVC001");
        verify(providerServiceRepository).save(any(ProviderService.class));
    }

    @Test
    void create_ProviderNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(providerRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> providerCatalogService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Provider");
    }

    @Test
    void create_ServiceIdAlreadyExists_ThrowsConflictException() {
        // Given
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        when(providerServiceRepository.existsByProviderIdAndProviderServiceId(1, "SVC001")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> providerCatalogService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Provider service ID already exists");
    }

    // ============ GET BY ID TESTS ============

    @Test
    void getById_Success() {
        // Given
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        when(providerServiceMapper.toResponse(testProviderService)).thenReturn(testProviderServiceResponse);

        // When
        ProviderServiceResponse result = providerCatalogService.getById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(providerServiceRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> providerCatalogService.getById(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProviderService");
    }

    // ============ GET BY PROVIDER ID AND SERVICE ID TESTS ============

    @Test
    void getByProviderIdAndServiceId_Success() {
        // Given
        when(providerServiceRepository.findByProviderIdAndProviderServiceId(1, "SVC001"))
                .thenReturn(Optional.of(testProviderService));
        when(providerServiceMapper.toResponse(testProviderService)).thenReturn(testProviderServiceResponse);

        // When
        ProviderServiceResponse result = providerCatalogService.getByProviderIdAndServiceId(1, "SVC001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProviderServiceId()).isEqualTo("SVC001");
    }

    @Test
    void getByProviderIdAndServiceId_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(providerServiceRepository.findByProviderIdAndProviderServiceId(1, "SVC999"))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> providerCatalogService.getByProviderIdAndServiceId(1, "SVC999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProviderService");
    }

    // ============ UPDATE TESTS ============

    @Test
    void update_Success() {
        // Given
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        when(providerServiceRepository.save(any(ProviderService.class))).thenReturn(testProviderService);
        when(providerServiceMapper.toResponse(any(ProviderService.class))).thenReturn(testProviderServiceResponse);

        // When
        ProviderServiceResponse result = providerCatalogService.update(1, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(providerServiceMapper).updateEntityFromDto(eq(updateRequest), any(ProviderService.class));
        verify(providerServiceRepository).save(any(ProviderService.class));
    }

    @Test
    void update_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(providerServiceRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> providerCatalogService.update(1, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ DELETE TESTS ============

    @Test
    void delete_Success() {
        // Given
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        doNothing().when(providerServiceRepository).delete(testProviderService);

        // When
        providerCatalogService.delete(1);

        // Then
        verify(providerServiceRepository).delete(testProviderService);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(providerServiceRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> providerCatalogService.delete(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ LISTING BY PROVIDER TESTS ============

    @Test
    void getAllByProvider_Success() {
        // Given
        when(providerServiceRepository.findByProviderIdOrderByNameAsc(1)).thenReturn(List.of(testProviderService));
        when(providerServiceMapper.toResponseList(List.of(testProviderService)))
                .thenReturn(List.of(testProviderServiceResponse));

        // When
        List<ProviderServiceResponse> result = providerCatalogService.getAllByProvider(1);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getActiveByProvider_Success() {
        // Given
        when(providerServiceRepository.findByProviderIdAndIsActiveTrueOrderByNameAsc(1))
                .thenReturn(List.of(testProviderService));
        when(providerServiceMapper.toResponseList(List.of(testProviderService)))
                .thenReturn(List.of(testProviderServiceResponse));

        // When
        List<ProviderServiceResponse> result = providerCatalogService.getActiveByProvider(1);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getAllActive_Success() {
        // Given
        when(providerServiceRepository.findAllActiveProviderServices()).thenReturn(List.of(testProviderService));
        when(providerServiceMapper.toResponseList(List.of(testProviderService)))
                .thenReturn(List.of(testProviderServiceResponse));

        // When
        List<ProviderServiceResponse> result = providerCatalogService.getAllActive();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getSummariesByProvider_Success() {
        // Given
        ProviderServiceSummary summary = ProviderServiceSummary.builder()
                .id(1)
                .name("Instagram Followers")
                .providerServiceId("SVC001")
                .build();
        when(providerServiceRepository.findByProviderIdOrderByNameAsc(1)).thenReturn(List.of(testProviderService));
        when(providerServiceMapper.toSummaryList(List.of(testProviderService))).thenReturn(List.of(summary));

        // When
        List<ProviderServiceSummary> result = providerCatalogService.getSummariesByProvider(1);

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATUS OPERATIONS TESTS ============

    @Test
    void toggleActive_FromActiveToInactive() {
        // Given
        testProviderService.setIsActive(true);
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        when(providerServiceRepository.save(any(ProviderService.class))).thenAnswer(inv -> inv.getArgument(0));
        when(providerServiceMapper.toResponse(any(ProviderService.class))).thenReturn(
                ProviderServiceResponse.builder().id(1).isActive(false).build()
        );

        // When
        providerCatalogService.toggleActive(1);

        // Then
        verify(providerServiceRepository).save(argThat(ps -> !ps.getIsActive()));
    }

    @Test
    void toggleActive_FromInactiveToActive() {
        // Given
        testProviderService.setIsActive(false);
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        when(providerServiceRepository.save(any(ProviderService.class))).thenAnswer(inv -> inv.getArgument(0));
        when(providerServiceMapper.toResponse(any(ProviderService.class))).thenReturn(
                ProviderServiceResponse.builder().id(1).isActive(true).build()
        );

        // When
        providerCatalogService.toggleActive(1);

        // Then
        verify(providerServiceRepository).save(argThat(ps -> ps.getIsActive()));
    }

    @Test
    void activate_Success() {
        // Given
        testProviderService.setIsActive(false);
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        when(providerServiceRepository.save(any(ProviderService.class))).thenReturn(testProviderService);
        when(providerServiceMapper.toResponse(any(ProviderService.class))).thenReturn(testProviderServiceResponse);

        // When
        providerCatalogService.activate(1);

        // Then
        verify(providerServiceRepository).save(argThat(ps -> ps.getIsActive()));
    }

    @Test
    void deactivate_Success() {
        // Given
        testProviderService.setIsActive(true);
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        when(providerServiceRepository.save(any(ProviderService.class))).thenReturn(testProviderService);
        when(providerServiceMapper.toResponse(any(ProviderService.class))).thenReturn(testProviderServiceResponse);

        // When
        providerCatalogService.deactivate(1);

        // Then
        verify(providerServiceRepository).save(argThat(ps -> !ps.getIsActive()));
    }

    // ============ COST OPERATIONS TESTS ============

    @Test
    void updateCost_Success() {
        // Given
        BigDecimal newCost = new BigDecimal("3.00");
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        when(providerServiceRepository.save(any(ProviderService.class))).thenReturn(testProviderService);
        when(providerServiceMapper.toResponse(any(ProviderService.class))).thenReturn(testProviderServiceResponse);

        // When
        providerCatalogService.updateCost(1, newCost);

        // Then
        verify(providerServiceRepository).save(argThat(ps -> ps.getCostPerK().equals(newCost)));
    }

    @Test
    void getByCostLessThan_Success() {
        // Given
        BigDecimal maxCost = new BigDecimal("5.00");
        when(providerServiceRepository.findByCostLessThan(maxCost)).thenReturn(List.of(testProviderService));
        when(providerServiceMapper.toResponseList(List.of(testProviderService)))
                .thenReturn(List.of(testProviderServiceResponse));

        // When
        List<ProviderServiceResponse> result = providerCatalogService.getByCostLessThan(maxCost);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getAverageCostPerK_Success() {
        // Given
        BigDecimal avg = new BigDecimal("2.50");
        when(providerServiceRepository.getAverageCostPerK()).thenReturn(avg);

        // When
        BigDecimal result = providerCatalogService.getAverageCostPerK();

        // Then
        assertThat(result).isEqualByComparingTo(avg);
    }

    @Test
    void getAverageCostPerK_Null_ReturnsZero() {
        // Given
        when(providerServiceRepository.getAverageCostPerK()).thenReturn(null);

        // When
        BigDecimal result = providerCatalogService.getAverageCostPerK();

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ============ SYNC OPERATIONS TESTS ============

    @Test
    void updateLastSynced_Success() {
        // Given
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        when(providerServiceRepository.save(any(ProviderService.class))).thenReturn(testProviderService);
        when(providerServiceMapper.toResponse(any(ProviderService.class))).thenReturn(testProviderServiceResponse);

        // When
        providerCatalogService.updateLastSynced(1);

        // Then
        verify(providerServiceRepository).save(argThat(ps -> ps.getLastSyncedAt() != null));
    }

    @Test
    void getServicesNeedingSync_Success() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusHours(1);
        when(providerServiceRepository.findServicesNeedingSync(before)).thenReturn(List.of(testProviderService));
        when(providerServiceMapper.toResponseList(List.of(testProviderService)))
                .thenReturn(List.of(testProviderServiceResponse));

        // When
        List<ProviderServiceResponse> result = providerCatalogService.getServicesNeedingSync(before);

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATISTICS TESTS ============

    @Test
    void countActiveByProvider_Success() {
        // Given
        when(providerServiceRepository.countActiveByProvider(1)).thenReturn(5L);

        // When
        long count = providerCatalogService.countActiveByProvider(1);

        // Then
        assertThat(count).isEqualTo(5L);
    }

    // ============ VALIDATION TESTS ============

    @Test
    void existsByProviderIdAndServiceId_ReturnsTrue() {
        // Given
        when(providerServiceRepository.existsByProviderIdAndProviderServiceId(1, "SVC001")).thenReturn(true);

        // When
        boolean exists = providerCatalogService.existsByProviderIdAndServiceId(1, "SVC001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByProviderIdAndServiceId_ReturnsFalse() {
        // Given
        when(providerServiceRepository.existsByProviderIdAndProviderServiceId(1, "SVC999")).thenReturn(false);

        // When
        boolean exists = providerCatalogService.existsByProviderIdAndServiceId(1, "SVC999");

        // Then
        assertThat(exists).isFalse();
    }
}
