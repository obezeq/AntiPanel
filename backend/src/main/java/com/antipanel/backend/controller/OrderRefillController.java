package com.antipanel.backend.controller;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillCreateRequest;
import com.antipanel.backend.dto.orderrefill.OrderRefillResponse;
import com.antipanel.backend.security.CurrentUser;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.OrderRefillService;
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
 * REST Controller for user order refill operations.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/v1/refills")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Refills", description = "Order refill request management endpoints")
public class OrderRefillController {

    private final OrderRefillService orderRefillService;

    @Operation(summary = "Create a refill request",
            description = "Creates a new refill request for a completed order with refill guarantee")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Refill request created successfully",
                    content = @Content(schema = @Schema(implementation = OrderRefillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or order not eligible for refill"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Pending refill request already exists")
    })
    @PostMapping
    public ResponseEntity<OrderRefillResponse> createRefill(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody OrderRefillCreateRequest request) {
        log.debug("Creating refill request for user ID: {} - order: {}", currentUser.getUserId(), request.getOrderId());
        OrderRefillResponse response = orderRefillService.create(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get user's refill requests with pagination",
            description = "Returns paginated list of refill requests for the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refill requests retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<PageResponse<OrderRefillResponse>> getUserRefills(
            @CurrentUser CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Getting refill requests for user ID: {}", currentUser.getUserId());
        PageResponse<OrderRefillResponse> response = orderRefillService.getByUserPaginated(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get refill request by ID",
            description = "Returns detailed information about a specific refill request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refill request found",
                    content = @Content(schema = @Schema(implementation = OrderRefillResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - refill belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Refill request not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderRefillResponse> getRefill(
            @CurrentUser CustomUserDetails currentUser,
            @Parameter(description = "Refill ID", example = "1")
            @PathVariable Long id) {
        log.debug("Getting refill {} for user ID: {}", id, currentUser.getUserId());
        OrderRefillResponse response = orderRefillService.getById(id);
        // Security: verify the refill belongs to the current user by checking through user's refills
        List<OrderRefillResponse> userRefills = orderRefillService.getByUser(currentUser.getUserId());
        boolean isOwner = userRefills.stream().anyMatch(r -> r.getId().equals(id));
        if (!isOwner) {
            log.warn("User {} attempted to access refill {} that does not belong to them",
                    currentUser.getUserId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get refills for a specific order",
            description = "Returns all refill requests for a specific order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refill requests retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderRefillResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderRefillResponse>> getRefillsByOrder(
            @CurrentUser CustomUserDetails currentUser,
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long orderId) {
        log.debug("Getting refills for order {} - user ID: {}", orderId, currentUser.getUserId());
        // Note: Security check should be done at service level or here by verifying order ownership
        List<OrderRefillResponse> response = orderRefillService.getByOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
