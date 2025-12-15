package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.providerservice.ProviderServiceCreateRequest;
import com.antipanel.backend.dto.providerservice.ProviderServiceResponse;
import com.antipanel.backend.dto.providerservice.ProviderServiceSummary;
import com.antipanel.backend.dto.providerservice.ProviderServiceUpdateRequest;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.entity.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ProviderServiceMapper.
 */
@SpringBootTest(classes = {ProviderServiceMapperImpl.class, ProviderMapperImpl.class})
class ProviderServiceMapperTest {

    @Autowired
    private ProviderServiceMapper mapper;

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        ProviderService providerService = createTestProviderService();

        // When
        ProviderServiceResponse response = mapper.toResponse(providerService);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(providerService.getId());
        assertThat(response.getProviderServiceId()).isEqualTo(providerService.getProviderServiceId());
        assertThat(response.getName()).isEqualTo(providerService.getName());
        assertThat(response.getMinQuantity()).isEqualTo(providerService.getMinQuantity());
        assertThat(response.getMaxQuantity()).isEqualTo(providerService.getMaxQuantity());
        assertThat(response.getCostPerK()).isEqualByComparingTo(providerService.getCostPerK());
        assertThat(response.getRefillDays()).isEqualTo(providerService.getRefillDays());
        assertThat(response.getIsActive()).isEqualTo(providerService.getIsActive());
        assertThat(response.getLastSyncedAt()).isEqualTo(providerService.getLastSyncedAt());
        // Provider is mapped via ProviderMapper
        assertThat(response.getProvider()).isNotNull();
        assertThat(response.getProvider().getId()).isEqualTo(providerService.getProvider().getId());
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        // Given
        ProviderServiceCreateRequest request = ProviderServiceCreateRequest.builder()
                .providerId(1)
                .providerServiceId("12345")
                .name("Instagram Followers - HQ")
                .minQuantity(100)
                .maxQuantity(100000)
                .costPerK(BigDecimal.valueOf(1.50))
                .refillDays(30)
                .isActive(true)
                .build();

        // When
        ProviderService providerService = mapper.toEntity(request);

        // Then
        assertThat(providerService).isNotNull();
        assertThat(providerService.getId()).isNull();  // ID should be null for new entity
        assertThat(providerService.getProviderServiceId()).isEqualTo(request.getProviderServiceId());
        assertThat(providerService.getName()).isEqualTo(request.getName());
        assertThat(providerService.getMinQuantity()).isEqualTo(request.getMinQuantity());
        assertThat(providerService.getMaxQuantity()).isEqualTo(request.getMaxQuantity());
        assertThat(providerService.getCostPerK()).isEqualByComparingTo(request.getCostPerK());
        assertThat(providerService.getRefillDays()).isEqualTo(request.getRefillDays());
        assertThat(providerService.getIsActive()).isEqualTo(request.getIsActive());
        assertThat(providerService.getProvider()).isNull();  // Provider must be set manually
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {
        // Given
        ProviderService providerService = createTestProviderService();
        String originalName = providerService.getName();
        String originalProviderServiceId = providerService.getProviderServiceId();

        ProviderServiceUpdateRequest request = ProviderServiceUpdateRequest.builder()
                .costPerK(BigDecimal.valueOf(2.00))
                .isActive(false)
                .build();

        // When
        mapper.updateEntityFromDto(request, providerService);

        // Then
        assertThat(providerService.getName()).isEqualTo(originalName);  // Unchanged
        assertThat(providerService.getProviderServiceId()).isEqualTo(originalProviderServiceId);  // Unchanged (ignored)
        assertThat(providerService.getCostPerK()).isEqualByComparingTo(BigDecimal.valueOf(2.00));
        assertThat(providerService.getIsActive()).isFalse();
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        ProviderService providerService = createTestProviderService();

        // When
        ProviderServiceSummary summary = mapper.toSummary(providerService);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(providerService.getId());
        assertThat(summary.getProviderServiceId()).isEqualTo(providerService.getProviderServiceId());
        assertThat(summary.getName()).isEqualTo(providerService.getName());
        assertThat(summary.getCostPerK()).isEqualByComparingTo(providerService.getCostPerK());
    }

    @Test
    void toResponseList_ShouldMapAllProviderServices() {
        // Given
        List<ProviderService> providerServices = List.of(
                createTestProviderService(),
                createTestProviderService()
        );
        providerServices.get(1).setId(2);
        providerServices.get(1).setName("Instagram Likes - HQ");
        providerServices.get(1).setProviderServiceId("12346");

        // When
        List<ProviderServiceResponse> responses = mapper.toResponseList(providerServices);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Instagram Followers - HQ");
        assertThat(responses.get(1).getName()).isEqualTo("Instagram Likes - HQ");
    }

    private ProviderService createTestProviderService() {
        Provider provider = new Provider();
        provider.setId(1);
        provider.setName("SMM Panel Pro");
        provider.setIsActive(true);

        ProviderService providerService = new ProviderService();
        providerService.setId(1);
        providerService.setProvider(provider);
        providerService.setProviderServiceId("12345");
        providerService.setName("Instagram Followers - HQ");
        providerService.setMinQuantity(100);
        providerService.setMaxQuantity(100000);
        providerService.setCostPerK(BigDecimal.valueOf(1.50));
        providerService.setRefillDays(30);
        providerService.setIsActive(true);
        providerService.setLastSyncedAt(LocalDateTime.now().minusHours(2));
        return providerService;
    }
}
