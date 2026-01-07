package com.antipanel.backend.entity;

import com.antipanel.backend.entity.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa a todos los usuarios del sistema.
 * Incluye usuarios regulares, administradores y personal de soporte.
 *
 * Tabla: users
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_role", columnList = "role"),
    @Index(name = "idx_users_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "El password hash no puede estar vacío")
    @Size(max = 255, message = "El password hash no puede exceder 255 caracteres")
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @NotNull(message = "El rol no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @Size(max = 100, message = "El departamento no puede exceder 100 caracteres")
    @Column(name = "department", length = 100)
    private String department;

    @NotNull(message = "El balance no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El balance no puede ser negativo")
    @Column(name = "balance", precision = 12, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull(message = "El campo is_banned no puede ser nulo")
    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;

    @Column(name = "banned_reason", columnDefinition = "TEXT")
    private String bannedReason;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @NotNull(message = "El login count no puede ser nulo")
    @Min(value = 0, message = "El login count no puede ser negativo")
    @Column(name = "login_count", nullable = false)
    private Integer loginCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Métodos de utilidad

    /**
     * Verifica si el usuario es administrador
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /**
     * Verifica si el usuario es de soporte
     */
    public boolean isSupport() {
        return this.role == UserRole.SUPPORT;
    }

    /**
     * Verifica si el usuario es staff (admin o support)
     */
    public boolean isStaff() {
        return this.role == UserRole.ADMIN || this.role == UserRole.SUPPORT;
    }

    /**
     * Verifica si el usuario tiene balance suficiente
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}
