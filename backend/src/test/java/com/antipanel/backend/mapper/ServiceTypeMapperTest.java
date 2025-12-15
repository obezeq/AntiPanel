package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.servicetype.ServiceTypeCreateRequest;
import com.antipanel.backend.dto.servicetype.ServiceTypeResponse;
import com.antipanel.backend.dto.servicetype.ServiceTypeSummary;
import com.antipanel.backend.dto.servicetype.ServiceTypeUpdateRequest;
import com.antipanel.backend.entity.Category;
import com.antipanel.backend.entity.ServiceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ServiceTypeMapper.
 */
@SpringBootTest(classes = {ServiceTypeMapperImpl.class, CategoryMapperImpl.class})
class ServiceTypeMapperTest {

    @Autowired
    private ServiceTypeMapper mapper;

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        ServiceType serviceType = createTestServiceType();

        // When
        ServiceTypeResponse response = mapper.toResponse(serviceType);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(serviceType.getId());
        assertThat(response.getName()).isEqualTo(serviceType.getName());
        assertThat(response.getSlug()).isEqualTo(serviceType.getSlug());
        assertThat(response.getSortOrder()).isEqualTo(serviceType.getSortOrder());
        assertThat(response.getIsActive()).isEqualTo(serviceType.getIsActive());
        assertThat(response.getServiceCount()).isNull();  // Not mapped by default
        // Category is mapped via CategoryMapper
        assertThat(response.getCategory()).isNotNull();
        assertThat(response.getCategory().getId()).isEqualTo(serviceType.getCategory().getId());
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        // Given
        ServiceTypeCreateRequest request = ServiceTypeCreateRequest.builder()
                .categoryId(1)
                .name("Followers")
                .slug("followers")
                .sortOrder(1)
                .isActive(true)
                .build();

        // When
        ServiceType serviceType = mapper.toEntity(request);

        // Then
        assertThat(serviceType).isNotNull();
        assertThat(serviceType.getId()).isNull();  // ID should be null for new entity
        assertThat(serviceType.getName()).isEqualTo(request.getName());
        assertThat(serviceType.getSlug()).isEqualTo(request.getSlug());
        assertThat(serviceType.getSortOrder()).isEqualTo(request.getSortOrder());
        assertThat(serviceType.getIsActive()).isEqualTo(request.getIsActive());
        assertThat(serviceType.getCategory()).isNull();  // Category must be set manually
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {
        // Given
        ServiceType serviceType = createTestServiceType();
        String originalName = serviceType.getName();
        String originalSlug = serviceType.getSlug();

        ServiceTypeUpdateRequest request = ServiceTypeUpdateRequest.builder()
                .sortOrder(5)
                .isActive(false)
                .build();

        // When
        mapper.updateEntityFromDto(request, serviceType);

        // Then
        assertThat(serviceType.getName()).isEqualTo(originalName);  // Unchanged (null in request)
        assertThat(serviceType.getSlug()).isEqualTo(originalSlug);  // Unchanged (ignored)
        assertThat(serviceType.getSortOrder()).isEqualTo(5);
        assertThat(serviceType.getIsActive()).isFalse();
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        ServiceType serviceType = createTestServiceType();

        // When
        ServiceTypeSummary summary = mapper.toSummary(serviceType);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(serviceType.getId());
        assertThat(summary.getName()).isEqualTo(serviceType.getName());
        assertThat(summary.getSlug()).isEqualTo(serviceType.getSlug());
    }

    @Test
    void toResponseList_ShouldMapAllServiceTypes() {
        // Given
        List<ServiceType> serviceTypes = List.of(
                createTestServiceType(),
                createTestServiceType()
        );
        serviceTypes.get(1).setId(2);
        serviceTypes.get(1).setName("Likes");
        serviceTypes.get(1).setSlug("likes");

        // When
        List<ServiceTypeResponse> responses = mapper.toResponseList(serviceTypes);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Followers");
        assertThat(responses.get(1).getName()).isEqualTo("Likes");
    }

    @Test
    void toResponseWithCount_ShouldIncludeServiceCount() {
        // Given
        ServiceType serviceType = createTestServiceType();
        Long serviceCount = 15L;

        // When
        ServiceTypeResponse response = mapper.toResponseWithCount(serviceType, serviceCount);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(serviceType.getId());
        assertThat(response.getServiceCount()).isEqualTo(15L);
    }

    private ServiceType createTestServiceType() {
        Category category = new Category();
        category.setId(1);
        category.setName("Instagram");
        category.setSlug("instagram");

        ServiceType serviceType = new ServiceType();
        serviceType.setId(1);
        serviceType.setCategory(category);
        serviceType.setName("Followers");
        serviceType.setSlug("followers");
        serviceType.setSortOrder(1);
        serviceType.setIsActive(true);
        return serviceType;
    }
}
