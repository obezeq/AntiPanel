package com.antipanel.backend.scheduler;

import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.repository.InvoiceRepository;
import com.antipanel.backend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Background scheduler for polling pending Paymento payments.
 * Periodically checks PENDING and PROCESSING invoices for payment completion.
 *
 * This is a fallback mechanism for when webhooks are unavailable
 * (e.g., localhost development without HTTPS).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentPollingScheduler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    /**
     * Polls all eligible invoices every 30 seconds.
     * Uses fixedDelay to ensure previous poll completes before next starts.
     * Includes both PENDING and PROCESSING invoices with payment tokens.
     */
    @Scheduled(fixedDelay = 30000)
    public void pollProcessingPayments() {
        List<Invoice> eligible = invoiceRepository.findInvoicesEligibleForPolling();

        if (eligible.isEmpty()) {
            return;
        }

        log.debug("Polling {} eligible invoices for payment status", eligible.size());

        for (Invoice invoice : eligible) {
            try {
                invoiceService.checkPaymentStatus(invoice.getId());
            } catch (Exception e) {
                log.warn("Failed to check payment status for invoice {}", invoice.getId(), e);
            }
        }
    }
}
