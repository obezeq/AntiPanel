package com.antipanel.backend.scheduler;

import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.repository.InvoiceRepository;
import com.antipanel.backend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Background scheduler for polling pending Paymento payments.
 * Periodically checks PROCESSING invoices for payment completion.
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
     * Polls all PROCESSING invoices every 30 seconds.
     * Checks each one against Paymento verify API.
     */
    @Scheduled(fixedRate = 30000)
    public void pollProcessingPayments() {
        List<Invoice> processing = invoiceRepository.findByStatusOrderByCreatedAtDesc(InvoiceStatus.PROCESSING);

        if (processing.isEmpty()) {
            return;
        }

        log.debug("Polling {} processing invoices for payment status", processing.size());

        for (Invoice invoice : processing) {
            try {
                invoiceService.checkPaymentStatus(invoice.getId());
            } catch (Exception e) {
                log.warn("Failed to check payment status for invoice {}", invoice.getId(), e);
            }
        }
    }
}
