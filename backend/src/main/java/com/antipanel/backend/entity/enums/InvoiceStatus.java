package com.antipanel.backend.entity.enums;

/**
 * Estado de una factura/depósito.
 * Mapea el ENUM de PostgreSQL: invoice_status_enum
 */
public enum InvoiceStatus {

    /**
     * Factura creada, esperando pago
     */
    PENDING("pending"),

    /**
     * Pago en proceso de verificación
     */
    PROCESSING("processing"),

    /**
     * Pago completado y acreditado
     */
    COMPLETED("completed"),

    /**
     * Pago fallido
     */
    FAILED("failed"),

    /**
     * Factura cancelada por el usuario
     */
    CANCELLED("cancelled"),

    /**
     * Factura expirada por tiempo
     */
    EXPIRED("expired");

    private final String value;

    InvoiceStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static InvoiceStatus fromValue(String value) {
        for (InvoiceStatus status : InvoiceStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown invoice status: " + value);
    }

    /**
     * Verifica si la factura fue exitosa
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    /**
     * Verifica si la factura está en un estado final
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == EXPIRED;
    }
}
