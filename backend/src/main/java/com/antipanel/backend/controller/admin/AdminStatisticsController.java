package com.antipanel.backend.controller.admin;

import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.repository.InvoiceRepository;
import com.antipanel.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin REST Controller for platform statistics.
 * Requires ADMIN role for all operations.
 */
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Statistics", description = "Admin platform statistics endpoints")
public class AdminStatisticsController {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get order statistics",
            description = "Returns aggregated order statistics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        log.debug("Admin: Getting order statistics");

        Map<String, Object> stats = new HashMap<>();

        // Count orders by status
        stats.put("totalOrders", orderRepository.count());

        // Convert List<Object[]> to Map for better JSON output
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
        Map<String, Long> ordersByStatus = statusCounts.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
        stats.put("ordersByStatus", ordersByStatus);

        // Revenue statistics
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        BigDecimal totalCost = orderRepository.getTotalCost();
        BigDecimal totalProfit = orderRepository.getTotalProfit();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        stats.put("totalCost", totalCost != null ? totalCost : BigDecimal.ZERO);
        stats.put("totalProfit", totalProfit != null ? totalProfit : BigDecimal.ZERO);

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get order statistics for date range",
            description = "Returns order statistics for a specific date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/orders/range")
    public ResponseEntity<Map<String, Object>> getOrderStatisticsByDateRange(
            @Parameter(description = "Start date", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("Admin: Getting order statistics for range {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Map<String, Object> stats = new HashMap<>();

        // Count orders in range
        long ordersInRange = orderRepository.findOrdersBetweenDates(start, end).size();
        stats.put("ordersInRange", ordersInRange);

        // Revenue in range (using completed orders - paidAt/completedAt)
        BigDecimal revenueInRange = orderRepository.getRevenueBetweenDates(start, end);
        BigDecimal profitInRange = orderRepository.getProfitBetweenDates(start, end);
        stats.put("revenueInRange", revenueInRange != null ? revenueInRange : BigDecimal.ZERO);
        stats.put("profitInRange", profitInRange != null ? profitInRange : BigDecimal.ZERO);

        stats.put("startDate", startDate);
        stats.put("endDate", endDate);

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get invoice statistics",
            description = "Returns aggregated invoice (deposit) statistics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/invoices")
    public ResponseEntity<Map<String, Object>> getInvoiceStatistics() {
        log.debug("Admin: Getting invoice statistics");

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalInvoices", invoiceRepository.count());

        // Count by each status
        Map<String, Long> invoicesByStatus = new HashMap<>();
        for (InvoiceStatus status : InvoiceStatus.values()) {
            invoicesByStatus.put(status.name(), invoiceRepository.countByStatus(status));
        }
        stats.put("invoicesByStatus", invoicesByStatus);

        BigDecimal totalDeposits = invoiceRepository.getTotalRevenue();
        stats.put("totalDeposits", totalDeposits != null ? totalDeposits : BigDecimal.ZERO);

        stats.put("pendingInvoices", invoiceRepository.countByStatus(InvoiceStatus.PENDING));

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get user statistics",
            description = "Returns aggregated user statistics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        log.debug("Admin: Getting user statistics");

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("bannedUsers", userRepository.findByIsBannedTrue().size());

        BigDecimal totalBalance = userRepository.getTotalUserBalance();
        stats.put("totalUserBalance", totalBalance != null ? totalBalance : BigDecimal.ZERO);

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get dashboard summary",
            description = "Returns a summary of key platform metrics for the admin dashboard")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        log.debug("Admin: Getting dashboard summary");

        Map<String, Object> summary = new HashMap<>();

        // User metrics
        summary.put("totalUsers", userRepository.count());
        summary.put("bannedUsers", userRepository.findByIsBannedTrue().size());

        // Order metrics
        summary.put("totalOrders", orderRepository.count());
        summary.put("pendingOrders", orderRepository.countByStatus(OrderStatus.PENDING));

        // Revenue metrics
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        BigDecimal totalProfit = orderRepository.getTotalProfit();
        summary.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        summary.put("totalProfit", totalProfit != null ? totalProfit : BigDecimal.ZERO);

        // Invoice metrics
        BigDecimal totalDeposits = invoiceRepository.getTotalRevenue();
        summary.put("totalDeposits", totalDeposits != null ? totalDeposits : BigDecimal.ZERO);
        summary.put("pendingInvoices", invoiceRepository.countByStatus(InvoiceStatus.PENDING));

        // Today's metrics
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        summary.put("ordersToday", orderRepository.findOrdersBetweenDates(todayStart, todayEnd).size());

        BigDecimal revenueToday = orderRepository.getRevenueBetweenDates(todayStart, todayEnd);
        summary.put("revenueToday", revenueToday != null ? revenueToday : BigDecimal.ZERO);

        return ResponseEntity.ok(summary);
    }
}
