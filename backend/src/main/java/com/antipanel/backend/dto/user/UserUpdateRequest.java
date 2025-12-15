package com.antipanel.backend.dto.user;

import com.antipanel.backend.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing user.
 * All fields are optional to support partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    /**
     * Updated email address
     */
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * Updated password (only if changing password)
     */
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    /**
     * Updated user role
     */
    private UserRole role;

    /**
     * Updated department
     */
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    /**
     * Updated banned status
     */
    private Boolean isBanned;

    /**
     * Reason for ban (if applicable)
     */
    @Size(max = 500, message = "Banned reason must not exceed 500 characters")
    private String bannedReason;
}
