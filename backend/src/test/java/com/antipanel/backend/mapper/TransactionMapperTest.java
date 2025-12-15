package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.transaction.TransactionResponse;
import com.antipanel.backend.dto.transaction.TransactionSummary;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TransactionMapper.
 */
@SpringBootTest(classes = {
        TransactionMapperImpl.class,
        UserMapperImpl.class
})
class TransactionMapperTest {

    @Autowired
    private TransactionMapper mapper;

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        Transaction transaction = createTestTransaction();

        // When
        TransactionResponse response = mapper.toResponse(transaction);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(transaction.getId());
        assertThat(response.getType()).isEqualTo(transaction.getType());
        assertThat(response.getAmount()).isEqualByComparingTo(transaction.getAmount());
        assertThat(response.getBalanceBefore()).isEqualByComparingTo(transaction.getBalanceBefore());
        assertThat(response.getBalanceAfter()).isEqualByComparingTo(transaction.getBalanceAfter());
        assertThat(response.getReferenceType()).isEqualTo(transaction.getReferenceType());
        assertThat(response.getReferenceId()).isEqualTo(transaction.getReferenceId());
        assertThat(response.getDescription()).isEqualTo(transaction.getDescription());
        assertThat(response.getCreatedAt()).isEqualTo(transaction.getCreatedAt());
        // User nested
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(transaction.getUser().getId());
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        Transaction transaction = createTestTransaction();

        // When
        TransactionSummary summary = mapper.toSummary(transaction);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(transaction.getId());
        assertThat(summary.getType()).isEqualTo(transaction.getType());
        assertThat(summary.getAmount()).isEqualByComparingTo(transaction.getAmount());
        assertThat(summary.getBalanceAfter()).isEqualByComparingTo(transaction.getBalanceAfter());
        assertThat(summary.getCreatedAt()).isEqualTo(transaction.getCreatedAt());
    }

    @Test
    void toResponseList_ShouldMapAllTransactions() {
        // Given
        List<Transaction> transactions = List.of(
                createTestTransaction(),
                createTestTransaction()
        );
        transactions.get(1).setId(2L);
        transactions.get(1).setType(TransactionType.ORDER);

        // When
        List<TransactionResponse> responses = mapper.toResponseList(transactions);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
        assertThat(responses.get(0).getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(responses.get(1).getType()).isEqualTo(TransactionType.ORDER);
    }

    @Test
    void toSummaryList_ShouldMapAllTransactions() {
        // Given
        List<Transaction> transactions = List.of(createTestTransaction());

        // When
        List<TransactionSummary> summaries = mapper.toSummaryList(transactions);

        // Then
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).getId()).isEqualTo(1L);
    }

    private Transaction createTestTransaction() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(UserRole.USER);
        user.setBalance(BigDecimal.valueOf(100));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUser(user);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(50.00));
        transaction.setBalanceBefore(BigDecimal.valueOf(50.00));
        transaction.setBalanceAfter(BigDecimal.valueOf(100.00));
        transaction.setReferenceType("invoice");
        transaction.setReferenceId(123L);
        transaction.setDescription("Deposit via PayPal");
        transaction.setCreatedAt(LocalDateTime.now().minusHours(1));
        return transaction;
    }
}
