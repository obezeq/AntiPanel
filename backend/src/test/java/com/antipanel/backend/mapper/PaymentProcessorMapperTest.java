package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorCreateRequest;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorResponse;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorSummary;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorUpdateRequest;
import com.antipanel.backend.entity.PaymentProcessor;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaymentProcessorMapper.
 */
class PaymentProcessorMapperTest {

    private final PaymentProcessorMapper mapper = Mappers.getMapper(PaymentProcessorMapper.class);

    @Test
    void toResponse_ShouldMapAllFieldsExceptCredentials() {
        // Given
        PaymentProcessor processor = createTestProcessor();

        // When
        PaymentProcessorResponse response = mapper.toResponse(processor);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(processor.getId());
        assertThat(response.getName()).isEqualTo(processor.getName());
        assertThat(response.getCode()).isEqualTo(processor.getCode());
        assertThat(response.getWebsite()).isEqualTo(processor.getWebsite());
        assertThat(response.getMinAmount()).isEqualByComparingTo(processor.getMinAmount());
        assertThat(response.getMaxAmount()).isEqualByComparingTo(processor.getMaxAmount());
        assertThat(response.getFeePercentage()).isEqualByComparingTo(processor.getFeePercentage());
        assertThat(response.getFeeFixed()).isEqualByComparingTo(processor.getFeeFixed());
        assertThat(response.getIsActive()).isEqualTo(processor.getIsActive());
        assertThat(response.getSortOrder()).isEqualTo(processor.getSortOrder());
        // Note: API credentials should NOT be in response DTO
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        // Given
        PaymentProcessorCreateRequest request = PaymentProcessorCreateRequest.builder()
                .name("Stripe")
                .code("STRIPE")
                .website("https://stripe.com")
                .apiKey("sk_test_123")
                .apiSecret("secret_123")
                .minAmount(BigDecimal.valueOf(5.00))
                .maxAmount(BigDecimal.valueOf(5000.00))
                .feePercentage(BigDecimal.valueOf(2.9))
                .feeFixed(BigDecimal.valueOf(0.30))
                .isActive(true)
                .sortOrder(1)
                .build();

        // When
        PaymentProcessor processor = mapper.toEntity(request);

        // Then
        assertThat(processor).isNotNull();
        assertThat(processor.getId()).isNull();
        assertThat(processor.getName()).isEqualTo(request.getName());
        assertThat(processor.getCode()).isEqualTo(request.getCode());
        assertThat(processor.getWebsite()).isEqualTo(request.getWebsite());
        assertThat(processor.getApiKey()).isEqualTo(request.getApiKey());
        assertThat(processor.getApiSecret()).isEqualTo(request.getApiSecret());
        assertThat(processor.getMinAmount()).isEqualByComparingTo(request.getMinAmount());
        assertThat(processor.getMaxAmount()).isEqualByComparingTo(request.getMaxAmount());
        assertThat(processor.getFeePercentage()).isEqualByComparingTo(request.getFeePercentage());
        assertThat(processor.getFeeFixed()).isEqualByComparingTo(request.getFeeFixed());
        assertThat(processor.getIsActive()).isEqualTo(request.getIsActive());
        assertThat(processor.getSortOrder()).isEqualTo(request.getSortOrder());
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {
        // Given
        PaymentProcessor processor = createTestProcessor();
        String originalName = processor.getName();
        String originalCode = processor.getCode();

        PaymentProcessorUpdateRequest request = PaymentProcessorUpdateRequest.builder()
                .feePercentage(BigDecimal.valueOf(3.5))
                .isActive(false)
                .build();

        // When
        mapper.updateEntityFromDto(request, processor);

        // Then
        assertThat(processor.getName()).isEqualTo(originalName);  // Unchanged
        assertThat(processor.getCode()).isEqualTo(originalCode);  // Unchanged (ignored)
        assertThat(processor.getFeePercentage()).isEqualByComparingTo(BigDecimal.valueOf(3.5));
        assertThat(processor.getIsActive()).isFalse();
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        PaymentProcessor processor = createTestProcessor();

        // When
        PaymentProcessorSummary summary = mapper.toSummary(processor);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(processor.getId());
        assertThat(summary.getName()).isEqualTo(processor.getName());
        assertThat(summary.getCode()).isEqualTo(processor.getCode());
        assertThat(summary.getMinAmount()).isEqualByComparingTo(processor.getMinAmount());
        assertThat(summary.getMaxAmount()).isEqualByComparingTo(processor.getMaxAmount());
        assertThat(summary.getFeePercentage()).isEqualByComparingTo(processor.getFeePercentage());
        assertThat(summary.getFeeFixed()).isEqualByComparingTo(processor.getFeeFixed());
    }

    @Test
    void toResponseList_ShouldMapAllProcessors() {
        // Given
        List<PaymentProcessor> processors = List.of(
                createTestProcessor(),
                createTestProcessor()
        );
        processors.get(1).setId(2);
        processors.get(1).setName("Stripe");
        processors.get(1).setCode("STRIPE");

        // When
        List<PaymentProcessorResponse> responses = mapper.toResponseList(processors);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("PayPal");
        assertThat(responses.get(1).getName()).isEqualTo("Stripe");
    }

    private PaymentProcessor createTestProcessor() {
        PaymentProcessor processor = new PaymentProcessor();
        processor.setId(1);
        processor.setName("PayPal");
        processor.setCode("PAYPAL");
        processor.setWebsite("https://paypal.com");
        processor.setApiKey("api_key_123");
        processor.setApiSecret("api_secret_123");
        processor.setMinAmount(BigDecimal.valueOf(5.00));
        processor.setMaxAmount(BigDecimal.valueOf(1000.00));
        processor.setFeePercentage(BigDecimal.valueOf(2.9));
        processor.setFeeFixed(BigDecimal.valueOf(0.30));
        processor.setIsActive(true);
        processor.setSortOrder(0);
        return processor;
    }
}
