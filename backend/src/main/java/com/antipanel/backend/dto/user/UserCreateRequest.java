package com.antipanel.backend.dto.user;

import com.antipanel.backend.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new user (registration).
 * Used for both regular users and admin-created accounts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    /**
     * User email address (used for login)
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * User password (will be hashed)
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    /**
     * User role (default: USER)
     */
    @NotNull(message = "Role is required")
    @Builder.Default
    private UserRole role = UserRole.USER;

    /**
     * Department (for staff members only)
     */
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
}
