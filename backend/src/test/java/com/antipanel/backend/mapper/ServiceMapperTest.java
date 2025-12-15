package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.service.ServiceCreateRequest;
import com.antipanel.backend.dto.service.ServiceDetailResponse;
import com.antipanel.backend.dto.service.ServiceResponse;
import com.antipanel.backend.dto.service.ServiceSummary;
import com.antipanel.backend.dto.service.ServiceUpdateRequest;
import com.antipanel.backend.entity.Category;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.entity.ProviderService;
import com.antipanel.backend.entity.Service;
import com.antipanel.backend.entity.ServiceType;
import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ServiceMapper.
 */
@SpringBootTest(classes = {
        ServiceMapperImpl.class,
        CategoryMapperImpl.class,
        ServiceTypeMapperImpl.class,
        ProviderServiceMapperImpl.class,
        ProviderMapperImpl.class
})
class ServiceMapperTest {

    @Autowired
    private ServiceMapper mapper;

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        Service service = createTestService();

        // When
        ServiceResponse response = mapper.toResponse(service);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(service.getId());
        assertThat(response.getCategoryId()).isEqualTo(service.getCategory().getId());
        assertThat(response.getServiceTypeId()).isEqualTo(service.getServiceType().getId());
        assertThat(response.getProviderServiceId()).isEqualTo(service.getProviderService().getId());
        assertThat(response.getName()).isEqualTo(service.getName());
        assertThat(response.getDescription()).isEqualTo(service.getDescription());
        assertThat(response.getQuality()).isEqualTo(service.getQuality());
        assertThat(response.getSpeed()).isEqualTo(service.getSpeed());
        assertThat(response.getMinQuantity()).isEqualTo(service.getMinQuantity());
        assertThat(response.getMaxQuantity()).isEqualTo(service.getMaxQuantity());
        assertThat(response.getPricePerK()).isEqualByComparingTo(service.getPricePerK());
        assertThat(response.getCostPerK()).isEqualByComparingTo(service.getProviderService().getCostPerK());
        assertThat(response.getRefillDays()).isEqualTo(service.getRefillDays());
        assertThat(response.getAverageTime()).isEqualTo(service.getAverageTime());
        assertThat(response.getIsActive()).isEqualTo(service.getIsActive());
        assertThat(response.getSortOrder()).isEqualTo(service.getSortOrder());
        assertThat(response.getCreatedAt()).isEqualTo(service.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(service.getUpdatedAt());
        assertThat(response.getProfitMargin()).isNull();  // Not mapped by default
    }

    @Test
    void toDetailResponse_ShouldMapNestedEntities() {
        // Given
        Service service = createTestService();

        // When
        ServiceDetailResponse response = mapper.toDetailResponse(service);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(service.getId());
        assertThat(response.getName()).isEqualTo(service.getName());

        // Category nested object
        assertThat(response.getCategory()).isNotNull();
        assertThat(response.getCategory().getId()).isEqualTo(service.getCategory().getId());
        assertThat(response.getCategory().getName()).isEqualTo(service.getCategory().getName());

        // ServiceType nested object
        assertThat(response.getServiceType()).isNotNull();
        assertThat(response.getServiceType().getId()).isEqualTo(service.getServiceType().getId());
        assertThat(response.getServiceType().getName()).isEqualTo(service.getServiceType().getName());

        // ProviderService nested object
        assertThat(response.getProviderService()).isNotNull();
        assertThat(response.getProviderService().getId()).isEqualTo(service.getProviderService().getId());
        assertThat(response.getProviderService().getName()).isEqualTo(service.getProviderService().getName());
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        // Given
        ServiceCreateRequest request = ServiceCreateRequest.builder()
                .categoryId(1)
                .serviceTypeId(1)
                .providerServiceId(1)
                .name("Instagram Followers")
                .description("High quality followers")
                .quality(ServiceQuality.HIGH)
                .speed(ServiceSpeed.FAST)
                .minQuantity(100)
                .maxQuantity(10000)
                .pricePerK(BigDecimal.valueOf(2.50))
                .refillDays(30)
                .averageTime("1-24 hours")
                .isActive(true)
                .sortOrder(1)
                .build();

        // When
        Service service = mapper.toEntity(request);

        // Then
        assertThat(service).isNotNull();
        assertThat(service.getId()).isNull();  // ID should be null for new entity
        assertThat(service.getName()).isEqualTo(request.getName());
        assertThat(service.getDescription()).isEqualTo(request.getDescription());
        assertThat(service.getQuality()).isEqualTo(request.getQuality());
        assertThat(service.getSpeed()).isEqualTo(request.getSpeed());
        assertThat(service.getMinQuantity()).isEqualTo(request.getMinQuantity());
        assertThat(service.getMaxQuantity()).isEqualTo(request.getMaxQuantity());
        assertThat(service.getPricePerK()).isEqualByComparingTo(request.getPricePerK());
        assertThat(service.getRefillDays()).isEqualTo(request.getRefillDays());
        assertThat(service.getAverageTime()).isEqualTo(request.getAverageTime());
        assertThat(service.getIsActive()).isEqualTo(request.getIsActive());
        assertThat(service.getSortOrder()).isEqualTo(request.getSortOrder());
        assertThat(service.getCategory()).isNull();  // Category must be set manually
        assertThat(service.getServiceType()).isNull();  // ServiceType must be set manually
        assertThat(service.getProviderService()).isNull();  // ProviderService must be set manually
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {
        // Given
        Service service = createTestService();
        String originalName = service.getName();
        ServiceQuality originalQuality = service.getQuality();

        ServiceUpdateRequest request = ServiceUpdateRequest.builder()
                .pricePerK(BigDecimal.valueOf(3.00))
                .isActive(false)
                .build();

        // When
        mapper.updateEntityFromDto(request, service);

        // Then
        assertThat(service.getName()).isEqualTo(originalName);  // Unchanged
        assertThat(service.getQuality()).isEqualTo(originalQuality);  // Unchanged (ignored)
        assertThat(service.getPricePerK()).isEqualByComparingTo(BigDecimal.valueOf(3.00));
        assertThat(service.getIsActive()).isFalse();
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        Service service = createTestService();

        // When
        ServiceSummary summary = mapper.toSummary(service);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(service.getId());
        assertThat(summary.getName()).isEqualTo(service.getName());
        assertThat(summary.getQuality()).isEqualTo(service.getQuality());
        assertThat(summary.getSpeed()).isEqualTo(service.getSpeed());
        assertThat(summary.getMinQuantity()).isEqualTo(service.getMinQuantity());
        assertThat(summary.getMaxQuantity()).isEqualTo(service.getMaxQuantity());
        assertThat(summary.getPricePerK()).isEqualByComparingTo(service.getPricePerK());
        assertThat(summary.getRefillDays()).isEqualTo(service.getRefillDays());
        assertThat(summary.getAverageTime()).isEqualTo(service.getAverageTime());
    }

    @Test
    void toResponseList_ShouldMapAllServices() {
        // Given
        List<Service> services = List.of(
                createTestService(),
                createTestService()
        );
        services.get(1).setId(2);
        services.get(1).setName("Instagram Likes");

        // When
        List<ServiceResponse> responses = mapper.toResponseList(services);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Instagram Followers");
        assertThat(responses.get(1).getName()).isEqualTo("Instagram Likes");
    }

    @Test
    void enrichWithProfitMargin_ShouldCalculateMargin() {
        // Given
        Service service = createTestService();
        // pricePerK = 2.50, costPerK = 1.50
        // Expected margin: (2.50 - 1.50) / 2.50 * 100 = 40%

        // When
        ServiceResponse response = mapper.enrichWithProfitMargin(service);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProfitMargin()).isEqualByComparingTo(BigDecimal.valueOf(40.0000));
    }

    private Service createTestService() {
        Category category = new Category();
        category.setId(1);
        category.setName("Instagram");
        category.setSlug("instagram");

        ServiceType serviceType = new ServiceType();
        serviceType.setId(1);
        serviceType.setName("Followers");
        serviceType.setSlug("followers");

        Provider provider = new Provider();
        provider.setId(1);
        provider.setName("SMM Panel Pro");

        ProviderService providerService = new ProviderService();
        providerService.setId(1);
        providerService.setProvider(provider);
        providerService.setProviderServiceId("12345");
        providerService.setName("Instagram Followers - HQ");
        providerService.setCostPerK(BigDecimal.valueOf(1.50));

        Service service = new Service();
        service.setId(1);
        service.setCategory(category);
        service.setServiceType(serviceType);
        service.setProviderService(providerService);
        service.setName("Instagram Followers");
        service.setDescription("High quality followers");
        service.setQuality(ServiceQuality.HIGH);
        service.setSpeed(ServiceSpeed.FAST);
        service.setMinQuantity(100);
        service.setMaxQuantity(10000);
        service.setPricePerK(BigDecimal.valueOf(2.50));
        service.setRefillDays(30);
        service.setAverageTime("1-24 hours");
        service.setIsActive(true);
        service.setSortOrder(1);
        service.setCreatedAt(LocalDateTime.now().minusDays(10));
        service.setUpdatedAt(LocalDateTime.now());
        return service;
    }
}
