package com.antipanel.backend.exception;

/**
 * Exception thrown when Paymento API operations fail.
 * Contains operation context for debugging.
 */
public class PaymentoApiException extends RuntimeException {

    private final String operation;
    private final String details;

    /**
     * Creates exception with operation context.
     *
     * @param operation API operation that failed (e.g., "createPayment", "verifyPayment")
     * @param message Error message
     */
    public PaymentoApiException(String operation, String message) {
        super(formatMessage(operation, message));
        this.operation = operation;
        this.details = message;
    }

    /**
     * Creates exception with operation context and cause.
     *
     * @param operation API operation that failed
     * @param message Error message
     * @param cause Underlying cause
     */
    public PaymentoApiException(String operation, String message, Throwable cause) {
        super(formatMessage(operation, message), cause);
        this.operation = operation;
        this.details = message;
    }

    /**
     * Gets the operation that failed.
     *
     * @return Operation name
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Gets the error details.
     *
     * @return Error details
     */
    public String getDetails() {
        return details;
    }

    /**
     * Formats the error message with operation context.
     */
    private static String formatMessage(String operation, String message) {
        return String.format("Paymento API error [%s]: %s", operation, message);
    }
}
