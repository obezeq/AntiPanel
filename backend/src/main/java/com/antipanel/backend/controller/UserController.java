package com.antipanel.backend.controller;

import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserUpdateRequest;
import com.antipanel.backend.security.CurrentUser;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for user profile operations.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile",
            description = "Returns the profile information of the currently authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser CustomUserDetails currentUser) {
        log.debug("Getting profile for user ID: {}", currentUser.getUserId());
        UserResponse response = userService.getById(currentUser.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update current user profile",
            description = "Updates the profile information of the currently authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody UserUpdateRequest request) {
        log.debug("Updating profile for user ID: {}", currentUser.getUserId());
        UserResponse response = userService.update(currentUser.getUserId(), request);
        return ResponseEntity.ok(response);
    }
}
