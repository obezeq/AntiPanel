package com.antipanel.backend.controller;

import com.antipanel.backend.dto.analytics.AnalyticResponse;
import com.antipanel.backend.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;

/**
 * REST Controller for global analytics.
 * Provides endpoints for authenticated users to view global statistics.
 * Protected with JWT authentication - accessible to all registered users.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Global analytics endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get global analytics",
            description = "Returns global statistics: Money Spent, Orders Made, and Users Registered. " +
                    "Accessible to all authenticated users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AnalyticResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/analytics")
    public ResponseEntity<List<AnalyticResponse>> getGlobalAnalytics() {
        log.debug("GET /api/v1/analytics - Fetching global analytics");

        List<AnalyticResponse> analytics = analyticsService.getGlobalAnalytics();

        log.debug("Global analytics retrieved: {} metrics", analytics.size());

        return ResponseEntity.ok(analytics);
    }
}
