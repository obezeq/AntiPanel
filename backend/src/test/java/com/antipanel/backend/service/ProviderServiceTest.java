package com.antipanel.backend.service;

import com.antipanel.backend.dto.provider.ProviderCreateRequest;
import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.provider.ProviderSummary;
import com.antipanel.backend.dto.provider.ProviderUpdateRequest;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.ProviderMapper;
import com.antipanel.backend.repository.ProviderRepository;
import com.antipanel.backend.service.impl.ProviderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProviderServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderMapper providerMapper;

    @InjectMocks
    private ProviderServiceImpl providerService;

    private Provider testProvider;
    private ProviderResponse testProviderResponse;
    private ProviderCreateRequest createRequest;
    private ProviderUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testProvider = Provider.builder()
                .id(1)
                .name("SMM Provider")
                .website("https://smmprovider.com")
                .apiUrl("https://api.smmprovider.com")
                .apiKey("test-api-key")
                .isActive(true)
                .balance(new BigDecimal("1000.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testProviderResponse = ProviderResponse.builder()
                .id(1)
                .name("SMM Provider")
                .website("https://smmprovider.com")
                .apiUrl("https://api.smmprovider.com")
                .isActive(true)
                .balance(new BigDecimal("1000.00"))
                .build();

        createRequest = ProviderCreateRequest.builder()
                .name("SMM Provider")
                .website("https://smmprovider.com")
                .apiUrl("https://api.smmprovider.com")
                .apiKey("test-api-key")
                .isActive(true)
                .build();

        updateRequest = ProviderUpdateRequest.builder()
                .name("Updated Provider")
                .website("https://updated.com")
                .build();
    }

    // ============ CREATE TESTS ============

    @Test
    void create_Success() {
        // Given
        when(providerRepository.existsByName("SMM Provider")).thenReturn(false);
        when(providerMapper.toEntity(any(ProviderCreateRequest.class))).thenReturn(testProvider);
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);
        when(providerMapper.toResponse(any(Provider.class))).thenReturn(testProviderResponse);

        // When
        ProviderResponse result = providerService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("SMM Provider");
        verify(providerRepository).existsByName("SMM Provider");
        verify(providerRepository).save(any(Provider.class));
    }

    @Test
    void create_NameAlreadyExists_ThrowsConflictException() {
        // Given
        when(providerRepository.existsByName("SMM Provider")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> providerService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Provider name already exists");
    }

    // ============ GET BY ID TESTS ============

    @Test
    void getById_Success() {
        // Given
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        when(providerMapper.toResponse(testProvider)).thenReturn(testProviderResponse);

        // When
        ProviderResponse result = providerService.getById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(providerRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> providerService.getById(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Provider");
    }

    // ============ GET BY NAME TESTS ============

    @Test
    void getByName_Success() {
        // Given
        when(providerRepository.findByName("SMM Provider")).thenReturn(Optional.of(testProvider));
        when(providerMapper.toResponse(testProvider)).thenReturn(testProviderResponse);

        // When
        ProviderResponse result = providerService.getByName("SMM Provider");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("SMM Provider");
    }

    @Test
    void getByName_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(providerRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> providerService.getByName("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Provider");
    }

    // ============ UPDATE TESTS ============

    @Test
    void update_Success() {
        // Given
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);
        when(providerMapper.toResponse(any(Provider.class))).thenReturn(testProviderResponse);

        // When
        ProviderResponse result = providerService.update(1, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(providerMapper).updateEntityFromDto(eq(updateRequest), any(Provider.class));
        verify(providerRepository).save(any(Provider.class));
    }

    @Test
    void update_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(providerRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> providerService.update(1, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ DELETE TESTS ============

    @Test
    void delete_Success() {
        // Given
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        doNothing().when(providerRepository).delete(testProvider);

        // When
        providerService.delete(1);

        // Then
        verify(providerRepository).delete(testProvider);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(providerRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> providerService.delete(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ LISTING TESTS ============

    @Test
    void getAll_Success() {
        // Given
        when(providerRepository.findAll()).thenReturn(List.of(testProvider));
        when(providerMapper.toResponseList(List.of(testProvider))).thenReturn(List.of(testProviderResponse));

        // When
        List<ProviderResponse> result = providerService.getAll();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getAllActive_Success() {
        // Given
        when(providerRepository.findAllActiveProviders()).thenReturn(List.of(testProvider));
        when(providerMapper.toResponseList(List.of(testProvider))).thenReturn(List.of(testProviderResponse));

        // When
        List<ProviderResponse> result = providerService.getAllActive();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getActiveProvidersWithServiceCount_Success() {
        // Given
        Object[] row = new Object[]{testProvider, 10L};
        List<Object[]> rows = Collections.singletonList(row);
        when(providerRepository.findActiveProvidersWithServiceCount()).thenReturn(rows);
        ProviderResponse responseWithCount = ProviderResponse.builder()
                .id(1)
                .name("SMM Provider")
                .serviceCount(10L)
                .build();
        when(providerMapper.toResponseWithCount(testProvider, 10L)).thenReturn(responseWithCount);

        // When
        List<ProviderResponse> result = providerService.getActiveProvidersWithServiceCount();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServiceCount()).isEqualTo(10L);
    }

    @Test
    void getAllSummaries_Success() {
        // Given
        ProviderSummary summary = ProviderSummary.builder()
                .id(1)
                .name("SMM Provider")
                .build();
        when(providerRepository.findAllActiveProviders()).thenReturn(List.of(testProvider));
        when(providerMapper.toSummaryList(List.of(testProvider))).thenReturn(List.of(summary));

        // When
        List<ProviderSummary> result = providerService.getAllSummaries();

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATUS OPERATIONS TESTS ============

    @Test
    void toggleActive_FromActiveToInactive() {
        // Given
        testProvider.setIsActive(true);
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(inv -> inv.getArgument(0));
        when(providerMapper.toResponse(any(Provider.class))).thenReturn(
                ProviderResponse.builder().id(1).isActive(false).build()
        );

        // When
        providerService.toggleActive(1);

        // Then
        verify(providerRepository).save(argThat(p -> !p.getIsActive()));
    }

    @Test
    void toggleActive_FromInactiveToActive() {
        // Given
        testProvider.setIsActive(false);
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(inv -> inv.getArgument(0));
        when(providerMapper.toResponse(any(Provider.class))).thenReturn(
                ProviderResponse.builder().id(1).isActive(true).build()
        );

        // When
        providerService.toggleActive(1);

        // Then
        verify(providerRepository).save(argThat(p -> p.getIsActive()));
    }

    @Test
    void activate_Success() {
        // Given
        testProvider.setIsActive(false);
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);
        when(providerMapper.toResponse(any(Provider.class))).thenReturn(testProviderResponse);

        // When
        providerService.activate(1);

        // Then
        verify(providerRepository).save(argThat(p -> p.getIsActive()));
    }

    @Test
    void deactivate_Success() {
        // Given
        testProvider.setIsActive(true);
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);
        when(providerMapper.toResponse(any(Provider.class))).thenReturn(testProviderResponse);

        // When
        providerService.deactivate(1);

        // Then
        verify(providerRepository).save(argThat(p -> !p.getIsActive()));
    }

    // ============ BALANCE OPERATIONS TESTS ============

    @Test
    void updateBalance_Success() {
        // Given
        BigDecimal newBalance = new BigDecimal("2000.00");
        when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);
        when(providerMapper.toResponse(any(Provider.class))).thenReturn(testProviderResponse);

        // When
        providerService.updateBalance(1, newBalance);

        // Then
        verify(providerRepository).save(argThat(p -> p.getBalance().equals(newBalance)));
    }

    @Test
    void getProvidersWithLowBalance_Success() {
        // Given
        BigDecimal threshold = new BigDecimal("500.00");
        when(providerRepository.findProvidersWithLowBalance(threshold)).thenReturn(List.of(testProvider));
        when(providerMapper.toResponseList(List.of(testProvider))).thenReturn(List.of(testProviderResponse));

        // When
        List<ProviderResponse> result = providerService.getProvidersWithLowBalance(threshold);

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATISTICS TESTS ============

    @Test
    void countActive_Success() {
        // Given
        when(providerRepository.countActiveProviders()).thenReturn(5L);

        // When
        long count = providerService.countActive();

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    void getTotalProviderBalance_Success() {
        // Given
        BigDecimal total = new BigDecimal("5000.00");
        when(providerRepository.getTotalProviderBalance()).thenReturn(total);

        // When
        BigDecimal result = providerService.getTotalProviderBalance();

        // Then
        assertThat(result).isEqualByComparingTo(total);
    }

    @Test
    void getTotalProviderBalance_Null_ReturnsZero() {
        // Given
        when(providerRepository.getTotalProviderBalance()).thenReturn(null);

        // When
        BigDecimal result = providerService.getTotalProviderBalance();

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ============ VALIDATION TESTS ============

    @Test
    void existsByName_ReturnsTrue() {
        // Given
        when(providerRepository.existsByName("SMM Provider")).thenReturn(true);

        // When
        boolean exists = providerService.existsByName("SMM Provider");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ReturnsFalse() {
        // Given
        when(providerRepository.existsByName("nonexistent")).thenReturn(false);

        // When
        boolean exists = providerService.existsByName("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }
}
