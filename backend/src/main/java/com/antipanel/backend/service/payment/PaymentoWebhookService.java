package com.antipanel.backend.service.payment;

import com.antipanel.backend.dto.paymento.PaymentoWebhookPayload;
import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.repository.InvoiceRepository;
import com.antipanel.backend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for processing Paymento webhook (IPN) callbacks.
 * Handles payment status updates and balance changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentoWebhookService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    /**
     * Processes a Paymento webhook callback.
     * Updates invoice status and user balance based on payment status.
     *
     * @param payload Webhook payload from Paymento
     */
    @Transactional
    public void processWebhook(PaymentoWebhookPayload payload) {
        log.info("Processing Paymento webhook - OrderId: {}, Status: {} ({})",
                payload.getOrderId(),
                payload.getOrderStatus(),
                payload.getStatusName());

        // Parse invoice ID from orderId
        Long invoiceId = parseInvoiceId(payload.getOrderId());
        if (invoiceId == null) {
            log.error("Invalid orderId in webhook: {}", payload.getOrderId());
            return;
        }

        // Find invoice
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice == null) {
            log.warn("Invoice not found for ID: {}", invoiceId);
            return;
        }

        // Skip if already in final state (idempotency)
        if (invoice.isFinal()) {
            log.debug("Invoice {} already in final state: {}, skipping", invoiceId, invoice.getStatus());
            return;
        }

        // Update processor invoice ID (token) if not set
        if (invoice.getProcessorInvoiceId() == null && payload.getToken() != null) {
            invoice.setProcessorInvoiceId(payload.getToken());
            invoiceRepository.save(invoice);
            log.debug("Updated invoice {} with token: {}", invoiceId, payload.getToken());
        }

        // Process based on payment status
        if (payload.isSuccessful()) {
            handleSuccessfulPayment(invoice, payload);
        } else if (payload.isFailed()) {
            handleFailedPayment(invoice, payload);
        } else if (payload.isPending()) {
            handlePendingPayment(invoice, payload);
        } else {
            log.warn("Unknown payment status {} for invoice {}", payload.getOrderStatus(), invoiceId);
        }
    }

    /**
     * Handles successful payment (Paid or Approved).
     */
    private void handleSuccessfulPayment(Invoice invoice, PaymentoWebhookPayload payload) {
        log.info("Completing payment for invoice ID: {}", invoice.getId());

        try {
            // Use existing InvoiceService to complete payment
            // This handles: status update, balance increase, transaction record
            invoiceService.completePayment(invoice.getId());

            log.info("Payment completed for invoice ID: {} - Status: {}",
                    invoice.getId(), payload.getStatusName());
        } catch (Exception e) {
            log.error("Failed to complete payment for invoice {}: {}", invoice.getId(), e.getMessage(), e);
        }
    }

    /**
     * Handles failed payment (Timeout, UserCanceled, Reject).
     */
    private void handleFailedPayment(Invoice invoice, PaymentoWebhookPayload payload) {
        log.info("Payment failed for invoice ID: {} - Status: {}",
                invoice.getId(), payload.getStatusName());

        InvoiceStatus newStatus;
        if (payload.isTimedOut()) {
            newStatus = InvoiceStatus.EXPIRED;
        } else if (payload.isCancelled()) {
            newStatus = InvoiceStatus.CANCELLED;
        } else {
            newStatus = InvoiceStatus.FAILED;
        }

        try {
            invoiceService.updateStatus(invoice.getId(), newStatus);
            log.info("Updated invoice {} status to {}", invoice.getId(), newStatus);
        } catch (Exception e) {
            log.error("Failed to update invoice {} status: {}", invoice.getId(), e.getMessage(), e);
        }
    }

    /**
     * Handles pending payment (Initialize, Pending, PartialPaid, WaitingToConfirm).
     */
    private void handlePendingPayment(Invoice invoice, PaymentoWebhookPayload payload) {
        log.debug("Payment pending for invoice ID: {} - Status: {}",
                invoice.getId(), payload.getStatusName());

        // Update to PROCESSING if still PENDING
        if (invoice.getStatus() == InvoiceStatus.PENDING) {
            try {
                invoiceService.updateStatus(invoice.getId(), InvoiceStatus.PROCESSING);
                log.debug("Updated invoice {} status to PROCESSING", invoice.getId());
            } catch (Exception e) {
                log.error("Failed to update invoice {} status: {}", invoice.getId(), e.getMessage(), e);
            }
        }

        // Log partial payment warning
        if (payload.isPartiallyPaid()) {
            log.warn("Invoice {} received partial payment - manual review may be needed", invoice.getId());
        }
    }

    /**
     * Parses invoice ID from order ID string.
     */
    private Long parseInvoiceId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(orderId.trim());
        } catch (NumberFormatException e) {
            log.error("Failed to parse orderId '{}' as Long", orderId);
            return null;
        }
    }
}
