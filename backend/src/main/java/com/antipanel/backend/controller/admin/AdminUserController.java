package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserUpdateRequest;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Admin REST Controller for user management.
 * Requires ADMIN role for all operations.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Users", description = "Admin user management endpoints")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "Get all users with pagination",
            description = "Returns paginated list of all users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Admin: Getting all users with pagination");
        PageResponse<UserResponse> response = userService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user by ID",
            description = "Returns detailed user information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        log.debug("Admin: Getting user by ID: {}", id);
        UserResponse response = userService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search users by email",
            description = "Search users with email containing the query string")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results retrieved",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/search")
    public ResponseEntity<PageResponse<UserResponse>> searchUsers(
            @Parameter(description = "Email search query", example = "john")
            @RequestParam String email,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Admin: Searching users by email: {}", email);
        PageResponse<UserResponse> response = userService.search(email, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get users by role",
            description = "Returns all users with a specific role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping("/role/{role}")
    public ResponseEntity<PageResponse<UserResponse>> getUsersByRole(
            @Parameter(description = "User role", example = "USER")
            @PathVariable UserRole role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Admin: Getting users by role: {}", role);
        PageResponse<UserResponse> response = userService.getByRole(role, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new user",
            description = "Creates a new user account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        log.debug("Admin: Creating new user with email: {}", request.getEmail());
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update user",
            description = "Updates user information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.debug("Admin: Updating user ID: {}", id);
        UserResponse response = userService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user",
            description = "Deletes a user account")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        log.debug("Admin: Deleting user ID: {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ban user",
            description = "Bans a user preventing them from using the platform")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User banned successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/ban")
    public ResponseEntity<UserResponse> banUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Ban reason", example = "Violation of terms")
            @RequestParam(required = false) String reason) {
        log.debug("Admin: Banning user ID: {} with reason: {}", id, reason);
        UserResponse response = userService.ban(id, reason);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Unban user",
            description = "Removes ban from a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User unbanned successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/unban")
    public ResponseEntity<UserResponse> unbanUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        log.debug("Admin: Unbanning user ID: {}", id);
        UserResponse response = userService.unban(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Adjust user balance",
            description = "Manually adjusts a user's balance (positive or negative)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance adjusted successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/balance")
    public ResponseEntity<UserResponse> adjustBalance(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Amount to add (positive) or subtract (negative)", example = "10.00")
            @RequestParam BigDecimal amount) {
        log.debug("Admin: Adjusting balance for user ID: {} by amount: {}", id, amount);
        UserResponse response = userService.adjustBalance(id, amount);
        return ResponseEntity.ok(response);
    }
}
