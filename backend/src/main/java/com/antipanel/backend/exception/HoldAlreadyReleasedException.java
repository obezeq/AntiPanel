package com.antipanel.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to capture/release a hold that is no longer in HELD status.
 * Returns HTTP 409 CONFLICT.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class HoldAlreadyReleasedException extends RuntimeException {

    private final Long holdId;

    public HoldAlreadyReleasedException(String message) {
        super(message);
        this.holdId = null;
    }

    public HoldAlreadyReleasedException(Long holdId, String message) {
        super(message);
        this.holdId = holdId;
    }

    public Long getHoldId() {
        return holdId;
    }
}
