package com.antipanel.backend.controller;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.order.OrderCreateRequest;
import com.antipanel.backend.dto.order.OrderDetailResponse;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.security.CurrentUser;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.OrderService;
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
 * REST Controller for user order operations.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "User order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order",
            description = "Creates a new order for a service. Validates quantity limits and user balance. " +
                    "Submits the order to the external provider after creation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Service not found"),
            @ApiResponse(responseCode = "502", description = "Provider API error (order refunded)")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody OrderCreateRequest request) {
        log.debug("Creating order for user ID: {} - service: {}", currentUser.getUserId(), request.getServiceId());

        // Step 1: Create order and deduct balance (main transaction)
        // This commits the transaction, releasing the user lock
        OrderResponse created = orderService.create(currentUser.getUserId(), request);

        // Step 2: Submit to external provider (separate transaction via REQUIRES_NEW)
        // If this fails, compensation is automatically triggered and ProviderApiException is thrown
        OrderResponse submitted = orderService.submitOrderToProvider(created.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(submitted);
    }

    @Operation(summary = "Get user's orders with pagination",
            description = "Returns paginated list of orders for the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getUserOrders(
            @CurrentUser CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Getting orders for user ID: {}", currentUser.getUserId());
        PageResponse<OrderResponse> response = orderService.getByUserPaginated(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order by ID",
            description = "Returns detailed information about a specific order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - order belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrder(
            @CurrentUser CustomUserDetails currentUser,
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        log.debug("Getting order {} for user ID: {}", id, currentUser.getUserId());
        OrderDetailResponse response = orderService.getDetailById(id);
        // Security: verify the order belongs to the current user
        if (!response.getUser().getId().equals(currentUser.getUserId())) {
            log.warn("User {} attempted to access order {} belonging to user {}",
                    currentUser.getUserId(), id, response.getUser().getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user's active orders",
            description = "Returns all orders that are currently in progress (pending, processing, in_progress)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active orders retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/active")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(@CurrentUser CustomUserDetails currentUser) {
        log.debug("Getting active orders for user ID: {}", currentUser.getUserId());
        List<OrderResponse> response = orderService.getActiveByUser(currentUser.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user's refillable orders",
            description = "Returns orders that are eligible for refill (completed with refill guarantee)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refillable orders retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/refillable")
    public ResponseEntity<List<OrderResponse>> getRefillableOrders(@CurrentUser CustomUserDetails currentUser) {
        log.debug("Getting refillable orders for user ID: {}", currentUser.getUserId());
        List<OrderResponse> response = orderService.getRefillableByUser(currentUser.getUserId());
        return ResponseEntity.ok(response);
    }
}
