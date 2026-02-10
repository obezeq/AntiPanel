package com.antipanel.backend.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a single analytic metric.
 * Used for global analytics dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single analytic metric with title and amount")
public class AnalyticResponse {

    @Schema(description = "Title of the analytic metric", example = "Money Spent")
    private String title;

    @Schema(description = "Numeric value of the metric", example = "15420.50")
    private BigDecimal amount;
}
