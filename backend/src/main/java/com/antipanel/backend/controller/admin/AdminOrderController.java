package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.order.OrderDetailResponse;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.service.OrderService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin REST Controller for order management.
 * Requires ADMIN or SUPPORT role for read operations.
 * Requires ADMIN role for status updates.
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Orders", description = "Admin order management endpoints")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "Get orders by status with pagination",
            description = "Returns paginated list of orders filtered by status. Use this to browse orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN or SUPPORT role")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<PageResponse<OrderResponse>> getOrdersByStatusPaginated(
            @Parameter(description = "Order status filter", example = "PENDING")
            @RequestParam(required = false, defaultValue = "PENDING") OrderStatus status,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Admin: Getting orders by status {} with pagination", status);
        PageResponse<OrderResponse> response = orderService.getByStatusPaginated(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order by ID",
            description = "Returns detailed order information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN or SUPPORT role"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<OrderDetailResponse> getOrderById(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        log.debug("Admin: Getting order by ID: {}", id);
        OrderDetailResponse response = orderService.getDetailById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get orders by status",
            description = "Returns all orders with a specific status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN or SUPPORT role")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<PageResponse<OrderResponse>> getOrdersByStatus(
            @Parameter(description = "Order status", example = "PENDING")
            @PathVariable OrderStatus status,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Admin: Getting orders by status: {}", status);
        PageResponse<OrderResponse> response = orderService.getByStatusPaginated(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get orders for a user",
            description = "Returns all orders for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN or SUPPORT role")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<PageResponse<OrderResponse>> getOrdersByUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Admin: Getting orders for user ID: {}", userId);
        PageResponse<OrderResponse> response = orderService.getByUserPaginated(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get pending orders",
            description = "Returns all orders with PENDING status (awaiting processing)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending orders retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN or SUPPORT role")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<OrderResponse>> getPendingOrders() {
        log.debug("Admin: Getting all pending orders");
        List<OrderResponse> response = orderService.getByStatus(OrderStatus.PENDING);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update order status",
            description = "Updates the status of an order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "New status", example = "COMPLETED")
            @RequestParam OrderStatus status) {
        log.debug("Admin: Updating order {} status to: {}", id, status);
        OrderResponse response = orderService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel order with refund",
            description = "Cancels an order and refunds the user's balance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled and refunded successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        log.debug("Admin: Cancelling order {}", id);
        OrderResponse response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update order progress",
            description = "Updates the progress of an order (start count and remaining)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order progress updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid progress values"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id}/progress")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateProgress(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Start count", example = "1000")
            @RequestParam Integer startCount,
            @Parameter(description = "Remaining count", example = "500")
            @RequestParam Integer remains) {
        log.debug("Admin: Updating progress for order {} - startCount: {}, remains: {}", id, startCount, remains);
        OrderResponse response = orderService.updateProgress(id, startCount, remains);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refund order",
            description = "Issues a full refund for an order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order refunded successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Order cannot be refunded"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> refundOrder(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        log.debug("Admin: Refunding order {}", id);
        OrderResponse response = orderService.refundOrder(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Complete order",
            description = "Marks an order as completed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order completed successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Order cannot be completed"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> completeOrder(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        log.debug("Admin: Completing order {}", id);
        OrderResponse response = orderService.completeOrder(id);
        return ResponseEntity.ok(response);
    }
}
