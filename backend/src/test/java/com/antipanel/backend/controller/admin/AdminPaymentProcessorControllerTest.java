package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorCreateRequest;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorResponse;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorUpdateRequest;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.PaymentProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentProcessorController Tests")
class AdminPaymentProcessorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentProcessorService paymentProcessorService;

    @InjectMocks
    private AdminPaymentProcessorController adminPaymentProcessorController;

    private ObjectMapper objectMapper;
    private CustomUserDetails adminDetails;
    private PaymentProcessorResponse paymentProcessorResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminPaymentProcessorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        User adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .balance(BigDecimal.ZERO)
                .isBanned(false)
                .build();

        adminDetails = new CustomUserDetails(adminUser);

        paymentProcessorResponse = PaymentProcessorResponse.builder()
                .id(1)
                .code("PAYPAL")
                .name("PayPal")
                .minAmount(new BigDecimal("5.00"))
                .maxAmount(new BigDecimal("10000.00"))
                .feePercentage(new BigDecimal("2.9"))
                .feeFixed(new BigDecimal("0.30"))
                .isActive(true)
                .sortOrder(0)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/payment-processors")
    class GetAllPaymentProcessors {

        @Test
        @DisplayName("Should return all payment processors")
        void shouldReturnAllPaymentProcessors() throws Exception {
            when(paymentProcessorService.getAll()).thenReturn(List.of(paymentProcessorResponse));

            mockMvc.perform(get("/api/v1/admin/payment-processors")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("PayPal"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/payment-processors/active")
    class GetActivePaymentProcessors {

        @Test
        @DisplayName("Should return all active payment processors")
        void shouldReturnAllActivePaymentProcessors() throws Exception {
            when(paymentProcessorService.getAllActive()).thenReturn(List.of(paymentProcessorResponse));

            mockMvc.perform(get("/api/v1/admin/payment-processors/active")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].isActive").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/payment-processors/{id}")
    class GetPaymentProcessorById {

        @Test
        @DisplayName("Should return payment processor by ID")
        void shouldReturnPaymentProcessorById() throws Exception {
            when(paymentProcessorService.getById(1)).thenReturn(paymentProcessorResponse);

            mockMvc.perform(get("/api/v1/admin/payment-processors/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("PayPal"))
                    .andExpect(jsonPath("$.code").value("PAYPAL"));
        }

        @Test
        @DisplayName("Should return 404 when payment processor not found")
        void shouldReturn404WhenPaymentProcessorNotFound() throws Exception {
            when(paymentProcessorService.getById(999))
                    .thenThrow(new ResourceNotFoundException("PaymentProcessor", "id", 999));

            mockMvc.perform(get("/api/v1/admin/payment-processors/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/payment-processors")
    class CreatePaymentProcessor {

        @Test
        @DisplayName("Should create new payment processor")
        void shouldCreateNewPaymentProcessor() throws Exception {
            PaymentProcessorCreateRequest request = PaymentProcessorCreateRequest.builder()
                    .code("STRIPE")
                    .name("Stripe")
                    .minAmount(new BigDecimal("1.00"))
                    .maxAmount(new BigDecimal("50000.00"))
                    .feePercentage(new BigDecimal("2.9"))
                    .feeFixed(new BigDecimal("0.30"))
                    .sortOrder(1)
                    .build();

            PaymentProcessorResponse createdProcessor = PaymentProcessorResponse.builder()
                    .id(2)
                    .code("STRIPE")
                    .name("Stripe")
                    .minAmount(new BigDecimal("1.00"))
                    .maxAmount(new BigDecimal("50000.00"))
                    .feePercentage(new BigDecimal("2.9"))
                    .feeFixed(new BigDecimal("0.30"))
                    .isActive(true)
                    .sortOrder(1)
                    .build();

            when(paymentProcessorService.create(any(PaymentProcessorCreateRequest.class)))
                    .thenReturn(createdProcessor);

            mockMvc.perform(post("/api/v1/admin/payment-processors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.name").value("Stripe"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/payment-processors/{id}")
    class UpdatePaymentProcessor {

        @Test
        @DisplayName("Should update payment processor")
        void shouldUpdatePaymentProcessor() throws Exception {
            PaymentProcessorUpdateRequest request = PaymentProcessorUpdateRequest.builder()
                    .name("Updated PayPal")
                    .feePercentage(new BigDecimal("2.5"))
                    .build();

            PaymentProcessorResponse updatedProcessor = PaymentProcessorResponse.builder()
                    .id(1)
                    .code("PAYPAL")
                    .name("Updated PayPal")
                    .feePercentage(new BigDecimal("2.5"))
                    .isActive(true)
                    .build();

            when(paymentProcessorService.update(eq(1), any(PaymentProcessorUpdateRequest.class)))
                    .thenReturn(updatedProcessor);

            mockMvc.perform(put("/api/v1/admin/payment-processors/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated PayPal"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/payment-processors/{id}")
    class DeletePaymentProcessor {

        @Test
        @DisplayName("Should delete payment processor")
        void shouldDeletePaymentProcessor() throws Exception {
            doNothing().when(paymentProcessorService).delete(1);

            mockMvc.perform(delete("/api/v1/admin/payment-processors/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(paymentProcessorService, times(1)).delete(1);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/payment-processors/{id}/toggle-active")
    class ToggleActive {

        @Test
        @DisplayName("Should toggle payment processor active status")
        void shouldTogglePaymentProcessorActiveStatus() throws Exception {
            PaymentProcessorResponse toggledProcessor = PaymentProcessorResponse.builder()
                    .id(1)
                    .code("PAYPAL")
                    .name("PayPal")
                    .isActive(false)
                    .build();

            when(paymentProcessorService.toggleActive(1)).thenReturn(toggledProcessor);

            mockMvc.perform(patch("/api/v1/admin/payment-processors/1/toggle-active")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }
}
