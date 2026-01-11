package com.antipanel.backend.scheduler;

import com.antipanel.backend.service.BalanceHoldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background scheduler for cleaning up expired balance holds.
 * Releases holds that have exceeded their expiration time, refunding users.
 *
 * Uses fixedDelay to ensure only one cleanup runs at a time,
 * waiting for the previous execution to complete before starting the next.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceHoldCleanupTask {

    private final BalanceHoldService balanceHoldService;

    /**
     * Releases expired balance holds every 5 minutes.
     * Expired holds indicate orders that failed silently or timed out.
     *
     * The cleanup method:
     * - Finds holds with status HELD that have exceeded expiresAt
     * - Refunds the held amount to user balance
     * - Updates hold status to RELEASED
     */
    @Scheduled(fixedDelayString = "${app.scheduler.balance-hold-cleanup.delay:300000}")
    public void releaseExpiredHolds() {
        try {
            log.debug("Starting expired balance hold cleanup...");
            int released = balanceHoldService.releaseExpiredHolds();
            if (released > 0) {
                log.info("Released {} expired balance holds", released);
            }
        } catch (Exception e) {
            log.error("Balance hold cleanup failed", e);
            // Don't rethrow - let scheduler continue on next iteration
        }
    }
}
