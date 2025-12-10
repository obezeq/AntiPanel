package com.antipanel.backend.entity.enums;

/**
 * Rol del usuario en el sistema AntiPanel.
 * Mapea el ENUM de PostgreSQL: user_role_enum
 */
public enum UserRole {

    /**
     * Usuario regular con acceso al catálogo y sus órdenes
     */
    USER("user"),

    /**
     * Administrador con acceso total al sistema
     */
    ADMIN("admin"),

    /**
     * Personal de soporte con acceso limitado
     */
    SUPPORT("support");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Obtiene el enum desde el valor de la base de datos
     */
    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role: " + value);
    }
}
