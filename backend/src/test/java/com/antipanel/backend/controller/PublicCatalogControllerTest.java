package com.antipanel.backend.controller;

import com.antipanel.backend.dto.category.CategoryResponse;
import com.antipanel.backend.dto.category.CategorySummary;
import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.service.ServiceDetailResponse;
import com.antipanel.backend.dto.service.ServiceResponse;
import com.antipanel.backend.dto.servicetype.ServiceTypeSummary;
import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.service.CatalogService;
import com.antipanel.backend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublicCatalogController Tests")
class PublicCatalogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private PublicCatalogController publicCatalogController;

    private CategoryResponse categoryResponse;
    private ServiceResponse serviceResponse;
    private ServiceDetailResponse serviceDetailResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicCatalogController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        categoryResponse = CategoryResponse.builder()
                .id(1)
                .name("Instagram Followers")
                .slug("instagram-followers")
                .sortOrder(1)
                .isActive(true)
                .serviceCount(10L)
                .build();

        CategorySummary categorySummary = CategorySummary.builder()
                .id(1)
                .name("Instagram Followers")
                .slug("instagram-followers")
                .build();

        ServiceTypeSummary serviceTypeSummary = ServiceTypeSummary.builder()
                .id(1)
                .name("Followers")
                .slug("followers")
                .build();

        serviceResponse = ServiceResponse.builder()
                .id(1)
                .name("Instagram Followers - Premium")
                .categoryId(1)
                .serviceTypeId(1)
                .pricePerK(new BigDecimal("5.00"))
                .minQuantity(100)
                .maxQuantity(100000)
                .quality(ServiceQuality.HIGH)
                .speed(ServiceSpeed.FAST)
                .isActive(true)
                .build();

        serviceDetailResponse = ServiceDetailResponse.builder()
                .id(1)
                .name("Instagram Followers - Premium")
                .description("High quality Instagram followers")
                .category(categorySummary)
                .serviceType(serviceTypeSummary)
                .pricePerK(new BigDecimal("5.00"))
                .minQuantity(100)
                .maxQuantity(100000)
                .quality(ServiceQuality.HIGH)
                .speed(ServiceSpeed.FAST)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/public/categories")
    class GetCategories {

        @Test
        @DisplayName("Should return all active categories")
        void shouldReturnAllActiveCategories() throws Exception {
            when(categoryService.getAllActive()).thenReturn(List.of(categoryResponse));

            mockMvc.perform(get("/api/v1/public/categories")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Instagram Followers"))
                    .andExpect(jsonPath("$[0].slug").value("instagram-followers"));
        }

        @Test
        @DisplayName("Should return empty list when no categories")
        void shouldReturnEmptyListWhenNoCategories() throws Exception {
            when(categoryService.getAllActive()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/public/categories")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/public/categories/{slug}")
    class GetCategoryBySlug {

        @Test
        @DisplayName("Should return category by slug")
        void shouldReturnCategoryBySlug() throws Exception {
            when(categoryService.getBySlug("instagram-followers")).thenReturn(categoryResponse);

            mockMvc.perform(get("/api/v1/public/categories/instagram-followers")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Instagram Followers"))
                    .andExpect(jsonPath("$.slug").value("instagram-followers"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent slug")
        void shouldReturn404ForNonExistentSlug() throws Exception {
            when(categoryService.getBySlug("non-existent"))
                    .thenThrow(new ResourceNotFoundException("Category not found with slug: non-existent"));

            mockMvc.perform(get("/api/v1/public/categories/non-existent")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/public/categories/with-counts")
    class GetCategoriesWithCounts {

        @Test
        @DisplayName("Should return categories with service count")
        void shouldReturnCategoriesWithServiceCount() throws Exception {
            when(categoryService.getActiveCategoriesWithServiceCount()).thenReturn(List.of(categoryResponse));

            mockMvc.perform(get("/api/v1/public/categories/with-counts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].serviceCount").value(10));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/public/categories/{categoryId}/services")
    class GetServicesByCategory {

        @Test
        @DisplayName("Should return services for category")
        void shouldReturnServicesForCategory() throws Exception {
            when(catalogService.getActiveCatalogServicesByCategory(1)).thenReturn(List.of(serviceResponse));

            mockMvc.perform(get("/api/v1/public/categories/1/services")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Instagram Followers - Premium"))
                    .andExpect(jsonPath("$[0].categoryId").value(1));
        }

        @Test
        @DisplayName("Should return empty list when no services in category")
        void shouldReturnEmptyListWhenNoServices() throws Exception {
            when(catalogService.getActiveCatalogServicesByCategory(1)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/public/categories/1/services")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/public/services/{id}")
    class GetServiceDetail {

        @Test
        @DisplayName("Should return service detail")
        void shouldReturnServiceDetail() throws Exception {
            when(catalogService.getDetailById(1)).thenReturn(serviceDetailResponse);

            mockMvc.perform(get("/api/v1/public/services/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Instagram Followers - Premium"))
                    .andExpect(jsonPath("$.category.id").value(1))
                    .andExpect(jsonPath("$.serviceType.id").value(1));
        }

        @Test
        @DisplayName("Should return 404 for non-existent service")
        void shouldReturn404ForNonExistentService() throws Exception {
            when(catalogService.getDetailById(999))
                    .thenThrow(new ResourceNotFoundException("Service not found with ID: 999"));

            mockMvc.perform(get("/api/v1/public/services/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/public/services")
    class GetAllActiveServices {

        @Test
        @DisplayName("Should return all active services")
        void shouldReturnAllActiveServices() throws Exception {
            when(catalogService.getActiveCatalogServices()).thenReturn(List.of(serviceResponse));

            mockMvc.perform(get("/api/v1/public/services")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].isActive").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/public/services/search")
    class SearchServices {

        @Test
        @DisplayName("Should search services with filters")
        void shouldSearchServicesWithFilters() throws Exception {
            PageResponse<ServiceResponse> pageResponse = PageResponse.<ServiceResponse>builder()
                    .content(List.of(serviceResponse))
                    .totalElements(1L)
                    .totalPages(1)
                    .pageNumber(0)
                    .pageSize(20)
                    .first(true)
                    .last(true)
                    .build();

            when(catalogService.getCatalogServicesFiltered(
                    eq(1), eq(1), eq(ServiceQuality.HIGH), eq(ServiceSpeed.FAST), any(Pageable.class)))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/public/services/search")
                            .param("categoryId", "1")
                            .param("serviceTypeId", "1")
                            .param("quality", "HIGH")
                            .param("speed", "FAST")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Should search services by text")
        void shouldSearchServicesByText() throws Exception {
            PageResponse<ServiceResponse> pageResponse = PageResponse.<ServiceResponse>builder()
                    .content(List.of(serviceResponse))
                    .totalElements(1L)
                    .totalPages(1)
                    .pageNumber(0)
                    .pageSize(20)
                    .first(true)
                    .last(true)
                    .build();

            when(catalogService.searchCatalogServices(eq("Instagram"), any(Pageable.class)))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/public/services/search")
                            .param("search", "Instagram")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Instagram Followers - Premium"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/public/categories/{categoryId}/types/{serviceTypeId}/services")
    class GetServicesByCategoryAndType {

        @Test
        @DisplayName("Should return services by category and type")
        void shouldReturnServicesByCategoryAndType() throws Exception {
            when(catalogService.getActiveCatalogServicesByCategoryAndType(1, 1))
                    .thenReturn(List.of(serviceResponse));

            mockMvc.perform(get("/api/v1/public/categories/1/types/1/services")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].categoryId").value(1))
                    .andExpect(jsonPath("$[0].serviceTypeId").value(1));
        }
    }
}
