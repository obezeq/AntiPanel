package com.antipanel.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user self-profile updates.
 * SECURITY: This DTO intentionally excludes sensitive fields (role, isBanned, bannedReason)
 * that should only be modifiable by admins.
 * Use UserUpdateRequest for admin operations only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

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
     * Updated department
     */
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
}
