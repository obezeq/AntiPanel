package com.antipanel.backend.dto.user;

import com.antipanel.backend.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight user summary for nested references.
 * Contains only essential fields needed for display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {

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
     * Current account balance
     */
    private BigDecimal balance;
}
