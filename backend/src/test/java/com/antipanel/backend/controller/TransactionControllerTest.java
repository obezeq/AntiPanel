package com.antipanel.backend.controller;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.transaction.TransactionResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionController Tests")
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private CustomUserDetails userDetails;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();

        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hash")
                .role(UserRole.USER)
                .isBanned(false)
                .build();

        userDetails = new CustomUserDetails(testUser);

        UserSummary userSummary = UserSummary.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        transactionResponse = TransactionResponse.builder()
                .id(1L)
                .user(userSummary)
                .amount(new BigDecimal("25.00"))
                .balanceAfter(new BigDecimal("75.00"))
                .type(TransactionType.DEPOSIT)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/transactions")
    class GetUserTransactions {

        @Test
        @DisplayName("Should return user's transactions with pagination")
        void shouldReturnUserTransactionsWithPagination() throws Exception {
            PageResponse<TransactionResponse> pageResponse = PageResponse.<TransactionResponse>builder()
                    .content(List.of(transactionResponse))
                    .totalElements(1L)
                    .totalPages(1)
                    .pageNumber(0)
                    .pageSize(20)
                    .first(true)
                    .last(true)
                    .build();

            when(transactionService.getByUserPaginated(eq(1L), any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/transactions/type")
    class GetTransactionsByType {

        @Test
        @DisplayName("Should return user's transactions filtered by type")
        void shouldReturnUserTransactionsByType() throws Exception {
            when(transactionService.getByUserAndType(1L, TransactionType.DEPOSIT))
                    .thenReturn(List.of(transactionResponse));

            mockMvc.perform(get("/api/v1/transactions/type")
                            .param("type", "DEPOSIT")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/transactions/latest")
    class GetLatestTransaction {

        @Test
        @DisplayName("Should return user's latest transaction")
        void shouldReturnUserLatestTransaction() throws Exception {
            when(transactionService.getLatestByUser(1L)).thenReturn(transactionResponse);

            mockMvc.perform(get("/api/v1/transactions/latest")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("Should return 204 when no transactions")
        void shouldReturn204WhenNoTransactions() throws Exception {
            when(transactionService.getLatestByUser(1L)).thenReturn(null);

            mockMvc.perform(get("/api/v1/transactions/latest")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }
    }
}
