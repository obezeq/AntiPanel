package com.antipanel.backend.entity.enums;

/**
 * Estado de una solicitud de refill.
 * Mapea el ENUM de PostgreSQL: refill_status_enum
 */
public enum RefillStatus {

    /**
     * Solicitud de refill recibida
     */
    PENDING("pending"),

    /**
     * Refill enviado al proveedor
     */
    PROCESSING("processing"),

    /**
     * Refill completado exitosamente
     */
    COMPLETED("completed"),

    /**
     * Refill rechazado por el proveedor
     */
    REJECTED("rejected"),

    /**
     * Refill cancelado
     */
    CANCELLED("cancelled");

    private final String value;

    RefillStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RefillStatus fromValue(String value) {
        for (RefillStatus status : RefillStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown refill status: " + value);
    }

    /**
     * Verifica si el refill está en un estado final
     */
    public boolean isFinal() {
        return this == COMPLETED || this == REJECTED || this == CANCELLED;
    }
}
