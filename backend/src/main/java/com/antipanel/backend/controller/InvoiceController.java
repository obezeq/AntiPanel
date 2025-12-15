package com.antipanel.backend.controller;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.invoice.InvoiceCreateRequest;
import com.antipanel.backend.dto.invoice.InvoiceResponse;
import com.antipanel.backend.security.CurrentUser;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.InvoiceService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for user invoice operations.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoices", description = "User invoice and deposit management endpoints")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "Create a deposit invoice",
            description = "Creates a new invoice for depositing funds to user balance")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invoice created successfully",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Payment processor not found")
    })
    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody InvoiceCreateRequest request) {
        log.debug("Creating invoice for user ID: {} - amount: {}", currentUser.getUserId(), request.getAmount());
        InvoiceResponse response = invoiceService.create(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get user's invoices with pagination",
            description = "Returns paginated list of invoices for the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<PageResponse<InvoiceResponse>> getUserInvoices(
            @CurrentUser CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Getting invoices for user ID: {}", currentUser.getUserId());
        PageResponse<InvoiceResponse> response = invoiceService.getByUserPaginated(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get invoice by ID",
            description = "Returns detailed information about a specific invoice")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice found",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - invoice belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @CurrentUser CustomUserDetails currentUser,
            @Parameter(description = "Invoice ID", example = "1")
            @PathVariable Long id) {
        log.debug("Getting invoice {} for user ID: {}", id, currentUser.getUserId());
        InvoiceResponse response = invoiceService.getById(id);
        // Security: verify the invoice belongs to the current user
        if (!response.getUser().getId().equals(currentUser.getUserId())) {
            log.warn("User {} attempted to access invoice {} belonging to user {}",
                    currentUser.getUserId(), id, response.getUser().getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user's pending invoices",
            description = "Returns all invoices with pending payment status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending invoices retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = InvoiceResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/pending")
    public ResponseEntity<List<InvoiceResponse>> getPendingInvoices(@CurrentUser CustomUserDetails currentUser) {
        log.debug("Getting pending invoices for user ID: {}", currentUser.getUserId());
        List<InvoiceResponse> response = invoiceService.getPendingByUser(currentUser.getUserId());
        return ResponseEntity.ok(response);
    }
}
