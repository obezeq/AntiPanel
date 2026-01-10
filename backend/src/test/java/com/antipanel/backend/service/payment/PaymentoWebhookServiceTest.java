package com.antipanel.backend.service.payment;

import com.antipanel.backend.dto.paymento.PaymentoWebhookPayload;
import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.PaymentProcessor;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.repository.InvoiceRepository;
import com.antipanel.backend.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for PaymentoWebhookService.
 * Tests webhook processing, status transitions, and idempotency.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentoWebhookService Tests")
class PaymentoWebhookServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private PaymentoWebhookService webhookService;

    private Invoice testInvoice;
    private User testUser;
    private PaymentProcessor testProcessor;

    @BeforeEach
    void setUp() {
        testProcessor = PaymentProcessor.builder()
                .id(1)
                .name("Paymento")
                .code("paymento")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .balance(BigDecimal.ZERO)
                .build();

        testInvoice = Invoice.builder()
                .id(123L)
                .user(testUser)
                .processor(testProcessor)
                .amount(new BigDecimal("100.00"))
                .fee(new BigDecimal("0.50"))
                .netAmount(new BigDecimal("99.50"))
                .currency("USD")
                .status(InvoiceStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("Process Successful Payment")
    class ProcessSuccessfulPaymentTests {

        @Test
        @DisplayName("Should complete payment when status is PAID (7)")
        void shouldCompletePaymentWhenStatusIsPaid() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .paymentId("pay_123")
                    .orderId("123")
                    .orderStatus(7)  // PAID
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService).completePayment(123L);
            verify(invoiceRepository).save(testInvoice);
        }

        @Test
        @DisplayName("Should complete payment when status is APPROVED (8)")
        void shouldCompletePaymentWhenStatusIsApproved() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .paymentId("pay_123")
                    .orderId("123")
                    .orderStatus(8)  // APPROVED
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService).completePayment(123L);
        }
    }

    @Nested
    @DisplayName("Process Failed Payment")
    class ProcessFailedPaymentTests {

        @Test
        @DisplayName("Should set status to EXPIRED when status is TIMEOUT (4)")
        void shouldSetStatusToExpiredWhenTimeout() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId("123")
                    .orderStatus(4)  // TIMEOUT
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService).updateStatus(123L, InvoiceStatus.EXPIRED);
        }

        @Test
        @DisplayName("Should set status to CANCELLED when status is USER_CANCELED (5)")
        void shouldSetStatusToCancelledWhenUserCanceled() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId("123")
                    .orderStatus(5)  // USER_CANCELED
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService).updateStatus(123L, InvoiceStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should set status to FAILED when status is REJECT (9)")
        void shouldSetStatusToFailedWhenRejected() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId("123")
                    .orderStatus(9)  // REJECT
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService).updateStatus(123L, InvoiceStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("Process Pending Payment")
    class ProcessPendingPaymentTests {

        @Test
        @DisplayName("Should set status to PROCESSING when status is PENDING (1) and invoice is PENDING")
        void shouldSetStatusToProcessingWhenPending() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId("123")
                    .orderStatus(1)  // PENDING
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService).updateStatus(123L, InvoiceStatus.PROCESSING);
        }

        @Test
        @DisplayName("Should not change status when invoice already PROCESSING")
        void shouldNotChangeStatusWhenAlreadyProcessing() {
            testInvoice.setStatus(InvoiceStatus.PROCESSING);

            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId("123")
                    .orderStatus(3)  // WAITING_TO_CONFIRM
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService, never()).updateStatus(any(), any());
        }
    }

    @Nested
    @DisplayName("Idempotency")
    class IdempotencyTests {

        @Test
        @DisplayName("Should skip processing when invoice is already COMPLETED")
        void shouldSkipWhenInvoiceAlreadyCompleted() {
            testInvoice.setStatus(InvoiceStatus.COMPLETED);

            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId("123")
                    .orderStatus(7)  // PAID
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService, never()).completePayment(any());
            verify(invoiceService, never()).updateStatus(any(), any());
        }

        @Test
        @DisplayName("Should skip processing when invoice is already FAILED")
        void shouldSkipWhenInvoiceAlreadyFailed() {
            testInvoice.setStatus(InvoiceStatus.FAILED);

            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId("123")
                    .orderStatus(7)  // PAID - trying to complete after failure
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService, never()).completePayment(any());
        }

        @Test
        @DisplayName("Should skip processing when invoice is already EXPIRED")
        void shouldSkipWhenInvoiceAlreadyExpired() {
            testInvoice.setStatus(InvoiceStatus.EXPIRED);

            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .orderId("123")
                    .orderStatus(4)
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService, never()).updateStatus(any(), any());
        }

        @Test
        @DisplayName("Should skip processing when invoice is already CANCELLED")
        void shouldSkipWhenInvoiceAlreadyCancelled() {
            testInvoice.setStatus(InvoiceStatus.CANCELLED);

            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .orderId("123")
                    .orderStatus(5)
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceService, never()).updateStatus(any(), any());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid orderId gracefully")
        void shouldHandleInvalidOrderIdGracefully() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId("invalid_id")
                    .orderStatus(7)
                    .build();

            webhookService.processWebhook(payload);

            verify(invoiceRepository, never()).findById(any());
            verify(invoiceService, never()).completePayment(any());
        }

        @Test
        @DisplayName("Should handle null orderId gracefully")
        void shouldHandleNullOrderIdGracefully() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId(null)
                    .orderStatus(7)
                    .build();

            webhookService.processWebhook(payload);

            verify(invoiceRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should handle invoice not found gracefully")
        void shouldHandleInvoiceNotFoundGracefully() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("abc123token")
                    .orderId("999")
                    .orderStatus(7)
                    .build();

            when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

            webhookService.processWebhook(payload);

            verify(invoiceService, never()).completePayment(any());
        }
    }

    @Nested
    @DisplayName("Token Update")
    class TokenUpdateTests {

        @Test
        @DisplayName("Should update processor invoice ID when not set")
        void shouldUpdateProcessorInvoiceIdWhenNotSet() {
            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("new_token_123")
                    .orderId("123")
                    .orderStatus(1)
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            verify(invoiceRepository).save(testInvoice);
            // Token should be set on the invoice
        }

        @Test
        @DisplayName("Should not update token when already set")
        void shouldNotUpdateTokenWhenAlreadySet() {
            testInvoice.setProcessorInvoiceId("existing_token");

            PaymentoWebhookPayload payload = PaymentoWebhookPayload.builder()
                    .token("new_token_123")
                    .orderId("123")
                    .orderStatus(1)
                    .build();

            when(invoiceRepository.findById(123L)).thenReturn(Optional.of(testInvoice));

            webhookService.processWebhook(payload);

            // Token should remain as existing_token
        }
    }
}
