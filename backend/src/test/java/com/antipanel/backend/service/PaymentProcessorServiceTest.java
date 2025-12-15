package com.antipanel.backend.service;

import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorCreateRequest;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorResponse;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorSummary;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorUpdateRequest;
import com.antipanel.backend.entity.PaymentProcessor;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.PaymentProcessorMapper;
import com.antipanel.backend.repository.PaymentProcessorRepository;
import com.antipanel.backend.service.impl.PaymentProcessorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentProcessorServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class PaymentProcessorServiceTest {

    @Mock
    private PaymentProcessorRepository paymentProcessorRepository;

    @Mock
    private PaymentProcessorMapper paymentProcessorMapper;

    @InjectMocks
    private PaymentProcessorServiceImpl paymentProcessorService;

    private PaymentProcessor testPaymentProcessor;
    private PaymentProcessorResponse testPaymentProcessorResponse;
    private PaymentProcessorCreateRequest createRequest;
    private PaymentProcessorUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testPaymentProcessor = PaymentProcessor.builder()
                .id(1)
                .name("Stripe")
                .code("STRIPE")
                .website("https://stripe.com")
                .apiKey("test-api-key")
                .apiSecret("test-api-secret")
                .configJson("{}")
                .minAmount(new BigDecimal("1.00"))
                .maxAmount(new BigDecimal("10000.00"))
                .feePercentage(new BigDecimal("2.90"))
                .feeFixed(new BigDecimal("0.30"))
                .isActive(true)
                .sortOrder(0)
                .build();

        testPaymentProcessorResponse = PaymentProcessorResponse.builder()
                .id(1)
                .name("Stripe")
                .code("STRIPE")
                .website("https://stripe.com")
                .minAmount(new BigDecimal("1.00"))
                .maxAmount(new BigDecimal("10000.00"))
                .feePercentage(new BigDecimal("2.90"))
                .feeFixed(new BigDecimal("0.30"))
                .isActive(true)
                .sortOrder(0)
                .build();

        createRequest = PaymentProcessorCreateRequest.builder()
                .name("Stripe")
                .code("STRIPE")
                .website("https://stripe.com")
                .apiKey("test-api-key")
                .apiSecret("test-api-secret")
                .configJson("{}")
                .minAmount(new BigDecimal("1.00"))
                .maxAmount(new BigDecimal("10000.00"))
                .feePercentage(new BigDecimal("2.90"))
                .feeFixed(new BigDecimal("0.30"))
                .isActive(true)
                .sortOrder(0)
                .build();

        updateRequest = PaymentProcessorUpdateRequest.builder()
                .name("Updated Stripe")
                .feePercentage(new BigDecimal("2.50"))
                .build();
    }

    // ============ CREATE TESTS ============

    @Test
    void create_Success() {
        // Given
        when(paymentProcessorRepository.existsByCode("STRIPE")).thenReturn(false);
        when(paymentProcessorMapper.toEntity(any(PaymentProcessorCreateRequest.class))).thenReturn(testPaymentProcessor);
        when(paymentProcessorRepository.save(any(PaymentProcessor.class))).thenReturn(testPaymentProcessor);
        when(paymentProcessorMapper.toResponse(any(PaymentProcessor.class))).thenReturn(testPaymentProcessorResponse);

        // When
        PaymentProcessorResponse result = paymentProcessorService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("STRIPE");
        verify(paymentProcessorRepository).existsByCode("STRIPE");
        verify(paymentProcessorRepository).save(any(PaymentProcessor.class));
    }

    @Test
    void create_CodeAlreadyExists_ThrowsConflictException() {
        // Given
        when(paymentProcessorRepository.existsByCode("STRIPE")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> paymentProcessorService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Payment processor code already exists");
    }

    // ============ GET BY ID TESTS ============

    @Test
    void getById_Success() {
        // Given
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testPaymentProcessor));
        when(paymentProcessorMapper.toResponse(testPaymentProcessor)).thenReturn(testPaymentProcessorResponse);

        // When
        PaymentProcessorResponse result = paymentProcessorService.getById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentProcessorService.getById(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("PaymentProcessor");
    }

    // ============ GET BY CODE TESTS ============

    @Test
    void getByCode_Success() {
        // Given
        when(paymentProcessorRepository.findByCode("STRIPE")).thenReturn(Optional.of(testPaymentProcessor));
        when(paymentProcessorMapper.toResponse(testPaymentProcessor)).thenReturn(testPaymentProcessorResponse);

        // When
        PaymentProcessorResponse result = paymentProcessorService.getByCode("STRIPE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("STRIPE");
    }

    @Test
    void getByCode_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(paymentProcessorRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentProcessorService.getByCode("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("PaymentProcessor");
    }

    // ============ UPDATE TESTS ============

    @Test
    void update_Success() {
        // Given
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testPaymentProcessor));
        when(paymentProcessorRepository.save(any(PaymentProcessor.class))).thenReturn(testPaymentProcessor);
        when(paymentProcessorMapper.toResponse(any(PaymentProcessor.class))).thenReturn(testPaymentProcessorResponse);

        // When
        PaymentProcessorResponse result = paymentProcessorService.update(1, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(paymentProcessorMapper).updateEntityFromDto(eq(updateRequest), any(PaymentProcessor.class));
        verify(paymentProcessorRepository).save(any(PaymentProcessor.class));
    }

    @Test
    void update_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentProcessorService.update(1, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ DELETE TESTS ============

    @Test
    void delete_Success() {
        // Given
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testPaymentProcessor));
        doNothing().when(paymentProcessorRepository).delete(testPaymentProcessor);

        // When
        paymentProcessorService.delete(1);

        // Then
        verify(paymentProcessorRepository).delete(testPaymentProcessor);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentProcessorService.delete(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ LISTING TESTS ============

    @Test
    void getAll_Success() {
        // Given
        when(paymentProcessorRepository.findAll()).thenReturn(List.of(testPaymentProcessor));
        when(paymentProcessorMapper.toResponseList(List.of(testPaymentProcessor)))
                .thenReturn(List.of(testPaymentProcessorResponse));

        // When
        List<PaymentProcessorResponse> result = paymentProcessorService.getAll();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getAllActive_Success() {
        // Given
        when(paymentProcessorRepository.findAllActiveProcessors()).thenReturn(List.of(testPaymentProcessor));
        when(paymentProcessorMapper.toResponseList(List.of(testPaymentProcessor)))
                .thenReturn(List.of(testPaymentProcessorResponse));

        // When
        List<PaymentProcessorResponse> result = paymentProcessorService.getAllActive();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getAllSummaries_Success() {
        // Given
        PaymentProcessorSummary summary = PaymentProcessorSummary.builder()
                .id(1)
                .name("Stripe")
                .code("STRIPE")
                .build();
        when(paymentProcessorRepository.findAllActiveProcessors()).thenReturn(List.of(testPaymentProcessor));
        when(paymentProcessorMapper.toSummaryList(List.of(testPaymentProcessor))).thenReturn(List.of(summary));

        // When
        List<PaymentProcessorSummary> result = paymentProcessorService.getAllSummaries();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getProcessorsForAmount_Success() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        when(paymentProcessorRepository.findProcessorsForAmount(amount)).thenReturn(List.of(testPaymentProcessor));
        when(paymentProcessorMapper.toResponseList(List.of(testPaymentProcessor)))
                .thenReturn(List.of(testPaymentProcessorResponse));

        // When
        List<PaymentProcessorResponse> result = paymentProcessorService.getProcessorsForAmount(amount);

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATUS OPERATIONS TESTS ============

    @Test
    void toggleActive_FromActiveToInactive() {
        // Given
        testPaymentProcessor.setIsActive(true);
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testPaymentProcessor));
        when(paymentProcessorRepository.save(any(PaymentProcessor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentProcessorMapper.toResponse(any(PaymentProcessor.class))).thenReturn(
                PaymentProcessorResponse.builder().id(1).isActive(false).build()
        );

        // When
        paymentProcessorService.toggleActive(1);

        // Then
        verify(paymentProcessorRepository).save(argThat(pp -> !pp.getIsActive()));
    }

    @Test
    void toggleActive_FromInactiveToActive() {
        // Given
        testPaymentProcessor.setIsActive(false);
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testPaymentProcessor));
        when(paymentProcessorRepository.save(any(PaymentProcessor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentProcessorMapper.toResponse(any(PaymentProcessor.class))).thenReturn(
                PaymentProcessorResponse.builder().id(1).isActive(true).build()
        );

        // When
        paymentProcessorService.toggleActive(1);

        // Then
        verify(paymentProcessorRepository).save(argThat(pp -> pp.getIsActive()));
    }

    @Test
    void activate_Success() {
        // Given
        testPaymentProcessor.setIsActive(false);
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testPaymentProcessor));
        when(paymentProcessorRepository.save(any(PaymentProcessor.class))).thenReturn(testPaymentProcessor);
        when(paymentProcessorMapper.toResponse(any(PaymentProcessor.class))).thenReturn(testPaymentProcessorResponse);

        // When
        paymentProcessorService.activate(1);

        // Then
        verify(paymentProcessorRepository).save(argThat(pp -> pp.getIsActive()));
    }

    @Test
    void deactivate_Success() {
        // Given
        testPaymentProcessor.setIsActive(true);
        when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testPaymentProcessor));
        when(paymentProcessorRepository.save(any(PaymentProcessor.class))).thenReturn(testPaymentProcessor);
        when(paymentProcessorMapper.toResponse(any(PaymentProcessor.class))).thenReturn(testPaymentProcessorResponse);

        // When
        paymentProcessorService.deactivate(1);

        // Then
        verify(paymentProcessorRepository).save(argThat(pp -> !pp.getIsActive()));
    }

    // ============ STATISTICS TESTS ============

    @Test
    void countActive_Success() {
        // Given
        when(paymentProcessorRepository.countActiveProcessors()).thenReturn(3L);

        // When
        long count = paymentProcessorService.countActive();

        // Then
        assertThat(count).isEqualTo(3L);
    }

    // ============ VALIDATION TESTS ============

    @Test
    void existsByCode_ReturnsTrue() {
        // Given
        when(paymentProcessorRepository.existsByCode("STRIPE")).thenReturn(true);

        // When
        boolean exists = paymentProcessorService.existsByCode("STRIPE");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_ReturnsFalse() {
        // Given
        when(paymentProcessorRepository.existsByCode("NONEXISTENT")).thenReturn(false);

        // When
        boolean exists = paymentProcessorService.existsByCode("NONEXISTENT");

        // Then
        assertThat(exists).isFalse();
    }
}
