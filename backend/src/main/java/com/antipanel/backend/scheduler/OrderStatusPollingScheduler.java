package com.antipanel.backend.scheduler;

import com.antipanel.backend.service.ExternalOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background scheduler for polling order status updates from providers.
 * Periodically checks PENDING, PROCESSING, and IN_PROGRESS orders for status changes.
 *
 * Uses fixedDelay to ensure only one polling batch runs at a time,
 * waiting for the previous execution to complete before starting the next.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusPollingScheduler {

    private final ExternalOrderService externalOrderService;

    /**
     * Polls order statuses from providers every 2 minutes (configurable).
     * Uses fixedDelay to wait for completion before next execution.
     *
     * The batch update method:
     * - Finds orders with status PENDING, PROCESSING, or IN_PROGRESS
     * - Groups them by provider for efficient batch API calls
     * - Updates local status based on provider response
     */
    @Scheduled(fixedDelayString = "${app.scheduler.order-status.delay:120000}")
    public void pollOrderStatuses() {
        try {
            log.info("Starting order status polling...");
            int updated = externalOrderService.batchUpdateOrderStatuses(100);
            log.info("Order status polling completed. Updated {} orders", updated);
        } catch (Exception e) {
            log.error("Order status polling failed", e);
            // Don't rethrow - let scheduler continue on next iteration
        }
    }
}
