package com.antipanel.backend.service;

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
import com.antipanel.backend.service.impl.ServiceTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceTypeServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ServiceTypeServiceTest {

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ServiceTypeMapper serviceTypeMapper;

    @InjectMocks
    private ServiceTypeServiceImpl serviceTypeService;

    private Category testCategory;
    private ServiceType testServiceType;
    private ServiceTypeResponse testServiceTypeResponse;
    private ServiceTypeCreateRequest createRequest;
    private ServiceTypeUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1)
                .name("Instagram")
                .slug("instagram")
                .isActive(true)
                .build();

        testServiceType = ServiceType.builder()
                .id(1)
                .category(testCategory)
                .name("Followers")
                .slug("followers")
                .sortOrder(0)
                .isActive(true)
                .build();

        testServiceTypeResponse = ServiceTypeResponse.builder()
                .id(1)
                .name("Followers")
                .slug("followers")
                .sortOrder(0)
                .isActive(true)
                .build();

        createRequest = ServiceTypeCreateRequest.builder()
                .categoryId(1)
                .name("Followers")
                .slug("followers")
                .sortOrder(0)
                .isActive(true)
                .build();

        updateRequest = ServiceTypeUpdateRequest.builder()
                .name("Followers Updated")
                .sortOrder(1)
                .build();
    }

    // ============ CREATE TESTS ============

    @Test
    void create_Success() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(serviceTypeRepository.existsByCategoryIdAndSlug(1, "followers")).thenReturn(false);
        when(serviceTypeMapper.toEntity(any(ServiceTypeCreateRequest.class))).thenReturn(testServiceType);
        when(serviceTypeRepository.save(any(ServiceType.class))).thenReturn(testServiceType);
        when(serviceTypeMapper.toResponse(any(ServiceType.class))).thenReturn(testServiceTypeResponse);

        // When
        ServiceTypeResponse result = serviceTypeService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("followers");
        verify(serviceTypeRepository).save(any(ServiceType.class));
    }

    @Test
    void create_CategoryNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> serviceTypeService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    void create_SlugAlreadyExistsInCategory_ThrowsConflictException() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(serviceTypeRepository.existsByCategoryIdAndSlug(1, "followers")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> serviceTypeService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Service type slug already exists in this category");
    }

    // ============ GET BY ID TESTS ============

    @Test
    void getById_Success() {
        // Given
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.of(testServiceType));
        when(serviceTypeMapper.toResponse(testServiceType)).thenReturn(testServiceTypeResponse);

        // When
        ServiceTypeResponse result = serviceTypeService.getById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> serviceTypeService.getById(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ServiceType");
    }

    // ============ GET BY CATEGORY ID AND SLUG TESTS ============

    @Test
    void getByCategoryIdAndSlug_Success() {
        // Given
        when(serviceTypeRepository.findByCategoryIdAndSlug(1, "followers"))
                .thenReturn(Optional.of(testServiceType));
        when(serviceTypeMapper.toResponse(testServiceType)).thenReturn(testServiceTypeResponse);

        // When
        ServiceTypeResponse result = serviceTypeService.getByCategoryIdAndSlug(1, "followers");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("followers");
    }

    @Test
    void getByCategoryIdAndSlug_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(serviceTypeRepository.findByCategoryIdAndSlug(1, "notfound"))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> serviceTypeService.getByCategoryIdAndSlug(1, "notfound"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ServiceType");
    }

    // ============ UPDATE TESTS ============

    @Test
    void update_Success() {
        // Given
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.of(testServiceType));
        when(serviceTypeRepository.save(any(ServiceType.class))).thenReturn(testServiceType);
        when(serviceTypeMapper.toResponse(any(ServiceType.class))).thenReturn(testServiceTypeResponse);

        // When
        ServiceTypeResponse result = serviceTypeService.update(1, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(serviceTypeMapper).updateEntityFromDto(eq(updateRequest), any(ServiceType.class));
        verify(serviceTypeRepository).save(any(ServiceType.class));
    }

    @Test
    void update_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> serviceTypeService.update(1, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ DELETE TESTS ============

    @Test
    void delete_Success() {
        // Given
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.of(testServiceType));
        doNothing().when(serviceTypeRepository).delete(testServiceType);

        // When
        serviceTypeService.delete(1);

        // Then
        verify(serviceTypeRepository).delete(testServiceType);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> serviceTypeService.delete(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ LISTING BY CATEGORY TESTS ============

    @Test
    void getAllByCategory_Success() {
        // Given
        when(serviceTypeRepository.findByCategoryIdOrderBySortOrderAsc(1))
                .thenReturn(List.of(testServiceType));
        when(serviceTypeMapper.toResponseList(List.of(testServiceType)))
                .thenReturn(List.of(testServiceTypeResponse));

        // When
        List<ServiceTypeResponse> result = serviceTypeService.getAllByCategory(1);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getActiveByCategory_Success() {
        // Given
        when(serviceTypeRepository.findActiveServiceTypesByCategory(1))
                .thenReturn(List.of(testServiceType));
        when(serviceTypeMapper.toResponseList(List.of(testServiceType)))
                .thenReturn(List.of(testServiceTypeResponse));

        // When
        List<ServiceTypeResponse> result = serviceTypeService.getActiveByCategory(1);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getServiceTypesWithCountByCategory_Success() {
        // Given
        Object[] row = new Object[]{testServiceType, 15L};
        List<Object[]> rows = Collections.singletonList(row);
        when(serviceTypeRepository.findServiceTypesWithCountByCategory(1))
                .thenReturn(rows);
        ServiceTypeResponse responseWithCount = ServiceTypeResponse.builder()
                .id(1)
                .name("Followers")
                .slug("followers")
                .serviceCount(15L)
                .build();
        when(serviceTypeMapper.toResponseWithCount(testServiceType, 15L))
                .thenReturn(responseWithCount);

        // When
        List<ServiceTypeResponse> result = serviceTypeService.getServiceTypesWithCountByCategory(1);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServiceCount()).isEqualTo(15L);
    }

    @Test
    void getSummariesByCategory_Success() {
        // Given
        ServiceTypeSummary summary = ServiceTypeSummary.builder()
                .id(1)
                .name("Followers")
                .slug("followers")
                .build();
        when(serviceTypeRepository.findByCategoryIdOrderBySortOrderAsc(1))
                .thenReturn(List.of(testServiceType));
        when(serviceTypeMapper.toSummaryList(List.of(testServiceType)))
                .thenReturn(List.of(summary));

        // When
        List<ServiceTypeSummary> result = serviceTypeService.getSummariesByCategory(1);

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATUS OPERATIONS TESTS ============

    @Test
    void toggleActive_FromActiveToInactive() {
        // Given
        testServiceType.setIsActive(true);
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.of(testServiceType));
        when(serviceTypeRepository.save(any(ServiceType.class))).thenAnswer(inv -> inv.getArgument(0));
        when(serviceTypeMapper.toResponse(any(ServiceType.class))).thenReturn(
                ServiceTypeResponse.builder().id(1).isActive(false).build()
        );

        // When
        ServiceTypeResponse result = serviceTypeService.toggleActive(1);

        // Then
        verify(serviceTypeRepository).save(argThat(st -> !st.getIsActive()));
    }

    @Test
    void toggleActive_FromInactiveToActive() {
        // Given
        testServiceType.setIsActive(false);
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.of(testServiceType));
        when(serviceTypeRepository.save(any(ServiceType.class))).thenAnswer(inv -> inv.getArgument(0));
        when(serviceTypeMapper.toResponse(any(ServiceType.class))).thenReturn(
                ServiceTypeResponse.builder().id(1).isActive(true).build()
        );

        // When
        ServiceTypeResponse result = serviceTypeService.toggleActive(1);

        // Then
        verify(serviceTypeRepository).save(argThat(st -> st.getIsActive()));
    }

    @Test
    void activate_Success() {
        // Given
        testServiceType.setIsActive(false);
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.of(testServiceType));
        when(serviceTypeRepository.save(any(ServiceType.class))).thenReturn(testServiceType);
        when(serviceTypeMapper.toResponse(any(ServiceType.class))).thenReturn(testServiceTypeResponse);

        // When
        serviceTypeService.activate(1);

        // Then
        verify(serviceTypeRepository).save(argThat(st -> st.getIsActive()));
    }

    @Test
    void deactivate_Success() {
        // Given
        testServiceType.setIsActive(true);
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.of(testServiceType));
        when(serviceTypeRepository.save(any(ServiceType.class))).thenReturn(testServiceType);
        when(serviceTypeMapper.toResponse(any(ServiceType.class))).thenReturn(testServiceTypeResponse);

        // When
        serviceTypeService.deactivate(1);

        // Then
        verify(serviceTypeRepository).save(argThat(st -> !st.getIsActive()));
    }

    // ============ STATISTICS TESTS ============

    @Test
    void countActiveByCategory_Success() {
        // Given
        when(serviceTypeRepository.countActiveByCategory(1)).thenReturn(10L);

        // When
        long count = serviceTypeService.countActiveByCategory(1);

        // Then
        assertThat(count).isEqualTo(10L);
    }

    // ============ VALIDATION TESTS ============

    @Test
    void existsByCategoryIdAndSlug_ReturnsTrue() {
        // Given
        when(serviceTypeRepository.existsByCategoryIdAndSlug(1, "followers")).thenReturn(true);

        // When
        boolean exists = serviceTypeService.existsByCategoryIdAndSlug(1, "followers");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCategoryIdAndSlugExcluding_SameServiceType_ReturnsFalse() {
        // Given
        when(serviceTypeRepository.findByCategoryIdAndSlug(1, "followers"))
                .thenReturn(Optional.of(testServiceType));

        // When
        boolean exists = serviceTypeService.existsByCategoryIdAndSlugExcluding(1, "followers", 1);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByCategoryIdAndSlugExcluding_DifferentServiceType_ReturnsTrue() {
        // Given
        when(serviceTypeRepository.findByCategoryIdAndSlug(1, "followers"))
                .thenReturn(Optional.of(testServiceType));

        // When
        boolean exists = serviceTypeService.existsByCategoryIdAndSlugExcluding(1, "followers", 2);

        // Then
        assertThat(exists).isTrue();
    }
}
