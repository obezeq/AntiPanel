package com.antipanel.backend.entity.enums;

/**
 * Status enum for balance holds.
 * Tracks the lifecycle of a balance reservation.
 */
public enum BalanceHoldStatus {
    HELD("held"),
    CAPTURED("captured"),
    RELEASED("released"),
    EXPIRED("expired");

    private final String value;

    BalanceHoldStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
