package com.antipanel.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

/**
 * Exception thrown when user balance is insufficient for an operation.
 * Returns HTTP 402 PAYMENT REQUIRED.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class InsufficientBalanceException extends RuntimeException {

    private final BigDecimal required;
    private final BigDecimal available;

    public InsufficientBalanceException(BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient balance. Required: %s, Available: %s", required, available));
        this.required = required;
        this.available = available;
    }

    public InsufficientBalanceException(String message) {
        super(message);
        this.required = null;
        this.available = null;
    }

    public BigDecimal getRequired() {
        return required;
    }

    public BigDecimal getAvailable() {
        return available;
    }
}
