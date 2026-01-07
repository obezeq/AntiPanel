package com.antipanel.backend.controller;

import com.antipanel.backend.dto.user.UserStatisticsResponse;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.security.CurrentUser;
import com.antipanel.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * REST Controller for user dashboard statistics.
 * Provides endpoints for authenticated users to view their own statistics.
 */
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Statistics", description = "User dashboard statistics endpoints")
public class UserStatisticsController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get user statistics",
            description = "Returns order statistics and balance for the authenticated user's dashboard")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/statistics")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(
            @CurrentUser CustomUserDetails currentUser) {
        log.debug("Getting statistics for user ID: {}", currentUser.getUserId());

        Long userId = currentUser.getUserId();

        // Get order counts
        long totalOrders = orderRepository.countByUserId(userId);
        long pendingOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING)
                + orderRepository.countByUserIdAndStatus(userId, OrderStatus.PROCESSING);
        long completedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.COMPLETED);

        // Get orders this month
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long ordersThisMonth = orderRepository.countByUserIdAndCreatedAtAfter(userId, startOfMonth);

        // Get user balance
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        UserStatisticsResponse response = new UserStatisticsResponse(
                totalOrders,
                pendingOrders,
                completedOrders,
                ordersThisMonth,
                user.getBalance()
        );

        log.debug("Statistics for user {}: total={}, pending={}, completed={}, thisMonth={}",
                userId, totalOrders, pendingOrders, completedOrders, ordersThisMonth);

        return ResponseEntity.ok(response);
    }
}
