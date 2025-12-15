package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.invoice.InvoiceCreateRequest;
import com.antipanel.backend.dto.invoice.InvoiceResponse;
import com.antipanel.backend.dto.invoice.InvoiceSummary;
import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.PaymentProcessor;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InvoiceMapper.
 */
@SpringBootTest(classes = {
        InvoiceMapperImpl.class,
        UserMapperImpl.class,
        PaymentProcessorMapperImpl.class
})
class InvoiceMapperTest {

    @Autowired
    private InvoiceMapper mapper;

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        Invoice invoice = createTestInvoice();

        // When
        InvoiceResponse response = mapper.toResponse(invoice);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(invoice.getId());
        assertThat(response.getProcessorInvoiceId()).isEqualTo(invoice.getProcessorInvoiceId());
        assertThat(response.getAmount()).isEqualByComparingTo(invoice.getAmount());
        assertThat(response.getFee()).isEqualByComparingTo(invoice.getFee());
        assertThat(response.getNetAmount()).isEqualByComparingTo(invoice.getNetAmount());
        assertThat(response.getCurrency()).isEqualTo(invoice.getCurrency());
        assertThat(response.getStatus()).isEqualTo(invoice.getStatus());
        assertThat(response.getPaymentUrl()).isEqualTo(invoice.getPaymentUrl());
        assertThat(response.getCreatedAt()).isEqualTo(invoice.getCreatedAt());
        // User nested
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(invoice.getUser().getId());
        // Processor nested
        assertThat(response.getProcessor()).isNotNull();
        assertThat(response.getProcessor().getId()).isEqualTo(invoice.getProcessor().getId());
    }

    @Test
    void toEntity_ShouldMapBasicFields() {
        // Given
        InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                .processorId(1)
                .amount(BigDecimal.valueOf(50.00))
                .currency("USD")
                .build();

        // When
        Invoice invoice = mapper.toEntity(request);

        // Then
        assertThat(invoice).isNotNull();
        assertThat(invoice.getId()).isNull();
        assertThat(invoice.getAmount()).isEqualByComparingTo(request.getAmount());
        assertThat(invoice.getCurrency()).isEqualTo(request.getCurrency());
        // All other fields should be null/set by service
        assertThat(invoice.getUser()).isNull();
        assertThat(invoice.getProcessor()).isNull();
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        Invoice invoice = createTestInvoice();

        // When
        InvoiceSummary summary = mapper.toSummary(invoice);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(invoice.getId());
        assertThat(summary.getAmount()).isEqualByComparingTo(invoice.getAmount());
        assertThat(summary.getNetAmount()).isEqualByComparingTo(invoice.getNetAmount());
        assertThat(summary.getCurrency()).isEqualTo(invoice.getCurrency());
        assertThat(summary.getStatus()).isEqualTo(invoice.getStatus());
        assertThat(summary.getCreatedAt()).isEqualTo(invoice.getCreatedAt());
    }

    @Test
    void toResponseList_ShouldMapAllInvoices() {
        // Given
        List<Invoice> invoices = List.of(
                createTestInvoice(),
                createTestInvoice()
        );
        invoices.get(1).setId(2L);
        invoices.get(1).setAmount(BigDecimal.valueOf(100.00));

        // When
        List<InvoiceResponse> responses = mapper.toResponseList(invoices);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
    }

    private Invoice createTestInvoice() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(UserRole.USER);
        user.setBalance(BigDecimal.valueOf(100));

        PaymentProcessor processor = new PaymentProcessor();
        processor.setId(1);
        processor.setName("PayPal");
        processor.setCode("PAYPAL");
        processor.setMinAmount(BigDecimal.valueOf(5.00));
        processor.setMaxAmount(BigDecimal.valueOf(1000.00));
        processor.setFeePercentage(BigDecimal.valueOf(2.9));
        processor.setFeeFixed(BigDecimal.valueOf(0.30));
        processor.setIsActive(true);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setUser(user);
        invoice.setProcessor(processor);
        invoice.setProcessorInvoiceId("PAY-123456");
        invoice.setAmount(BigDecimal.valueOf(50.00));
        invoice.setFee(BigDecimal.valueOf(1.75));
        invoice.setNetAmount(BigDecimal.valueOf(48.25));
        invoice.setCurrency("USD");
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setPaymentUrl("https://paypal.com/pay/PAY-123456");
        invoice.setCreatedAt(LocalDateTime.now().minusHours(1));
        invoice.setUpdatedAt(LocalDateTime.now());
        return invoice;
    }
}
