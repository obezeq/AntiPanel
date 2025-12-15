package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.service.ServiceCreateRequest;
import com.antipanel.backend.dto.service.ServiceDetailResponse;
import com.antipanel.backend.dto.service.ServiceResponse;
import com.antipanel.backend.dto.service.ServiceUpdateRequest;
import com.antipanel.backend.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin REST Controller for service (catalog item) management.
 * Requires ADMIN role for all operations.
 */
@RestController
@RequestMapping("/api/v1/admin/services")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Services", description = "Admin service catalog management endpoints")
public class AdminServiceController {

    private final CatalogService catalogService;

    @Operation(summary = "Get all active catalog services",
            description = "Returns all active services from the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllActiveCatalogServices() {
        log.debug("Admin: Getting all active catalog services");
        List<ServiceResponse> response = catalogService.getActiveCatalogServices();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get services by category",
            description = "Returns all active services in a specific category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ServiceResponse>> getServicesByCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer categoryId) {
        log.debug("Admin: Getting services for category ID: {}", categoryId);
        List<ServiceResponse> response = catalogService.getActiveCatalogServicesByCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get service by ID",
            description = "Returns detailed service information including provider service details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service found",
                    content = @Content(schema = @Schema(implementation = ServiceDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDetailResponse> getServiceById(
            @Parameter(description = "Service ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Getting service by ID: {}", id);
        ServiceDetailResponse response = catalogService.getDetailById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new service",
            description = "Creates a new service in the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Service created successfully",
                    content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Category, service type, or provider service not found")
    })
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody ServiceCreateRequest request) {
        log.debug("Admin: Creating new service: {}", request.getName());
        ServiceResponse response = catalogService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update service",
            description = "Updates service information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service updated successfully",
                    content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @Parameter(description = "Service ID", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody ServiceUpdateRequest request) {
        log.debug("Admin: Updating service ID: {}", id);
        ServiceResponse response = catalogService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete service",
            description = "Deletes a service from the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Service deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Service not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete service with active orders")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(
            @Parameter(description = "Service ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Deleting service ID: {}", id);
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle service active status",
            description = "Enables or disables a service")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service status toggled successfully",
                    content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ServiceResponse> toggleActive(
            @Parameter(description = "Service ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Toggling active status for service ID: {}", id);
        ServiceResponse response = catalogService.toggleActive(id);
        return ResponseEntity.ok(response);
    }
}
