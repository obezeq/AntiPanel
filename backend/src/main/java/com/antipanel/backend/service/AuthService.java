package com.antipanel.backend.service;

import com.antipanel.backend.dto.auth.LoginRequest;
import com.antipanel.backend.dto.auth.LoginResponse;
import com.antipanel.backend.dto.auth.RefreshTokenRequest;
import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserResponse;

/**
 * Service interface for authentication operations.
 * Handles login, registration, token refresh, and logout.
 */
public interface AuthService {

    /**
     * Authenticate user and generate tokens.
     *
     * @param request Login credentials
     * @return Login response with tokens and user info
     */
    LoginResponse login(LoginRequest request);

    /**
     * Register a new user account.
     *
     * @param request User registration data
     * @return Created user response
     */
    UserResponse register(UserCreateRequest request);

    /**
     * Refresh access token using refresh token.
     *
     * @param request Refresh token request
     * @return New login response with fresh tokens
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logout user (invalidate tokens if applicable).
     * For stateless JWT, this is mainly for client-side cleanup.
     *
     * @param userId User ID to logout
     */
    void logout(Long userId);
}
