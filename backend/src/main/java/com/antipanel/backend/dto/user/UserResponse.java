package com.antipanel.backend.dto.user;

import com.antipanel.backend.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for user with all details.
 * Password hash is excluded for security.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /**
     * Unique user ID
     */
    private Long id;

    /**
     * User email address
     */
    private String email;

    /**
     * User role
     */
    private UserRole role;

    /**
     * Department (for staff members)
     */
    private String department;

    /**
     * Current account balance
     */
    private BigDecimal balance;

    /**
     * Whether the user is banned
     */
    private Boolean isBanned;

    /**
     * Reason for ban (if applicable)
     */
    private String bannedReason;

    /**
     * Last login timestamp
     */
    private LocalDateTime lastLoginAt;

    /**
     * Total number of logins
     */
    private Integer loginCount;

    /**
     * Account creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}
