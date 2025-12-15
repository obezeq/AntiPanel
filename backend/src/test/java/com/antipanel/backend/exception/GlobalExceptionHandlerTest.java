package com.antipanel.backend.exception;

import com.antipanel.backend.exception.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
    }

    @Test
    void handleResourceNotFound_ShouldReturn404() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", 123L);

        // When
        var response = exceptionHandler.handleResourceNotFound(ex, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("User not found with id: '123'");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    void handleBadRequest_ShouldReturn400() {
        // Given
        BadRequestException ex = new BadRequestException("Invalid input data");

        // When
        var response = exceptionHandler.handleBadRequest(ex, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid input data");
    }

    @Test
    void handleUnauthorized_ShouldReturn401() {
        // Given
        UnauthorizedException ex = new UnauthorizedException("Invalid token");

        // When
        var response = exceptionHandler.handleUnauthorized(ex, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
    }

    @Test
    void handleForbidden_ShouldReturn403() {
        // Given
        ForbiddenException ex = new ForbiddenException("Admin access required");

        // When
        var response = exceptionHandler.handleForbidden(ex, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
    }

    @Test
    void handleConflict_ShouldReturn409() {
        // Given
        ConflictException ex = new ConflictException("Email already exists");

        // When
        var response = exceptionHandler.handleConflict(ex, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Email already exists");
    }

    @Test
    void handleInsufficientBalance_ShouldReturn402WithDetails() {
        // Given
        BigDecimal required = BigDecimal.valueOf(100.00);
        BigDecimal available = BigDecimal.valueOf(50.00);
        InsufficientBalanceException ex = new InsufficientBalanceException(required, available);

        // When
        var response = exceptionHandler.handleInsufficientBalance(ex, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(402);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(402);
        assertThat(response.getBody().getError()).isEqualTo("Payment Required");
        assertThat(response.getBody().getDetails()).isNotNull();
        assertThat(response.getBody().getDetails()).containsKey("required");
        assertThat(response.getBody().getDetails()).containsKey("available");
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");

        // When
        var response = exceptionHandler.handleGenericException(ex, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        // Message should not expose internal details
        assertThat(response.getBody().getMessage()).doesNotContain("Unexpected error");
    }

    @Test
    void errorResponse_ShouldHaveTimestamp() {
        // Given
        BadRequestException ex = new BadRequestException("Test");

        // When
        var response = exceptionHandler.handleBadRequest(ex, request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
