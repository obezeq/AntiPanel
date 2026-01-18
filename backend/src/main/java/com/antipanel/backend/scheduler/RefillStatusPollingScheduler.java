package com.antipanel.backend.scheduler;

import com.antipanel.backend.dto.provider.api.DripfeedRefillStatusResponse;
import com.antipanel.backend.entity.OrderRefill;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.entity.enums.RefillStatus;
import com.antipanel.backend.repository.OrderRefillRepository;
import com.antipanel.backend.service.OrderRefillService;
import com.antipanel.backend.service.provider.ProviderApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Background scheduler for polling refill status updates from providers.
 * Periodically checks PROCESSING refills for status changes.
 *
 * Uses fixedDelay to ensure only one polling batch runs at a time,
 * waiting for the previous execution to complete before starting the next.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefillStatusPollingScheduler {

    private final OrderRefillRepository orderRefillRepository;
    private final OrderRefillService orderRefillService;
    private final ProviderApiClient providerApiClient;

    /**
     * Polls refill statuses from providers every 2 minutes (configurable).
     * Uses fixedDelay to wait for completion before next execution.
     *
     * The polling process:
     * - Finds refills with status PROCESSING
     * - For each, queries provider for current status
     * - Updates local status based on provider response
     */
    @Scheduled(fixedDelayString = "${app.scheduler.refill-status.delay:120000}")
    public void pollRefillStatuses() {
        try {
            log.info("Starting refill status polling...");
            int updated = batchUpdateRefillStatuses(100);
            log.info("Refill status polling completed. Updated {} refills", updated);
        } catch (Exception e) {
            log.error("Refill status polling failed", e);
            // Don't rethrow - let scheduler continue on next iteration
        }
    }

    /**
     * Batch update refill statuses from provider.
     *
     * @param limit Maximum number of refills to process
     * @return Number of refills updated
     */
    private int batchUpdateRefillStatuses(int limit) {
        // Find all PROCESSING refills
        List<OrderRefill> processingRefills = orderRefillRepository
                .findByStatusOrderByCreatedAtDesc(RefillStatus.PROCESSING);

        if (processingRefills.isEmpty()) {
            log.debug("No processing refills to update");
            return 0;
        }

        // Limit the batch size
        List<OrderRefill> refillsToProcess = processingRefills.stream()
                .limit(limit)
                .toList();

        log.debug("Processing {} refills", refillsToProcess.size());

        int updatedCount = 0;

        for (OrderRefill refill : refillsToProcess) {
            try {
                if (updateRefillStatus(refill)) {
                    updatedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to update refill ID {}: {}", refill.getId(), e.getMessage());
                // Continue with other refills
            }
        }

        return updatedCount;
    }

    /**
     * Update a single refill's status from the provider.
     *
     * @param refill The refill to update
     * @return true if status was updated
     */
    private boolean updateRefillStatus(OrderRefill refill) {
        if (refill.getProviderRefillId() == null) {
            log.warn("Refill ID {} has no provider refill ID, skipping", refill.getId());
            return false;
        }

        // Get provider from the associated order
        Provider provider = refill.getOrder().getProviderService().getProvider();

        // Query provider for current status
        DripfeedRefillStatusResponse response = providerApiClient.getRefillStatus(
                provider, refill.getProviderRefillId()
        );

        // Map provider status to internal status
        RefillStatus newStatus = mapProviderStatus(response.getStatus());

        if (newStatus == null) {
            log.warn("Unknown refill status '{}' from provider for refill ID {}",
                    response.getStatus(), refill.getId());
            return false;
        }

        // Only update if status changed
        if (newStatus != refill.getStatus()) {
            log.info("Updating refill ID {} status from {} to {}",
                    refill.getId(), refill.getStatus(), newStatus);

            if (newStatus == RefillStatus.COMPLETED) {
                orderRefillService.completeRefill(refill.getId());
            } else if (newStatus == RefillStatus.REJECTED) {
                orderRefillService.rejectRefill(refill.getId());
            } else {
                orderRefillService.updateStatus(refill.getId(), newStatus);
            }
            return true;
        }

        return false;
    }

    /**
     * Maps provider status string to internal RefillStatus enum.
     *
     * Provider statuses: Pending, In progress, Completed, Rejected, Error
     */
    private RefillStatus mapProviderStatus(String providerStatus) {
        if (providerStatus == null) {
            return null;
        }

        return switch (providerStatus.toLowerCase()) {
            case "pending" -> RefillStatus.PENDING;
            case "in progress" -> RefillStatus.PROCESSING;
            case "completed" -> RefillStatus.COMPLETED;
            case "rejected", "error" -> RefillStatus.REJECTED;
            default -> null;
        };
    }
}
