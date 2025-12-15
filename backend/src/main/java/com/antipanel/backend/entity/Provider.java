package com.antipanel.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa los proveedores externos de servicios SMM.
 *
 * Tabla: providers
 */
@Entity
@Table(name = "providers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 255, message = "El website no puede exceder 255 caracteres")
    @Column(name = "website", length = 255)
    private String website;

    @NotBlank(message = "La API URL no puede estar vacía")
    @Size(max = 255, message = "La API URL no puede exceder 255 caracteres")
    @Column(name = "api_url", length = 255, nullable = false)
    private String apiUrl;

    @NotBlank(message = "La API Key no puede estar vacía")
    @Size(max = 255, message = "La API Key no puede exceder 255 caracteres")
    @Column(name = "api_key", length = 255, nullable = false)
    private String apiKey;

    @NotNull(message = "El campo is_active no puede ser nulo")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @DecimalMin(value = "0.0", message = "El balance no puede ser negativo")
    @Column(name = "balance", precision = 12, scale = 4)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
