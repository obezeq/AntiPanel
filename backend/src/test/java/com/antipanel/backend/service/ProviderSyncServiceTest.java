package com.antipanel.backend.service;

import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.provider.api.DripfeedBalanceResponse;
import com.antipanel.backend.dto.provider.api.DripfeedServiceDto;
import com.antipanel.backend.dto.providerservice.ProviderServiceResponse;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.exception.ProviderApiException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.repository.ProviderRepository;
import com.antipanel.backend.repository.ProviderServiceRepository;
import com.antipanel.backend.service.impl.ProviderSyncServiceImpl;
import com.antipanel.backend.service.provider.ProviderApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for ProviderSyncService.
 * Uses Mockito to mock dependencies and verify sync behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderSyncService Tests")
class ProviderSyncServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderServiceRepository providerServiceRepository;

    @Mock
    private ProviderApiClient providerApiClient;

    @Mock
    private ProviderService providerService;

    @Mock
    private ProviderCatalogService providerCatalogService;

    @InjectMocks
    private ProviderSyncServiceImpl syncService;

    private Provider testProvider;
    private ProviderResponse testProviderResponse;

    @BeforeEach
    void setUp() {
        testProvider = Provider.builder()
                .id(1)
                .name("DripfeedPanel")
                .apiUrl("https://dripfeedpanel.com/api/v2")
                .apiKey("test_api_key")
                .isActive(true)
                .balance(new BigDecimal("100.00"))
                .build();

        testProviderResponse = ProviderResponse.builder()
                .id(1)
                .name("DripfeedPanel")
                .website("https://dripfeedpanel.com")
                .isActive(true)
                .balance(new BigDecimal("100.00"))
                .build();
    }

    @Nested
    @DisplayName("Sync Services")
    class SyncServicesTests {

        @Test
        @DisplayName("Should sync services successfully - creates new services")
        void shouldSyncServicesSuccessfully() {
            List<DripfeedServiceDto> externalServices = List.of(
                    createExternalService(13311, "Instagram Followers", "1.00", 10, 300000, true),
                    createExternalService(15856, "Instagram Likes", "0.31", 10, 300000, false),
                    createExternalService(16132, "TikTok Followers", "1.69", 10, 100000, true)
            );

            ProviderServiceResponse createdService = ProviderServiceResponse.builder()
                    .id(1)
                    .providerId(1)
                    .name("Instagram Followers")
                    .isActive(true)
                    .build();

            when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
            when(providerApiClient.getServices(testProvider)).thenReturn(externalServices);
            when(providerServiceRepository.findByProviderIdOrderByNameAsc(1)).thenReturn(Collections.emptyList());
            when(providerCatalogService.create(any())).thenReturn(createdService);

            List<ProviderServiceResponse> result = syncService.syncServices(1);

            assertThat(result).hasSize(3);
            verify(providerApiClient).getServices(testProvider);
            verify(providerCatalogService, times(3)).create(any());
        }

        @Test
        @DisplayName("Should sync services - updates existing services")
        void shouldSyncServicesUpdatesExisting() {
            List<DripfeedServiceDto> externalServices = List.of(
                    createExternalService(13311, "Instagram Followers Updated", "1.50", 10, 300000, true)
            );

            com.antipanel.backend.entity.ProviderService existingService =
                    com.antipanel.backend.entity.ProviderService.builder()
                            .id(1)
                            .provider(testProvider)
                            .providerServiceId("13311")
                            .name("Instagram Followers")
                            .costPerK(new BigDecimal("1.00"))
                            .isActive(true)
                            .refillDays(30)
                            .build();

            ProviderServiceResponse updatedService = ProviderServiceResponse.builder()
                    .id(1)
                    .providerId(1)
                    .name("Instagram Followers Updated")
                    .isActive(true)
                    .build();

            when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
            when(providerApiClient.getServices(testProvider)).thenReturn(externalServices);
            when(providerServiceRepository.findByProviderIdOrderByNameAsc(1)).thenReturn(List.of(existingService));
            when(providerCatalogService.update(eq(1), any())).thenReturn(updatedService);

            List<ProviderServiceResponse> result = syncService.syncServices(1);

            assertThat(result).hasSize(1);
            verify(providerCatalogService).update(eq(1), any());
            verify(providerCatalogService).updateLastSynced(1);
        }

        @Test
        @DisplayName("Should deactivate removed services")
        void shouldDeactivateRemovedServices() {
            List<DripfeedServiceDto> externalServices = Collections.emptyList();

            com.antipanel.backend.entity.ProviderService existingService =
                    com.antipanel.backend.entity.ProviderService.builder()
                            .id(1)
                            .provider(testProvider)
                            .providerServiceId("13311")
                            .name("Instagram Followers")
                            .isActive(true)
                            .build();

            when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
            when(providerApiClient.getServices(testProvider)).thenReturn(externalServices);
            when(providerServiceRepository.findByProviderIdOrderByNameAsc(1)).thenReturn(List.of(existingService));

            List<ProviderServiceResponse> result = syncService.syncServices(1);

            assertThat(result).isEmpty();
            verify(providerCatalogService).deactivate(1);
        }

        @Test
        @DisplayName("Should throw exception when provider not found")
        void shouldThrowExceptionWhenProviderNotFound() {
            when(providerRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> syncService.syncServices(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Provider");
        }

        @Test
        @DisplayName("Should handle provider API error gracefully")
        void shouldHandleProviderApiError() {
            when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
            when(providerApiClient.getServices(testProvider))
                    .thenThrow(new ProviderApiException("DripfeedPanel", "services", "API Error"));

            assertThatThrownBy(() -> syncService.syncServices(1))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("API Error");
        }
    }

    @Nested
    @DisplayName("Sync Balance")
    class SyncBalanceTests {

        @Test
        @DisplayName("Should sync balance successfully")
        void shouldSyncBalanceSuccessfully() {
            DripfeedBalanceResponse balanceResponse = new DripfeedBalanceResponse();
            balanceResponse.setBalance("250.75");
            balanceResponse.setCurrency("USD");

            ProviderResponse updatedProvider = ProviderResponse.builder()
                    .id(1)
                    .name("DripfeedPanel")
                    .balance(new BigDecimal("250.75"))
                    .build();

            when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
            when(providerApiClient.getBalance(testProvider)).thenReturn(balanceResponse);
            when(providerService.updateBalance(eq(1), any(BigDecimal.class))).thenReturn(updatedProvider);

            ProviderResponse result = syncService.syncBalance(1);

            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("250.75"));
            verify(providerService).updateBalance(1, new BigDecimal("250.75"));
        }

        @Test
        @DisplayName("Should throw exception when provider not found for balance sync")
        void shouldThrowExceptionWhenProviderNotFoundForBalanceSync() {
            when(providerRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> syncService.syncBalance(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Provider");
        }

        @Test
        @DisplayName("Should handle balance API error gracefully")
        void shouldHandleBalanceApiError() {
            when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
            when(providerApiClient.getBalance(testProvider))
                    .thenThrow(new ProviderApiException("DripfeedPanel", "balance", "Invalid API key"));

            assertThatThrownBy(() -> syncService.syncBalance(1))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Invalid API key");
        }
    }

    @Nested
    @DisplayName("Sync All")
    class SyncAllTests {

        @Test
        @DisplayName("Should sync both services and balance")
        void shouldSyncBothServicesAndBalance() {
            List<DripfeedServiceDto> externalServices = List.of(
                    createExternalService(13311, "Instagram Followers", "1.00", 10, 300000, true)
            );

            DripfeedBalanceResponse balanceResponse = new DripfeedBalanceResponse();
            balanceResponse.setBalance("150.00");
            balanceResponse.setCurrency("USD");

            ProviderServiceResponse createdService = ProviderServiceResponse.builder()
                    .id(1)
                    .name("Instagram Followers")
                    .build();

            ProviderResponse updatedProvider = ProviderResponse.builder()
                    .id(1)
                    .name("DripfeedPanel")
                    .balance(new BigDecimal("150.00"))
                    .build();

            when(providerRepository.findById(1)).thenReturn(Optional.of(testProvider));
            when(providerApiClient.getServices(testProvider)).thenReturn(externalServices);
            when(providerServiceRepository.findByProviderIdOrderByNameAsc(1)).thenReturn(Collections.emptyList());
            when(providerCatalogService.create(any())).thenReturn(createdService);
            when(providerApiClient.getBalance(testProvider)).thenReturn(balanceResponse);
            when(providerService.updateBalance(eq(1), any(BigDecimal.class))).thenReturn(updatedProvider);

            ProviderResponse result = syncService.syncAll(1);

            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
            verify(providerApiClient).getServices(testProvider);
            verify(providerApiClient).getBalance(testProvider);
        }
    }

    @Nested
    @DisplayName("Sync All Providers")
    class SyncAllProvidersTests {

        @Test
        @DisplayName("Should sync services for all active providers")
        void shouldSyncServicesForAllActiveProviders() {
            Provider provider1 = Provider.builder()
                    .id(1)
                    .name("Provider1")
                    .isActive(true)
                    .build();

            Provider provider2 = Provider.builder()
                    .id(2)
                    .name("Provider2")
                    .isActive(true)
                    .build();

            List<DripfeedServiceDto> services = List.of(
                    createExternalService(1001, "Service", "1.00", 10, 1000, false)
            );

            ProviderServiceResponse createdService = ProviderServiceResponse.builder()
                    .id(1)
                    .name("Service")
                    .build();

            when(providerRepository.findAllActiveProviders()).thenReturn(List.of(provider1, provider2));
            when(providerRepository.findById(anyInt())).thenAnswer(inv -> {
                int id = inv.getArgument(0);
                return id == 1 ? Optional.of(provider1) : Optional.of(provider2);
            });
            when(providerApiClient.getServices(any())).thenReturn(services);
            when(providerServiceRepository.findByProviderIdOrderByNameAsc(anyInt())).thenReturn(Collections.emptyList());
            when(providerCatalogService.create(any())).thenReturn(createdService);

            int result = syncService.syncAllProviderServices();

            assertThat(result).isEqualTo(2);
            verify(providerApiClient, times(2)).getServices(any());
        }

        @Test
        @DisplayName("Should continue syncing when one provider fails")
        void shouldContinueSyncingWhenOneProviderFails() {
            Provider provider1 = Provider.builder()
                    .id(1)
                    .name("Provider1")
                    .isActive(true)
                    .build();

            Provider provider2 = Provider.builder()
                    .id(2)
                    .name("Provider2")
                    .isActive(true)
                    .build();

            List<DripfeedServiceDto> services = List.of(
                    createExternalService(1001, "Service", "1.00", 10, 1000, false)
            );

            ProviderServiceResponse createdService = ProviderServiceResponse.builder()
                    .id(1)
                    .name("Service")
                    .build();

            when(providerRepository.findAllActiveProviders()).thenReturn(List.of(provider1, provider2));
            when(providerRepository.findById(1)).thenReturn(Optional.of(provider1));
            when(providerRepository.findById(2)).thenReturn(Optional.of(provider2));
            when(providerApiClient.getServices(provider1))
                    .thenThrow(new ProviderApiException("Provider1", "services", "API Error"));
            when(providerApiClient.getServices(provider2)).thenReturn(services);
            when(providerServiceRepository.findByProviderIdOrderByNameAsc(2)).thenReturn(Collections.emptyList());
            when(providerCatalogService.create(any())).thenReturn(createdService);

            int result = syncService.syncAllProviderServices();

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("Should sync balance for all active providers")
        void shouldSyncBalanceForAllActiveProviders() {
            Provider provider1 = Provider.builder()
                    .id(1)
                    .name("Provider1")
                    .isActive(true)
                    .build();

            Provider provider2 = Provider.builder()
                    .id(2)
                    .name("Provider2")
                    .isActive(true)
                    .build();

            DripfeedBalanceResponse balanceResponse = new DripfeedBalanceResponse();
            balanceResponse.setBalance("100.00");
            balanceResponse.setCurrency("USD");

            ProviderResponse updatedProvider = ProviderResponse.builder()
                    .id(1)
                    .balance(new BigDecimal("100.00"))
                    .build();

            when(providerRepository.findAllActiveProviders()).thenReturn(List.of(provider1, provider2));
            when(providerRepository.findById(anyInt())).thenAnswer(inv -> {
                int id = inv.getArgument(0);
                return id == 1 ? Optional.of(provider1) : Optional.of(provider2);
            });
            when(providerApiClient.getBalance(any())).thenReturn(balanceResponse);
            when(providerService.updateBalance(anyInt(), any())).thenReturn(updatedProvider);

            int result = syncService.syncAllProviderBalances();

            assertThat(result).isEqualTo(2);
            verify(providerApiClient, times(2)).getBalance(any());
        }
    }

    /**
     * Helper method to create a DripfeedServiceDto for testing.
     */
    private DripfeedServiceDto createExternalService(
            Integer serviceId, String name, String rate, Integer min, Integer max, Boolean refill) {
        DripfeedServiceDto dto = new DripfeedServiceDto();
        dto.setServiceId(serviceId);
        dto.setName(name);
        dto.setRate(rate);
        dto.setMin(min.toString());
        dto.setMax(max.toString());
        dto.setRefill(refill);
        dto.setCancel(true);
        dto.setType("Default");
        dto.setCategory("Instagram");
        return dto;
    }
}
