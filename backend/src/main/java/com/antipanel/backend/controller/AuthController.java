package com.antipanel.backend.controller;

import com.antipanel.backend.dto.auth.LoginRequest;
import com.antipanel.backend.dto.auth.LoginResponse;
import com.antipanel.backend.dto.auth.RefreshTokenRequest;
import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.security.CurrentUser;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for authentication operations.
 * All endpoints are public except logout which requires authentication.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login", description = "Authenticate user and receive JWT tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login request for email: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register", description = "Register a new user account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest request) {
        log.debug("Registration request for email: {}", request.getEmail());
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request");
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Logout current user (invalidate session)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser CustomUserDetails currentUser) {
        log.debug("Logout request for user ID: {}", currentUser.getUserId());
        authService.logout(currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
