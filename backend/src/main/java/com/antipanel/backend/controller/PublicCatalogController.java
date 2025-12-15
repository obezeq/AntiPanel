package com.antipanel.backend.controller;

import com.antipanel.backend.dto.category.CategoryResponse;
import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.service.ServiceDetailResponse;
import com.antipanel.backend.dto.service.ServiceResponse;
import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import com.antipanel.backend.service.CatalogService;
import com.antipanel.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for public catalog access.
 * All endpoints are accessible without authentication.
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Catalog", description = "Public endpoints for browsing services catalog")
public class PublicCatalogController {

    private final CategoryService categoryService;
    private final CatalogService catalogService;

    // ============ CATEGORY ENDPOINTS ============

    @Operation(summary = "Get all active categories",
            description = "Returns all active categories sorted by sort order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
    })
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        log.debug("Getting all active categories");
        List<CategoryResponse> categories = categoryService.getAllActive();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Get category by slug",
            description = "Returns a single category by its URL-friendly slug")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/categories/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @Parameter(description = "Category URL slug", example = "instagram-followers")
            @PathVariable String slug) {
        log.debug("Getting category by slug: {}", slug);
        CategoryResponse category = categoryService.getBySlug(slug);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "Get active categories with service count",
            description = "Returns all active categories with the count of active services in each")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
    })
    @GetMapping("/categories/with-counts")
    public ResponseEntity<List<CategoryResponse>> getActiveCategoriesWithServiceCount() {
        log.debug("Getting active categories with service count");
        List<CategoryResponse> categories = categoryService.getActiveCategoriesWithServiceCount();
        return ResponseEntity.ok(categories);
    }

    // ============ SERVICE ENDPOINTS ============

    @Operation(summary = "Get services by category",
            description = "Returns all active services for a specific category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/categories/{categoryId}/services")
    public ResponseEntity<List<ServiceResponse>> getServicesByCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer categoryId) {
        log.debug("Getting services for category: {}", categoryId);
        List<ServiceResponse> services = catalogService.getActiveCatalogServicesByCategory(categoryId);
        return ResponseEntity.ok(services);
    }

    @Operation(summary = "Get service details",
            description = "Returns detailed information about a specific service")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service found",
                    content = @Content(schema = @Schema(implementation = ServiceDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceDetailResponse> getServiceDetail(
            @Parameter(description = "Service ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Getting service detail: {}", id);
        ServiceDetailResponse service = catalogService.getDetailById(id);
        return ResponseEntity.ok(service);
    }

    @Operation(summary = "Get all active services",
            description = "Returns all active services from the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceResponse.class))))
    })
    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> getAllActiveServices() {
        log.debug("Getting all active services");
        List<ServiceResponse> services = catalogService.getActiveCatalogServices();
        return ResponseEntity.ok(services);
    }

    @Operation(summary = "Search and filter services",
            description = "Search catalog services with optional filters for category, type, quality, and speed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/services/search")
    public ResponseEntity<PageResponse<ServiceResponse>> searchServices(
            @Parameter(description = "Category ID filter")
            @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "Service type ID filter")
            @RequestParam(required = false) Integer serviceTypeId,
            @Parameter(description = "Service quality filter")
            @RequestParam(required = false) ServiceQuality quality,
            @Parameter(description = "Service speed filter")
            @RequestParam(required = false) ServiceSpeed speed,
            @Parameter(description = "Search term for name/description")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("Searching services with filters - category: {}, type: {}, quality: {}, speed: {}, search: {}",
                categoryId, serviceTypeId, quality, speed, search);

        PageResponse<ServiceResponse> response;
        if (search != null && !search.isBlank()) {
            response = catalogService.searchCatalogServices(search, pageable);
        } else {
            response = catalogService.getCatalogServicesFiltered(categoryId, serviceTypeId, quality, speed, pageable);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get services by category and type",
            description = "Returns active services filtered by both category and service type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceResponse.class))))
    })
    @GetMapping("/categories/{categoryId}/types/{serviceTypeId}/services")
    public ResponseEntity<List<ServiceResponse>> getServicesByCategoryAndType(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer categoryId,
            @Parameter(description = "Service type ID", example = "1")
            @PathVariable Integer serviceTypeId) {
        log.debug("Getting services for category: {} and type: {}", categoryId, serviceTypeId);
        List<ServiceResponse> services = catalogService.getActiveCatalogServicesByCategoryAndType(categoryId, serviceTypeId);
        return ResponseEntity.ok(services);
    }
}
