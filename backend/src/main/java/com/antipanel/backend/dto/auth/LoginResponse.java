package com.antipanel.backend.dto.auth;

import com.antipanel.backend.dto.user.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful login.
 * Contains access token, refresh token, and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT access token.
     */
    private String accessToken;

    /**
     * JWT refresh token.
     */
    private String refreshToken;

    /**
     * Token type (always "Bearer").
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token expiration time in seconds.
     */
    private Long expiresIn;

    /**
     * Authenticated user information.
     */
    private UserSummary user;
}
