package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.provider.ProviderCreateRequest;
import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.provider.ProviderUpdateRequest;
import com.antipanel.backend.dto.providerservice.ProviderServiceCreateRequest;
import com.antipanel.backend.dto.providerservice.ProviderServiceResponse;
import com.antipanel.backend.dto.providerservice.ProviderServiceUpdateRequest;
import com.antipanel.backend.service.ProviderService;
import com.antipanel.backend.service.ProviderCatalogService;
import com.antipanel.backend.service.ProviderSyncService;
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
 * Admin REST Controller for provider and provider service management.
 * Requires ADMIN role for all operations.
 */
@RestController
@RequestMapping("/api/v1/admin/providers")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Providers", description = "Admin provider management endpoints")
public class AdminProviderController {

    private final ProviderService providerService;
    private final ProviderCatalogService providerCatalogService;
    private final ProviderSyncService providerSyncService;

    // === Provider Endpoints ===

    @Operation(summary = "Get all providers",
            description = "Returns all SMM providers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProviderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping
    public ResponseEntity<List<ProviderResponse>> getAllProviders() {
        log.debug("Admin: Getting all providers");
        List<ProviderResponse> response = providerService.getAll();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all active providers",
            description = "Returns all active providers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active providers retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProviderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/active")
    public ResponseEntity<List<ProviderResponse>> getAllActiveProviders() {
        log.debug("Admin: Getting all active providers");
        List<ProviderResponse> response = providerService.getAllActive();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get provider by ID",
            description = "Returns detailed provider information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Provider found",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getProviderById(
            @Parameter(description = "Provider ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Getting provider by ID: {}", id);
        ProviderResponse response = providerService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new provider",
            description = "Creates a new SMM provider configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Provider created successfully",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PostMapping
    public ResponseEntity<ProviderResponse> createProvider(
            @Valid @RequestBody ProviderCreateRequest request) {
        log.debug("Admin: Creating new provider: {}", request.getName());
        ProviderResponse response = providerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update provider",
            description = "Updates provider configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Provider updated successfully",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProviderResponse> updateProvider(
            @Parameter(description = "Provider ID", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody ProviderUpdateRequest request) {
        log.debug("Admin: Updating provider ID: {}", id);
        ProviderResponse response = providerService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete provider",
            description = "Deletes a provider")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Provider deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete provider with services")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(
            @Parameter(description = "Provider ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Deleting provider ID: {}", id);
        providerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle provider active status",
            description = "Enables or disables a provider")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Provider status toggled successfully",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ProviderResponse> toggleProviderActive(
            @Parameter(description = "Provider ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Toggling active status for provider ID: {}", id);
        ProviderResponse response = providerService.toggleActive(id);
        return ResponseEntity.ok(response);
    }

    // === Provider Sync Endpoints ===

    @Operation(summary = "Sync services from provider",
            description = "Fetches and synchronizes all services from the provider's API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Services synced successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProviderServiceResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "502", description = "Provider API error")
    })
    @PostMapping("/{id}/sync-services")
    public ResponseEntity<List<ProviderServiceResponse>> syncProviderServices(
            @Parameter(description = "Provider ID", example = "1")
            @PathVariable Integer id) {
        log.info("Admin: Syncing services for provider ID: {}", id);
        List<ProviderServiceResponse> response = providerSyncService.syncServices(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Sync balance from provider",
            description = "Fetches and updates the provider's account balance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance synced successfully",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "502", description = "Provider API error")
    })
    @PostMapping("/{id}/sync-balance")
    public ResponseEntity<ProviderResponse> syncProviderBalance(
            @Parameter(description = "Provider ID", example = "1")
            @PathVariable Integer id) {
        log.info("Admin: Syncing balance for provider ID: {}", id);
        ProviderResponse response = providerSyncService.syncBalance(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Full sync from provider",
            description = "Syncs both services and balance from the provider's API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Full sync completed successfully",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "502", description = "Provider API error")
    })
    @PostMapping("/{id}/sync")
    public ResponseEntity<ProviderResponse> syncProvider(
            @Parameter(description = "Provider ID", example = "1")
            @PathVariable Integer id) {
        log.info("Admin: Full sync for provider ID: {}", id);
        ProviderResponse response = providerSyncService.syncAll(id);
        return ResponseEntity.ok(response);
    }

    // === Provider Service Endpoints ===

    @Operation(summary = "Get provider's services",
            description = "Returns all services available from a specific provider")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Provider services retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProviderServiceResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/{providerId}/services")
    public ResponseEntity<List<ProviderServiceResponse>> getProviderServices(
            @Parameter(description = "Provider ID", example = "1")
            @PathVariable Integer providerId) {
        log.debug("Admin: Getting services for provider ID: {}", providerId);
        List<ProviderServiceResponse> response = providerCatalogService.getAllByProvider(providerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get provider service by ID",
            description = "Returns detailed provider service information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Provider service found",
                    content = @Content(schema = @Schema(implementation = ProviderServiceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider service not found")
    })
    @GetMapping("/services/{id}")
    public ResponseEntity<ProviderServiceResponse> getProviderServiceById(
            @Parameter(description = "Provider Service ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Getting provider service by ID: {}", id);
        ProviderServiceResponse response = providerCatalogService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create provider service",
            description = "Creates a new service mapping from a provider")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Provider service created successfully",
                    content = @Content(schema = @Schema(implementation = ProviderServiceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    @PostMapping("/services")
    public ResponseEntity<ProviderServiceResponse> createProviderService(
            @Valid @RequestBody ProviderServiceCreateRequest request) {
        log.debug("Admin: Creating provider service");
        ProviderServiceResponse response = providerCatalogService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update provider service",
            description = "Updates provider service configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Provider service updated successfully",
                    content = @Content(schema = @Schema(implementation = ProviderServiceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider service not found")
    })
    @PutMapping("/services/{id}")
    public ResponseEntity<ProviderServiceResponse> updateProviderService(
            @Parameter(description = "Provider Service ID", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody ProviderServiceUpdateRequest request) {
        log.debug("Admin: Updating provider service ID: {}", id);
        ProviderServiceResponse response = providerCatalogService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete provider service",
            description = "Deletes a provider service mapping")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Provider service deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider service not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete provider service linked to catalog services")
    })
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteProviderService(
            @Parameter(description = "Provider Service ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Deleting provider service ID: {}", id);
        providerCatalogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle provider service active status",
            description = "Enables or disables a provider service")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Provider service status toggled successfully",
                    content = @Content(schema = @Schema(implementation = ProviderServiceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Provider service not found")
    })
    @PatchMapping("/services/{id}/toggle-active")
    public ResponseEntity<ProviderServiceResponse> toggleProviderServiceActive(
            @Parameter(description = "Provider Service ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Toggling active status for provider service ID: {}", id);
        ProviderServiceResponse response = providerCatalogService.toggleActive(id);
        return ResponseEntity.ok(response);
    }
}
