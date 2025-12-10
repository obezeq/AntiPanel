package com.antipanel.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa las categorías principales de servicios.
 * Corresponde a las diferentes redes sociales (Instagram, TikTok, etc.)
 *
 * Tabla: categories
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_sort", columnList = "sort_order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @NotBlank(message = "El slug no puede estar vacío")
    @Size(max = 50, message = "El slug no puede exceder 50 caracteres")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "El slug solo puede contener minúsculas, números y guiones")
    @Column(name = "slug", length = 50, nullable = false, unique = true)
    private String slug;

    @Size(max = 500, message = "La URL del icono no puede exceder 500 caracteres")
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @NotNull(message = "El sort order no puede ser nulo")
    @Min(value = 0, message = "El sort order no puede ser negativo")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @NotNull(message = "El campo is_active no puede ser nulo")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Métodos de utilidad

    /**
     * Genera un slug a partir del nombre
     */
    public static String generateSlug(String name) {
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9]+", "-")
                   .replaceAll("^-|-$", "");
    }
}
