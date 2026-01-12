package com.antipanel.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an external provider API call fails.
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ProviderApiException extends RuntimeException {

    private final String providerName;
    private final String action;

    public ProviderApiException(String message) {
        super(message);
        this.providerName = null;
        this.action = null;
    }

    public ProviderApiException(String providerName, String action, String message) {
        super(String.format("[%s] %s: %s", providerName, action, message));
        this.providerName = providerName;
        this.action = action;
    }

    public ProviderApiException(String providerName, String action, String message, Throwable cause) {
        super(String.format("[%s] %s: %s", providerName, action, message), cause);
        this.providerName = providerName;
        this.action = action;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getAction() {
        return action;
    }
}
