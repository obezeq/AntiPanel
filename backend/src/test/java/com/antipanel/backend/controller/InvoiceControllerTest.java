package com.antipanel.backend.controller;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.invoice.InvoiceCreateRequest;
import com.antipanel.backend.dto.invoice.InvoiceResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceController Tests")
class InvoiceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    private ObjectMapper objectMapper;
    private CustomUserDetails userDetails;
    private InvoiceResponse invoiceResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(invoiceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();

        objectMapper = new ObjectMapper();

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

        invoiceResponse = InvoiceResponse.builder()
                .id(1L)
                .user(userSummary)
                .amount(new BigDecimal("50.00"))
                .status(InvoiceStatus.PENDING)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Nested
    @DisplayName("POST /api/v1/invoices")
    class CreateInvoice {

        @Test
        @DisplayName("Should create invoice successfully")
        void shouldCreateInvoiceSuccessfully() throws Exception {
            InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                    .amount(new BigDecimal("50.00"))
                    .processorId(1)
                    .build();

            when(invoiceService.create(eq(1L), any(InvoiceCreateRequest.class))).thenReturn(invoiceResponse);

            mockMvc.perform(post("/api/v1/invoices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.amount").value(50.00));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/invoices")
    class GetUserInvoices {

        @Test
        @DisplayName("Should return user's invoices with pagination")
        void shouldReturnUserInvoicesWithPagination() throws Exception {
            PageResponse<InvoiceResponse> pageResponse = PageResponse.<InvoiceResponse>builder()
                    .content(List.of(invoiceResponse))
                    .totalElements(1L)
                    .totalPages(1)
                    .pageNumber(0)
                    .pageSize(20)
                    .first(true)
                    .last(true)
                    .build();

            when(invoiceService.getByUserPaginated(eq(1L), any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/invoices")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/invoices/{id}")
    class GetInvoice {

        @Test
        @DisplayName("Should return invoice for owner")
        void shouldReturnInvoiceForOwner() throws Exception {
            when(invoiceService.getById(1L)).thenReturn(invoiceResponse);

            mockMvc.perform(get("/api/v1/invoices/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("Should return 403 for non-owner")
        void shouldReturn403ForNonOwner() throws Exception {
            UserSummary otherUser = UserSummary.builder()
                    .id(999L)
                    .email("other@example.com")
                    .build();

            InvoiceResponse otherUserInvoice = InvoiceResponse.builder()
                    .id(1L)
                    .user(otherUser)
                    .amount(new BigDecimal("50.00"))
                    .status(InvoiceStatus.PENDING)
                    .build();

            when(invoiceService.getById(1L)).thenReturn(otherUserInvoice);

            mockMvc.perform(get("/api/v1/invoices/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/invoices/pending")
    class GetPendingInvoices {

        @Test
        @DisplayName("Should return user's pending invoices")
        void shouldReturnUserPendingInvoices() throws Exception {
            when(invoiceService.getPendingByUser(1L)).thenReturn(List.of(invoiceResponse));

            mockMvc.perform(get("/api/v1/invoices/pending")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }
    }
}
