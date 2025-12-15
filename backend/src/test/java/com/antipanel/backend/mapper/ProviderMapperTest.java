package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.provider.ProviderCreateRequest;
import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.provider.ProviderSummary;
import com.antipanel.backend.dto.provider.ProviderUpdateRequest;
import com.antipanel.backend.entity.Provider;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ProviderMapper.
 */
class ProviderMapperTest {

    private final ProviderMapper mapper = Mappers.getMapper(ProviderMapper.class);

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        Provider provider = createTestProvider();

        // When
        ProviderResponse response = mapper.toResponse(provider);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(provider.getId());
        assertThat(response.getName()).isEqualTo(provider.getName());
        assertThat(response.getWebsite()).isEqualTo(provider.getWebsite());
        assertThat(response.getApiUrl()).isEqualTo(provider.getApiUrl());
        assertThat(response.getApiKey()).isEqualTo(provider.getApiKey());
        assertThat(response.getIsActive()).isEqualTo(provider.getIsActive());
        assertThat(response.getBalance()).isEqualByComparingTo(provider.getBalance());
        assertThat(response.getCreatedAt()).isEqualTo(provider.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(provider.getUpdatedAt());
        assertThat(response.getServiceCount()).isNull();  // Not mapped by default
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        // Given
        ProviderCreateRequest request = ProviderCreateRequest.builder()
                .name("SMM Panel Pro")
                .website("https://smmpanelpro.com")
                .apiUrl("https://smmpanelpro.com/api/v2")
                .apiKey("secret-api-key")
                .isActive(true)
                .build();

        // When
        Provider provider = mapper.toEntity(request);

        // Then
        assertThat(provider).isNotNull();
        assertThat(provider.getId()).isNull();  // ID should be null for new entity
        assertThat(provider.getName()).isEqualTo(request.getName());
        assertThat(provider.getWebsite()).isEqualTo(request.getWebsite());
        assertThat(provider.getApiUrl()).isEqualTo(request.getApiUrl());
        assertThat(provider.getApiKey()).isEqualTo(request.getApiKey());
        assertThat(provider.getIsActive()).isEqualTo(request.getIsActive());
        assertThat(provider.getBalance()).isNull();  // Balance set by service
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {
        // Given
        Provider provider = createTestProvider();
        String originalName = provider.getName();
        String originalApiUrl = provider.getApiUrl();

        ProviderUpdateRequest request = ProviderUpdateRequest.builder()
                .website("https://new-website.com")
                .apiKey("new-api-key")
                .build();

        // When
        mapper.updateEntityFromDto(request, provider);

        // Then
        assertThat(provider.getName()).isEqualTo(originalName);  // Unchanged
        assertThat(provider.getApiUrl()).isEqualTo(originalApiUrl);  // Unchanged
        assertThat(provider.getWebsite()).isEqualTo("https://new-website.com");
        assertThat(provider.getApiKey()).isEqualTo("new-api-key");
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        Provider provider = createTestProvider();

        // When
        ProviderSummary summary = mapper.toSummary(provider);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(provider.getId());
        assertThat(summary.getName()).isEqualTo(provider.getName());
        assertThat(summary.getIsActive()).isEqualTo(provider.getIsActive());
    }

    @Test
    void toResponseList_ShouldMapAllProviders() {
        // Given
        List<Provider> providers = List.of(
                createTestProvider(),
                createTestProvider()
        );
        providers.get(1).setId(2);
        providers.get(1).setName("SMM Panel Basic");

        // When
        List<ProviderResponse> responses = mapper.toResponseList(providers);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("SMM Panel Pro");
        assertThat(responses.get(1).getName()).isEqualTo("SMM Panel Basic");
    }

    @Test
    void toResponseWithCount_ShouldIncludeServiceCount() {
        // Given
        Provider provider = createTestProvider();
        Long serviceCount = 50L;

        // When
        ProviderResponse response = mapper.toResponseWithCount(provider, serviceCount);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(provider.getId());
        assertThat(response.getServiceCount()).isEqualTo(50L);
    }

    private Provider createTestProvider() {
        Provider provider = new Provider();
        provider.setId(1);
        provider.setName("SMM Panel Pro");
        provider.setWebsite("https://smmpanelpro.com");
        provider.setApiUrl("https://smmpanelpro.com/api/v2");
        provider.setApiKey("secret-api-key");
        provider.setIsActive(true);
        provider.setBalance(BigDecimal.valueOf(500.00));
        provider.setCreatedAt(LocalDateTime.now().minusDays(30));
        provider.setUpdatedAt(LocalDateTime.now());
        return provider;
    }
}
