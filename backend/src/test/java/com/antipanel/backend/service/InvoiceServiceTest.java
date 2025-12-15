package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.invoice.InvoiceCreateRequest;
import com.antipanel.backend.dto.invoice.InvoiceResponse;
import com.antipanel.backend.dto.invoice.InvoiceSummary;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorSummary;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.PaymentProcessor;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.exception.BadRequestException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.InvoiceMapper;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.repository.InvoiceRepository;
import com.antipanel.backend.repository.PaymentProcessorRepository;
import com.antipanel.backend.repository.TransactionRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceService Tests")
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentProcessorRepository paymentProcessorRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private User testUser;
    private PaymentProcessor testProcessor;
    private Invoice testInvoice;
    private InvoiceResponse testInvoiceResponse;
    private InvoiceCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .balance(new BigDecimal("100.00"))
                .isBanned(false)
                .build();

        testProcessor = PaymentProcessor.builder()
                .id(1)
                .name("Stripe")
                .feePercentage(new BigDecimal("2.90"))
                .feeFixed(new BigDecimal("0.30"))
                .minAmount(new BigDecimal("5.00"))
                .maxAmount(new BigDecimal("1000.00"))
                .isActive(true)
                .build();

        testInvoice = Invoice.builder()
                .id(1L)
                .user(testUser)
                .processor(testProcessor)
                .amount(new BigDecimal("50.00"))
                .fee(new BigDecimal("1.75"))
                .netAmount(new BigDecimal("48.25"))
                .currency("USD")
                .status(InvoiceStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        UserSummary userSummary = UserSummary.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        PaymentProcessorSummary processorSummary = PaymentProcessorSummary.builder()
                .id(1)
                .name("Stripe")
                .build();

        testInvoiceResponse = InvoiceResponse.builder()
                .id(1L)
                .user(userSummary)
                .processor(processorSummary)
                .amount(new BigDecimal("50.00"))
                .fee(new BigDecimal("1.75"))
                .netAmount(new BigDecimal("48.25"))
                .currency("USD")
                .status(InvoiceStatus.PENDING)
                .build();

        createRequest = InvoiceCreateRequest.builder()
                .processorId(1)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .build();
    }

    @Nested
    @DisplayName("Create Operations")
    class CreateOperations {

        @Test
        @DisplayName("Should create invoice successfully")
        void shouldCreateInvoiceSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testProcessor));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
            when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(testInvoiceResponse);

            InvoiceResponse result = invoiceService.create(1L, createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.create(999L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("Should throw exception when user is banned")
        void shouldThrowExceptionWhenUserIsBanned() {
            testUser.setIsBanned(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> invoiceService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("banned");
        }

        @Test
        @DisplayName("Should throw exception when processor not found")
        void shouldThrowExceptionWhenProcessorNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(paymentProcessorRepository.findById(999)).thenReturn(Optional.empty());
            createRequest.setProcessorId(999);

            assertThatThrownBy(() -> invoiceService.create(1L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("PaymentProcessor");
        }

        @Test
        @DisplayName("Should throw exception when processor is not active")
        void shouldThrowExceptionWhenProcessorNotActive() {
            testProcessor.setIsActive(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testProcessor));

            assertThatThrownBy(() -> invoiceService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("Should throw exception when amount is below minimum")
        void shouldThrowExceptionWhenAmountBelowMinimum() {
            createRequest.setAmount(new BigDecimal("1.00")); // Below min of 5.00
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testProcessor));

            assertThatThrownBy(() -> invoiceService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Amount");
        }

        @Test
        @DisplayName("Should throw exception when amount exceeds maximum")
        void shouldThrowExceptionWhenAmountExceedsMaximum() {
            createRequest.setAmount(new BigDecimal("5000.00")); // Above max of 1000.00
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(paymentProcessorRepository.findById(1)).thenReturn(Optional.of(testProcessor));

            assertThatThrownBy(() -> invoiceService.create(1L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Amount");
        }
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadOperations {

        @Test
        @DisplayName("Should get invoice by ID")
        void shouldGetInvoiceById() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(invoiceMapper.toResponse(testInvoice)).thenReturn(testInvoiceResponse);

            InvoiceResponse result = invoiceService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when invoice not found")
        void shouldThrowExceptionWhenInvoiceNotFound() {
            when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.getById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Invoice");
        }

        @Test
        @DisplayName("Should get invoice by processor invoice ID")
        void shouldGetInvoiceByProcessorInvoiceId() {
            testInvoice.setProcessorInvoiceId("PROC-123");
            when(invoiceRepository.findByProcessorInvoiceId("PROC-123")).thenReturn(Optional.of(testInvoice));
            when(invoiceMapper.toResponse(testInvoice)).thenReturn(testInvoiceResponse);

            InvoiceResponse result = invoiceService.getByProcessorInvoiceId("PROC-123");

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("User Queries")
    class UserQueries {

        @Test
        @DisplayName("Should get invoices by user")
        void shouldGetInvoicesByUser() {
            when(invoiceRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(testInvoice));
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));

            List<InvoiceResponse> result = invoiceService.getByUser(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get invoices by user paginated")
        void shouldGetInvoicesByUserPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Invoice> page = new PageImpl<>(List.of(testInvoice), pageable, 1);
            PageResponse<InvoiceResponse> expectedPageResponse = PageResponse.<InvoiceResponse>builder()
                    .content(List.of(testInvoiceResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(invoiceRepository.findByUserId(1L, pageable)).thenReturn(page);
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));
            doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

            PageResponse<InvoiceResponse> result = invoiceService.getByUserPaginated(1L, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get invoices by user and status")
        void shouldGetInvoicesByUserAndStatus() {
            when(invoiceRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, InvoiceStatus.PENDING))
                    .thenReturn(List.of(testInvoice));
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));

            List<InvoiceResponse> result = invoiceService.getByUserAndStatus(1L, InvoiceStatus.PENDING);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get pending invoices by user")
        void shouldGetPendingInvoicesByUser() {
            when(invoiceRepository.findPendingInvoicesByUser(1L)).thenReturn(List.of(testInvoice));
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));

            List<InvoiceResponse> result = invoiceService.getPendingByUser(1L);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Admin Queries")
    class AdminQueries {

        @Test
        @DisplayName("Should get invoices by status")
        void shouldGetInvoicesByStatus() {
            when(invoiceRepository.findByStatusOrderByCreatedAtDesc(InvoiceStatus.PENDING))
                    .thenReturn(List.of(testInvoice));
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));

            List<InvoiceResponse> result = invoiceService.getByStatus(InvoiceStatus.PENDING);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get invoices by status paginated")
        void shouldGetInvoicesByStatusPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Invoice> page = new PageImpl<>(List.of(testInvoice), pageable, 1);
            PageResponse<InvoiceResponse> expectedPageResponse = PageResponse.<InvoiceResponse>builder()
                    .content(List.of(testInvoiceResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(invoiceRepository.findByStatus(InvoiceStatus.PENDING, pageable)).thenReturn(page);
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));
            doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

            PageResponse<InvoiceResponse> result = invoiceService.getByStatusPaginated(InvoiceStatus.PENDING, pageable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should get invoices by processor")
        void shouldGetInvoicesByProcessor() {
            when(invoiceRepository.findByProcessorIdOrderByCreatedAtDesc(1)).thenReturn(List.of(testInvoice));
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));

            List<InvoiceResponse> result = invoiceService.getByProcessor(1);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Time-Based Queries")
    class TimeBasedQueries {

        @Test
        @DisplayName("Should get invoices between dates")
        void shouldGetInvoicesBetweenDates() {
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();
            when(invoiceRepository.findInvoicesBetweenDates(start, end)).thenReturn(List.of(testInvoice));
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));

            List<InvoiceResponse> result = invoiceService.getInvoicesBetweenDates(start, end);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get paid invoices between dates")
        void shouldGetPaidInvoicesBetweenDates() {
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();
            when(invoiceRepository.findPaidInvoicesBetweenDates(start, end)).thenReturn(List.of(testInvoice));
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));

            List<InvoiceResponse> result = invoiceService.getPaidInvoicesBetweenDates(start, end);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get expired pending invoices")
        void shouldGetExpiredPendingInvoices() {
            LocalDateTime expiryTime = LocalDateTime.now().minusHours(24);
            when(invoiceRepository.findExpiredPendingInvoices(expiryTime)).thenReturn(List.of(testInvoice));
            when(invoiceMapper.toResponseList(anyList())).thenReturn(List.of(testInvoiceResponse));

            List<InvoiceResponse> result = invoiceService.getExpiredPendingInvoices(expiryTime);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Status Operations")
    class StatusOperations {

        @Test
        @DisplayName("Should update invoice status")
        void shouldUpdateInvoiceStatus() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
            when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(testInvoiceResponse);

            InvoiceResponse result = invoiceService.updateStatus(1L, InvoiceStatus.PROCESSING);

            assertThat(result).isNotNull();
            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception when updating status of final invoice")
        void shouldThrowExceptionWhenUpdatingFinalInvoiceStatus() {
            testInvoice.setStatus(InvoiceStatus.COMPLETED);
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

            assertThatThrownBy(() -> invoiceService.updateStatus(1L, InvoiceStatus.PROCESSING))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("final state");
        }

        @Test
        @DisplayName("Should mark invoice as processing")
        void shouldMarkInvoiceAsProcessing() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
            when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(testInvoiceResponse);

            InvoiceResponse result = invoiceService.markAsProcessing(1L, "PROC-123", "https://payment.url");

            assertThat(result).isNotNull();
            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception when marking non-pending invoice as processing")
        void shouldThrowExceptionWhenMarkingNonPendingAsProcessing() {
            testInvoice.setStatus(InvoiceStatus.PROCESSING);
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

            assertThatThrownBy(() -> invoiceService.markAsProcessing(1L, "PROC-123", "https://payment.url"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("Should complete payment and add balance")
        void shouldCompletePaymentAndAddBalance() {
            BigDecimal initialBalance = testUser.getBalance();
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(Transaction.builder().build());
            when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(testInvoiceResponse);

            InvoiceResponse result = invoiceService.completePayment(1L);

            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw exception when completing final invoice payment")
        void shouldThrowExceptionWhenCompletingFinalInvoicePayment() {
            testInvoice.setStatus(InvoiceStatus.COMPLETED);
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

            assertThatThrownBy(() -> invoiceService.completePayment(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("final state");
        }

        @Test
        @DisplayName("Should cancel invoice")
        void shouldCancelInvoice() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
            when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(testInvoiceResponse);

            InvoiceResponse result = invoiceService.cancelInvoice(1L);

            assertThat(result).isNotNull();
            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception when cancelling final invoice")
        void shouldThrowExceptionWhenCancellingFinalInvoice() {
            testInvoice.setStatus(InvoiceStatus.COMPLETED);
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

            assertThatThrownBy(() -> invoiceService.cancelInvoice(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("final state");
        }

        @Test
        @DisplayName("Should expire invoice")
        void shouldExpireInvoice() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
            when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(testInvoiceResponse);

            InvoiceResponse result = invoiceService.expireInvoice(1L);

            assertThat(result).isNotNull();
            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception when expiring non-pending invoice")
        void shouldThrowExceptionWhenExpiringNonPendingInvoice() {
            testInvoice.setStatus(InvoiceStatus.PROCESSING);
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

            assertThatThrownBy(() -> invoiceService.expireInvoice(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("pending");
        }
    }

    @Nested
    @DisplayName("Statistics")
    class Statistics {

        @Test
        @DisplayName("Should count invoices by status")
        void shouldCountInvoicesByStatus() {
            when(invoiceRepository.countByStatus(InvoiceStatus.COMPLETED)).thenReturn(10L);

            long result = invoiceService.countByStatus(InvoiceStatus.COMPLETED);

            assertThat(result).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should get total revenue")
        void shouldGetTotalRevenue() {
            when(invoiceRepository.getTotalRevenue()).thenReturn(new BigDecimal("5000.00"));

            BigDecimal result = invoiceService.getTotalRevenue();

            assertThat(result).isEqualByComparingTo(new BigDecimal("5000.00"));
        }

        @Test
        @DisplayName("Should return zero when total revenue is null")
        void shouldReturnZeroWhenTotalRevenueNull() {
            when(invoiceRepository.getTotalRevenue()).thenReturn(null);

            BigDecimal result = invoiceService.getTotalRevenue();

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should get revenue between dates")
        void shouldGetRevenueBetweenDates() {
            LocalDateTime start = LocalDateTime.now().minusDays(30);
            LocalDateTime end = LocalDateTime.now();
            when(invoiceRepository.getRevenueBetweenDates(start, end)).thenReturn(new BigDecimal("1000.00"));

            BigDecimal result = invoiceService.getRevenueBetweenDates(start, end);

            assertThat(result).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should get total revenue by user")
        void shouldGetTotalRevenueByUser() {
            when(invoiceRepository.getTotalRevenueByUser(1L)).thenReturn(new BigDecimal("500.00"));

            BigDecimal result = invoiceService.getTotalRevenueByUser(1L);

            assertThat(result).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should get average invoice amount")
        void shouldGetAverageInvoiceAmount() {
            when(invoiceRepository.getAverageInvoiceAmount()).thenReturn(new BigDecimal("75.00"));

            BigDecimal result = invoiceService.getAverageInvoiceAmount();

            assertThat(result).isEqualByComparingTo(new BigDecimal("75.00"));
        }
    }

    @Nested
    @DisplayName("Summaries")
    class Summaries {

        @Test
        @DisplayName("Should get all invoice summaries")
        void shouldGetAllInvoiceSummaries() {
            InvoiceSummary summary = InvoiceSummary.builder()
                    .id(1L)
                    .amount(new BigDecimal("50.00"))
                    .status(InvoiceStatus.PENDING)
                    .build();

            when(invoiceRepository.findAll()).thenReturn(List.of(testInvoice));
            when(invoiceMapper.toSummaryList(anyList())).thenReturn(List.of(summary));

            List<InvoiceSummary> result = invoiceService.getAllSummaries();

            assertThat(result).hasSize(1);
        }
    }
}
