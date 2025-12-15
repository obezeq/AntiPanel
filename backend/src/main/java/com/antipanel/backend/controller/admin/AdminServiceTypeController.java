package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.servicetype.ServiceTypeCreateRequest;
import com.antipanel.backend.dto.servicetype.ServiceTypeResponse;
import com.antipanel.backend.dto.servicetype.ServiceTypeUpdateRequest;
import com.antipanel.backend.service.ServiceTypeService;
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
 * Admin REST Controller for service type management.
 * Requires ADMIN role for all operations.
 */
@RestController
@RequestMapping("/api/v1/admin/service-types")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Service Types", description = "Admin service type management endpoints")
public class AdminServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @Operation(summary = "Get all service types by category",
            description = "Returns all service types for a category including inactive, sorted by sort order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service types retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceTypeResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ServiceTypeResponse>> getServiceTypesByCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer categoryId) {
        log.debug("Admin: Getting all service types for category ID: {}", categoryId);
        List<ServiceTypeResponse> response = serviceTypeService.getAllByCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get service type by ID",
            description = "Returns detailed service type information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service type found",
                    content = @Content(schema = @Schema(implementation = ServiceTypeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Service type not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceTypeResponse> getServiceTypeById(
            @Parameter(description = "Service Type ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Getting service type by ID: {}", id);
        ServiceTypeResponse response = serviceTypeService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new service type",
            description = "Creates a new service type")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Service type created successfully",
                    content = @Content(schema = @Schema(implementation = ServiceTypeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "409", description = "Service type with same slug already exists")
    })
    @PostMapping
    public ResponseEntity<ServiceTypeResponse> createServiceType(
            @Valid @RequestBody ServiceTypeCreateRequest request) {
        log.debug("Admin: Creating new service type: {}", request.getName());
        ServiceTypeResponse response = serviceTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update service type",
            description = "Updates service type information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service type updated successfully",
                    content = @Content(schema = @Schema(implementation = ServiceTypeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Service type not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ServiceTypeResponse> updateServiceType(
            @Parameter(description = "Service Type ID", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody ServiceTypeUpdateRequest request) {
        log.debug("Admin: Updating service type ID: {}", id);
        ServiceTypeResponse response = serviceTypeService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete service type",
            description = "Deletes a service type")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Service type deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Service type not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete service type with services")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceType(
            @Parameter(description = "Service Type ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Deleting service type ID: {}", id);
        serviceTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle service type active status",
            description = "Enables or disables a service type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service type status toggled successfully",
                    content = @Content(schema = @Schema(implementation = ServiceTypeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Service type not found")
    })
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ServiceTypeResponse> toggleActive(
            @Parameter(description = "Service Type ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Toggling active status for service type ID: {}", id);
        ServiceTypeResponse response = serviceTypeService.toggleActive(id);
        return ResponseEntity.ok(response);
    }

}
