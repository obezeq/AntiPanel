package com.antipanel.backend.entity.enums;

/**
 * Velocidad de entrega del servicio SMM.
 * Mapea el ENUM de PostgreSQL: service_speed_enum
 */
public enum ServiceSpeed {

    /**
     * Entrega lenta - Varios días
     */
    SLOW("slow"),

    /**
     * Velocidad estándar - 24-48 horas
     */
    MEDIUM("medium"),

    /**
     * Entrega rápida - 1-24 horas
     */
    FAST("fast"),

    /**
     * Entrega inmediata - Minutos
     */
    INSTANT("instant");

    private final String value;

    ServiceSpeed(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ServiceSpeed fromValue(String value) {
        for (ServiceSpeed speed : ServiceSpeed.values()) {
            if (speed.value.equals(value)) {
                return speed;
            }
        }
        throw new IllegalArgumentException("Unknown service speed: " + value);
    }
}
