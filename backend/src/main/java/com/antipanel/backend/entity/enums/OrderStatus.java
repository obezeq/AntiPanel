package com.antipanel.backend.entity.enums;

/**
 * Estado de una orden en el sistema.
 * Mapea el ENUM de PostgreSQL: order_status_enum
 */
public enum OrderStatus {

    /**
     * Orden recibida, pendiente de procesar
     */
    PENDING("pending"),

    /**
     * Orden enviada al proveedor, en proceso
     */
    PROCESSING("processing"),

    /**
     * El proveedor está entregando el servicio
     */
    IN_PROGRESS("in_progress"),

    /**
     * Orden completada exitosamente
     */
    COMPLETED("completed"),

    /**
     * Orden completada parcialmente
     */
    PARTIAL("partial"),

    /**
     * Orden cancelada antes de procesar
     */
    CANCELLED("cancelled"),

    /**
     * Orden reembolsada al usuario
     */
    REFUNDED("refunded");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + value);
    }

    /**
     * Verifica si la orden está en un estado final
     */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * Verifica si la orden está en proceso
     */
    public boolean isInProgress() {
        return this == PROCESSING || this == IN_PROGRESS;
    }
}
