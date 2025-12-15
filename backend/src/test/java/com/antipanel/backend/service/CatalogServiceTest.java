package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
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
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.mapper.ServiceMapper;
import com.antipanel.backend.repository.CategoryRepository;
import com.antipanel.backend.repository.ProviderServiceRepository;
import com.antipanel.backend.repository.ServiceRepository;
import com.antipanel.backend.repository.ServiceTypeRepository;
import com.antipanel.backend.service.impl.CatalogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CatalogServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Mock
    private ProviderServiceRepository providerServiceRepository;

    @Mock
    private ServiceMapper serviceMapper;

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private Category testCategory;
    private ServiceType testServiceType;
    private Provider testProvider;
    private ProviderService testProviderService;
    private Service testService;
    private ServiceResponse testServiceResponse;
    private ServiceDetailResponse testServiceDetailResponse;
    private ServiceCreateRequest createRequest;
    private ServiceUpdateRequest updateRequest;

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
                .name("Followers")
                .category(testCategory)
                .isActive(true)
                .build();

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
                .build();

        testService = Service.builder()
                .id(1)
                .category(testCategory)
                .serviceType(testServiceType)
                .providerService(testProviderService)
                .name("Instagram Followers - Premium")
                .description("High quality Instagram followers")
                .quality(ServiceQuality.HIGH)
                .speed(ServiceSpeed.FAST)
                .minQuantity(100)
                .maxQuantity(10000)
                .pricePerK(new BigDecimal("3.00"))
                .refillDays(30)
                .averageTime("1-2 hours")
                .isActive(true)
                .sortOrder(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testServiceResponse = ServiceResponse.builder()
                .id(1)
                .categoryId(1)
                .serviceTypeId(1)
                .providerServiceId(1)
                .name("Instagram Followers - Premium")
                .description("High quality Instagram followers")
                .quality(ServiceQuality.HIGH)
                .speed(ServiceSpeed.FAST)
                .minQuantity(100)
                .maxQuantity(10000)
                .pricePerK(new BigDecimal("3.00"))
                .refillDays(30)
                .averageTime("1-2 hours")
                .isActive(true)
                .build();

        testServiceDetailResponse = ServiceDetailResponse.builder()
                .id(1)
                .name("Instagram Followers - Premium")
                .pricePerK(new BigDecimal("3.00"))
                .build();

        createRequest = ServiceCreateRequest.builder()
                .categoryId(1)
                .serviceTypeId(1)
                .providerServiceId(1)
                .name("Instagram Followers - Premium")
                .description("High quality Instagram followers")
                .quality(ServiceQuality.HIGH)
                .speed(ServiceSpeed.FAST)
                .minQuantity(100)
                .maxQuantity(10000)
                .pricePerK(new BigDecimal("3.00"))
                .refillDays(30)
                .averageTime("1-2 hours")
                .isActive(true)
                .sortOrder(0)
                .build();

        updateRequest = ServiceUpdateRequest.builder()
                .name("Updated Service Name")
                .pricePerK(new BigDecimal("4.00"))
                .build();
    }

    // ============ CREATE TESTS ============

    @Test
    void create_Success() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.of(testServiceType));
        when(providerServiceRepository.findById(1)).thenReturn(Optional.of(testProviderService));
        when(serviceMapper.toEntity(any(ServiceCreateRequest.class))).thenReturn(testService);
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);
        when(serviceMapper.enrichWithProfitMargin(any(Service.class))).thenReturn(testServiceResponse);

        // When
        ServiceResponse result = catalogService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Instagram Followers - Premium");
        verify(categoryRepository).findById(1);
        verify(serviceTypeRepository).findById(1);
        verify(providerServiceRepository).findById(1);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void create_CategoryNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> catalogService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    void create_ServiceTypeNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> catalogService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ServiceType");
    }

    @Test
    void create_ProviderServiceNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(serviceTypeRepository.findById(1)).thenReturn(Optional.of(testServiceType));
        when(providerServiceRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> catalogService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProviderService");
    }

    // ============ GET BY ID TESTS ============

    @Test
    void getById_Success() {
        // Given
        when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);

        // When
        ServiceResponse result = catalogService.getById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(serviceRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> catalogService.getById(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Service");
    }

    // ============ GET DETAIL BY ID TESTS ============

    @Test
    void getDetailById_Success() {
        // Given
        when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
        when(serviceMapper.toDetailResponse(testService)).thenReturn(testServiceDetailResponse);

        // When
        ServiceDetailResponse result = catalogService.getDetailById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getDetailById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(serviceRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> catalogService.getDetailById(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Service");
    }

    // ============ UPDATE TESTS ============

    @Test
    void update_Success() {
        // Given
        when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);
        when(serviceMapper.enrichWithProfitMargin(any(Service.class))).thenReturn(testServiceResponse);

        // When
        ServiceResponse result = catalogService.update(1, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(serviceMapper).updateEntityFromDto(eq(updateRequest), any(Service.class));
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void update_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(serviceRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> catalogService.update(1, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ DELETE TESTS ============

    @Test
    void delete_Success() {
        // Given
        when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
        doNothing().when(serviceRepository).delete(testService);

        // When
        catalogService.delete(1);

        // Then
        verify(serviceRepository).delete(testService);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(serviceRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> catalogService.delete(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ PUBLIC CATALOG QUERIES TESTS ============

    @Test
    void getActiveCatalogServices_Success() {
        // Given
        when(serviceRepository.findActiveCatalogServices()).thenReturn(List.of(testService));
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);

        // When
        List<ServiceResponse> result = catalogService.getActiveCatalogServices();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getActiveCatalogServicesByCategory_Success() {
        // Given
        when(serviceRepository.findActiveCatalogServicesByCategory(1)).thenReturn(List.of(testService));
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);

        // When
        List<ServiceResponse> result = catalogService.getActiveCatalogServicesByCategory(1);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getActiveCatalogServicesByCategoryAndType_Success() {
        // Given
        when(serviceRepository.findActiveCatalogServicesByCategoryAndType(1, 1)).thenReturn(List.of(testService));
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);

        // When
        List<ServiceResponse> result = catalogService.getActiveCatalogServicesByCategoryAndType(1, 1);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCatalogServicesFiltered_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Service> page = new PageImpl<>(List.of(testService), pageable, 1);
        PageResponse<ServiceResponse> expectedPageResponse = PageResponse.<ServiceResponse>builder()
                .content(List.of(testServiceResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(serviceRepository.findCatalogServicesFiltered(1, 1, ServiceQuality.HIGH, ServiceSpeed.FAST, pageable))
                .thenReturn(page);
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);
        doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

        // When
        PageResponse<ServiceResponse> result = catalogService.getCatalogServicesFiltered(
                1, 1, ServiceQuality.HIGH, ServiceSpeed.FAST, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchCatalogServices_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Service> page = new PageImpl<>(List.of(testService), pageable, 1);
        PageResponse<ServiceResponse> expectedPageResponse = PageResponse.<ServiceResponse>builder()
                .content(List.of(testServiceResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(serviceRepository.searchCatalogServices("Instagram", pageable)).thenReturn(page);
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);
        doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

        // When
        PageResponse<ServiceResponse> result = catalogService.searchCatalogServices("Instagram", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    // ============ ADMIN LISTING TESTS ============

    @Test
    void getAll_Success() {
        // Given
        when(serviceRepository.findAll()).thenReturn(List.of(testService));
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);

        // When
        List<ServiceResponse> result = catalogService.getAll();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getAllSummaries_Success() {
        // Given
        ServiceSummary summary = ServiceSummary.builder()
                .id(1)
                .name("Instagram Followers - Premium")
                .build();
        when(serviceRepository.findAll()).thenReturn(List.of(testService));
        when(serviceMapper.toSummaryList(List.of(testService))).thenReturn(List.of(summary));

        // When
        List<ServiceSummary> result = catalogService.getAllSummaries();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getByProviderServiceId_Success() {
        // Given
        when(serviceRepository.findByProviderServiceId(1)).thenReturn(List.of(testService));
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);

        // When
        List<ServiceResponse> result = catalogService.getByProviderServiceId(1);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getByProviderId_Success() {
        // Given
        when(serviceRepository.findByProviderId(1)).thenReturn(List.of(testService));
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);

        // When
        List<ServiceResponse> result = catalogService.getByProviderId(1);

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATUS OPERATIONS TESTS ============

    @Test
    void toggleActive_FromActiveToInactive() {
        // Given
        testService.setIsActive(true);
        when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));
        when(serviceMapper.enrichWithProfitMargin(any(Service.class))).thenReturn(
                ServiceResponse.builder().id(1).isActive(false).build()
        );

        // When
        catalogService.toggleActive(1);

        // Then
        verify(serviceRepository).save(argThat(s -> !s.getIsActive()));
    }

    @Test
    void toggleActive_FromInactiveToActive() {
        // Given
        testService.setIsActive(false);
        when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));
        when(serviceMapper.enrichWithProfitMargin(any(Service.class))).thenReturn(
                ServiceResponse.builder().id(1).isActive(true).build()
        );

        // When
        catalogService.toggleActive(1);

        // Then
        verify(serviceRepository).save(argThat(s -> s.getIsActive()));
    }

    @Test
    void activate_Success() {
        // Given
        testService.setIsActive(false);
        when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);
        when(serviceMapper.enrichWithProfitMargin(any(Service.class))).thenReturn(testServiceResponse);

        // When
        catalogService.activate(1);

        // Then
        verify(serviceRepository).save(argThat(s -> s.getIsActive()));
    }

    @Test
    void deactivate_Success() {
        // Given
        testService.setIsActive(true);
        when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);
        when(serviceMapper.enrichWithProfitMargin(any(Service.class))).thenReturn(testServiceResponse);

        // When
        catalogService.deactivate(1);

        // Then
        verify(serviceRepository).save(argThat(s -> !s.getIsActive()));
    }

    // ============ PRICE OPERATIONS TESTS ============

    @Test
    void updatePrice_Success() {
        // Given
        BigDecimal newPrice = new BigDecimal("5.00");
        when(serviceRepository.findById(1)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);
        when(serviceMapper.enrichWithProfitMargin(any(Service.class))).thenReturn(testServiceResponse);

        // When
        catalogService.updatePrice(1, newPrice);

        // Then
        verify(serviceRepository).save(argThat(s -> s.getPricePerK().equals(newPrice)));
    }

    @Test
    void getByPriceRange_Success() {
        // Given
        BigDecimal minPrice = new BigDecimal("1.00");
        BigDecimal maxPrice = new BigDecimal("5.00");
        when(serviceRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(List.of(testService));
        when(serviceMapper.enrichWithProfitMargin(testService)).thenReturn(testServiceResponse);

        // When
        List<ServiceResponse> result = catalogService.getByPriceRange(minPrice, maxPrice);

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATISTICS TESTS ============

    @Test
    void countActive_Success() {
        // Given
        when(serviceRepository.countActiveServices()).thenReturn(10L);

        // When
        long count = catalogService.countActive();

        // Then
        assertThat(count).isEqualTo(10L);
    }

    @Test
    void countActiveByCategory_Success() {
        // Given
        when(serviceRepository.countActiveByCategoryId(1)).thenReturn(5L);

        // When
        long count = catalogService.countActiveByCategory(1);

        // Then
        assertThat(count).isEqualTo(5L);
    }
}
