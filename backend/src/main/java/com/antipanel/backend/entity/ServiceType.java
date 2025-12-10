package com.antipanel.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entidad que representa los tipos de servicio dentro de cada categoría.
 * Ejemplo: Followers, Likes, Comments, Views, etc.
 *
 * Tabla: service_types
 */
@Entity
@Table(name = "service_types",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_service_types_category_slug",
                         columnNames = {"category_id", "slug"})
    },
    indexes = {
        @Index(name = "idx_service_types_category", columnList = "category_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "La categoría no puede ser nula")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_service_types_category"))
    private Category category;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @NotBlank(message = "El slug no puede estar vacío")
    @Size(max = 50, message = "El slug no puede exceder 50 caracteres")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "El slug solo puede contener minúsculas, números y guiones")
    @Column(name = "slug", length = 50, nullable = false)
    private String slug;

    @NotNull(message = "El sort order no puede ser nulo")
    @Min(value = 0, message = "El sort order no puede ser negativo")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @NotNull(message = "El campo is_active no puede ser nulo")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
