package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.transaction.TransactionResponse;
import com.antipanel.backend.dto.transaction.TransactionSummary;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.mapper.TransactionMapper;
import com.antipanel.backend.repository.TransactionRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.impl.TransactionServiceImpl;
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
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Transaction testTransaction;
    private TransactionResponse testTransactionResponse;
    private UserSummary testUserSummary;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .balance(new BigDecimal("100.00"))
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .user(testUser)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("50.00"))
                .balanceBefore(new BigDecimal("50.00"))
                .balanceAfter(new BigDecimal("100.00"))
                .referenceType("INVOICE")
                .referenceId(1L)
                .description("Deposit via Stripe")
                .createdAt(LocalDateTime.now())
                .build();

        testUserSummary = UserSummary.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testTransactionResponse = TransactionResponse.builder()
                .id(1L)
                .user(testUserSummary)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("50.00"))
                .balanceBefore(new BigDecimal("50.00"))
                .balanceAfter(new BigDecimal("100.00"))
                .referenceType("INVOICE")
                .referenceId(1L)
                .description("Deposit via Stripe")
                .build();
    }

    @Nested
    @DisplayName("Create Operations")
    class CreateOperations {

        @Test
        @DisplayName("Should create transaction successfully")
        void shouldCreateTransactionSuccessfully() {
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

            Transaction result = transactionService.createTransaction(
                    testUser,
                    TransactionType.DEPOSIT,
                    new BigDecimal("50.00"),
                    "INVOICE",
                    1L,
                    "Deposit via Stripe"
            );

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should calculate balance after correctly")
        void shouldCalculateBalanceAfterCorrectly() {
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            Transaction result = transactionService.createTransaction(
                    testUser,
                    TransactionType.DEPOSIT,
                    new BigDecimal("50.00"),
                    "INVOICE",
                    1L,
                    "Deposit via Stripe"
            );

            assertThat(result.getBalanceBefore()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(result.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("150.00"));
        }
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadOperations {

        @Test
        @DisplayName("Should get transaction by ID")
        void shouldGetTransactionById() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
            when(transactionMapper.toResponse(testTransaction)).thenReturn(testTransactionResponse);

            TransactionResponse result = transactionService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when transaction not found")
        void shouldThrowExceptionWhenTransactionNotFound() {
            when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.getById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transaction");
        }

        @Test
        @DisplayName("Should get latest transaction by user")
        void shouldGetLatestTransactionByUser() {
            when(transactionRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                    .thenReturn(Optional.of(testTransaction));
            when(transactionMapper.toResponse(testTransaction)).thenReturn(testTransactionResponse);

            TransactionResponse result = transactionService.getLatestByUser(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should return null when no transactions for user")
        void shouldReturnNullWhenNoTransactionsForUser() {
            when(transactionRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                    .thenReturn(Optional.empty());

            TransactionResponse result = transactionService.getLatestByUser(1L);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("User Queries")
    class UserQueries {

        @Test
        @DisplayName("Should get transactions by user")
        void shouldGetTransactionsByUser() {
            when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(testTransaction));
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(testTransactionResponse));

            List<TransactionResponse> result = transactionService.getByUser(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get transactions by user paginated")
        void shouldGetTransactionsByUserPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> page = new PageImpl<>(List.of(testTransaction), pageable, 1);
            PageResponse<TransactionResponse> expectedPageResponse = PageResponse.<TransactionResponse>builder()
                    .content(List.of(testTransactionResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(transactionRepository.findByUserId(1L, pageable)).thenReturn(page);
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(testTransactionResponse));
            doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

            PageResponse<TransactionResponse> result = transactionService.getByUserPaginated(1L, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get transactions by user and type")
        void shouldGetTransactionsByUserAndType() {
            when(transactionRepository.findByUserIdAndTypeOrderByCreatedAtDesc(1L, TransactionType.DEPOSIT))
                    .thenReturn(List.of(testTransaction));
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(testTransactionResponse));

            List<TransactionResponse> result = transactionService.getByUserAndType(1L, TransactionType.DEPOSIT);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get user transactions between dates")
        void shouldGetUserTransactionsBetweenDates() {
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();
            when(transactionRepository.findUserTransactionsBetweenDates(1L, start, end))
                    .thenReturn(List.of(testTransaction));
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(testTransactionResponse));

            List<TransactionResponse> result = transactionService.getUserTransactionsBetweenDates(1L, start, end);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Admin Queries")
    class AdminQueries {

        @Test
        @DisplayName("Should get transactions by type")
        void shouldGetTransactionsByType() {
            when(transactionRepository.findByTypeOrderByCreatedAtDesc(TransactionType.DEPOSIT))
                    .thenReturn(List.of(testTransaction));
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(testTransactionResponse));

            List<TransactionResponse> result = transactionService.getByType(TransactionType.DEPOSIT);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get transactions by type paginated")
        void shouldGetTransactionsByTypePaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> page = new PageImpl<>(List.of(testTransaction), pageable, 1);
            PageResponse<TransactionResponse> expectedPageResponse = PageResponse.<TransactionResponse>builder()
                    .content(List.of(testTransactionResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(transactionRepository.findByType(TransactionType.DEPOSIT, pageable)).thenReturn(page);
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(testTransactionResponse));
            doReturn(expectedPageResponse).when(pageMapper).toPageResponse(any(Page.class), anyList());

            PageResponse<TransactionResponse> result = transactionService.getByTypePaginated(TransactionType.DEPOSIT, pageable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should get transactions by reference")
        void shouldGetTransactionsByReference() {
            when(transactionRepository.findByReference("INVOICE", 1L))
                    .thenReturn(List.of(testTransaction));
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(testTransactionResponse));

            List<TransactionResponse> result = transactionService.getByReference("INVOICE", 1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get transactions between dates")
        void shouldGetTransactionsBetweenDates() {
            LocalDateTime start = LocalDateTime.now().minusDays(30);
            LocalDateTime end = LocalDateTime.now();
            when(transactionRepository.findTransactionsBetweenDates(start, end))
                    .thenReturn(List.of(testTransaction));
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(testTransactionResponse));

            List<TransactionResponse> result = transactionService.getTransactionsBetweenDates(start, end);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Audit & Validation")
    class AuditAndValidation {

        @Test
        @DisplayName("Should find inconsistent transactions")
        void shouldFindInconsistentTransactions() {
            when(transactionRepository.findInconsistentTransactions())
                    .thenReturn(List.of(testTransaction));
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(testTransactionResponse));

            List<TransactionResponse> result = transactionService.findInconsistentTransactions();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no inconsistent transactions")
        void shouldReturnEmptyListWhenNoInconsistentTransactions() {
            when(transactionRepository.findInconsistentTransactions())
                    .thenReturn(List.of());
            when(transactionMapper.toResponseList(anyList())).thenReturn(List.of());

            List<TransactionResponse> result = transactionService.findInconsistentTransactions();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should validate user balance when consistent")
        void shouldValidateUserBalanceWhenConsistent() {
            testUser.setBalance(new BigDecimal("100.00"));
            testTransaction.setBalanceAfter(new BigDecimal("100.00"));

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(transactionRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                    .thenReturn(Optional.of(testTransaction));

            boolean result = transactionService.validateUserBalance(1L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when balance is inconsistent")
        void shouldReturnFalseWhenBalanceInconsistent() {
            testUser.setBalance(new BigDecimal("100.00"));
            testTransaction.setBalanceAfter(new BigDecimal("150.00")); // Mismatch

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(transactionRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                    .thenReturn(Optional.of(testTransaction));

            boolean result = transactionService.validateUserBalance(1L);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when user has no transactions")
        void shouldReturnTrueWhenUserHasNoTransactions() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(transactionRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                    .thenReturn(Optional.empty());

            boolean result = transactionService.validateUserBalance(1L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when validating balance for non-existent user")
        void shouldThrowExceptionWhenValidatingBalanceForNonExistentUser() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.validateUserBalance(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    @Nested
    @DisplayName("Statistics")
    class Statistics {

        @Test
        @DisplayName("Should count transactions by type")
        void shouldCountTransactionsByType() {
            when(transactionRepository.countByType(TransactionType.DEPOSIT)).thenReturn(25L);

            long result = transactionService.countByType(TransactionType.DEPOSIT);

            assertThat(result).isEqualTo(25L);
        }

        @Test
        @DisplayName("Should get total deposits")
        void shouldGetTotalDeposits() {
            when(transactionRepository.getTotalDeposits()).thenReturn(new BigDecimal("10000.00"));

            BigDecimal result = transactionService.getTotalDeposits();

            assertThat(result).isEqualByComparingTo(new BigDecimal("10000.00"));
        }

        @Test
        @DisplayName("Should return zero when total deposits is null")
        void shouldReturnZeroWhenTotalDepositsNull() {
            when(transactionRepository.getTotalDeposits()).thenReturn(null);

            BigDecimal result = transactionService.getTotalDeposits();

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should get total order transactions")
        void shouldGetTotalOrderTransactions() {
            when(transactionRepository.getTotalOrderTransactions()).thenReturn(new BigDecimal("5000.00"));

            BigDecimal result = transactionService.getTotalOrderTransactions();

            assertThat(result).isEqualByComparingTo(new BigDecimal("5000.00"));
        }

        @Test
        @DisplayName("Should get deposits between dates")
        void shouldGetDepositsBetweenDates() {
            LocalDateTime start = LocalDateTime.now().minusDays(30);
            LocalDateTime end = LocalDateTime.now();
            when(transactionRepository.getDepositsBetweenDates(start, end))
                    .thenReturn(new BigDecimal("2000.00"));

            BigDecimal result = transactionService.getDepositsBetweenDates(start, end);

            assertThat(result).isEqualByComparingTo(new BigDecimal("2000.00"));
        }

        @Test
        @DisplayName("Should return zero when deposits between dates is null")
        void shouldReturnZeroWhenDepositsBetweenDatesNull() {
            LocalDateTime start = LocalDateTime.now().minusDays(30);
            LocalDateTime end = LocalDateTime.now();
            when(transactionRepository.getDepositsBetweenDates(start, end)).thenReturn(null);

            BigDecimal result = transactionService.getDepositsBetweenDates(start, end);

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Summaries")
    class Summaries {

        @Test
        @DisplayName("Should get all transaction summaries")
        void shouldGetAllTransactionSummaries() {
            TransactionSummary summary = TransactionSummary.builder()
                    .id(1L)
                    .type(TransactionType.DEPOSIT)
                    .amount(new BigDecimal("50.00"))
                    .build();

            when(transactionRepository.findAll()).thenReturn(List.of(testTransaction));
            when(transactionMapper.toSummaryList(anyList())).thenReturn(List.of(summary));

            List<TransactionSummary> result = transactionService.getAllSummaries();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(TransactionType.DEPOSIT);
        }
    }
}
