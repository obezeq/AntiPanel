package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorCreateRequest;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorResponse;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorUpdateRequest;
import com.antipanel.backend.service.PaymentProcessorService;
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
 * Admin REST Controller for payment processor management.
 * Requires ADMIN role for all operations.
 */
@RestController
@RequestMapping("/api/v1/admin/payment-processors")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Payment Processors", description = "Admin payment processor management endpoints")
public class AdminPaymentProcessorController {

    private final PaymentProcessorService paymentProcessorService;

    @Operation(summary = "Get all payment processors",
            description = "Returns all payment processors")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment processors retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentProcessorResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping
    public ResponseEntity<List<PaymentProcessorResponse>> getAllPaymentProcessors() {
        log.debug("Admin: Getting all payment processors");
        List<PaymentProcessorResponse> response = paymentProcessorService.getAll();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all active payment processors",
            description = "Returns all active payment processors")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active payment processors retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentProcessorResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/active")
    public ResponseEntity<List<PaymentProcessorResponse>> getAllActivePaymentProcessors() {
        log.debug("Admin: Getting all active payment processors");
        List<PaymentProcessorResponse> response = paymentProcessorService.getAllActive();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payment processor by ID",
            description = "Returns detailed payment processor information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment processor found",
                    content = @Content(schema = @Schema(implementation = PaymentProcessorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Payment processor not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentProcessorResponse> getPaymentProcessorById(
            @Parameter(description = "Payment Processor ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Getting payment processor by ID: {}", id);
        PaymentProcessorResponse response = paymentProcessorService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new payment processor",
            description = "Creates a new payment processor configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment processor created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentProcessorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PostMapping
    public ResponseEntity<PaymentProcessorResponse> createPaymentProcessor(
            @Valid @RequestBody PaymentProcessorCreateRequest request) {
        log.debug("Admin: Creating new payment processor: {}", request.getName());
        PaymentProcessorResponse response = paymentProcessorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update payment processor",
            description = "Updates payment processor configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment processor updated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentProcessorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Payment processor not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PaymentProcessorResponse> updatePaymentProcessor(
            @Parameter(description = "Payment Processor ID", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody PaymentProcessorUpdateRequest request) {
        log.debug("Admin: Updating payment processor ID: {}", id);
        PaymentProcessorResponse response = paymentProcessorService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete payment processor",
            description = "Deletes a payment processor")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Payment processor deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Payment processor not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete payment processor with invoices")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentProcessor(
            @Parameter(description = "Payment Processor ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Deleting payment processor ID: {}", id);
        paymentProcessorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle payment processor active status",
            description = "Enables or disables a payment processor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment processor status toggled successfully",
                    content = @Content(schema = @Schema(implementation = PaymentProcessorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Payment processor not found")
    })
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<PaymentProcessorResponse> toggleActive(
            @Parameter(description = "Payment Processor ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Toggling active status for payment processor ID: {}", id);
        PaymentProcessorResponse response = paymentProcessorService.toggleActive(id);
        return ResponseEntity.ok(response);
    }
}
