package com.antipanel.backend.entity.enums;

/**
 * Nivel de calidad del servicio SMM.
 * Mapea el ENUM de PostgreSQL: service_quality_enum
 */
public enum ServiceQuality {

    /**
     * Calidad básica - Precio más bajo
     */
    LOW("low"),

    /**
     * Calidad estándar - Balance precio/calidad
     */
    MEDIUM("medium"),

    /**
     * Calidad alta - Mayor calidad
     */
    HIGH("high"),

    /**
     * Calidad premium - Máxima calidad disponible
     */
    PREMIUM("premium");

    private final String value;

    ServiceQuality(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ServiceQuality fromValue(String value) {
        for (ServiceQuality quality : ServiceQuality.values()) {
            if (quality.value.equals(value)) {
                return quality;
            }
        }
        throw new IllegalArgumentException("Unknown service quality: " + value);
    }
}
