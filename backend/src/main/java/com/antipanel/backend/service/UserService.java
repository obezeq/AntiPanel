package com.antipanel.backend.service;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserProfileUpdateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.dto.user.UserUpdateRequest;
import com.antipanel.backend.entity.enums.UserRole;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for User operations.
 */
public interface UserService {

    // ============ CRUD OPERATIONS ============

    /**
     * Create a new user.
     *
     * @param request User creation data
     * @return Created user response
     */
    UserResponse create(UserCreateRequest request);

    /**
     * Get user by ID.
     *
     * @param id User ID
     * @return User response
     */
    UserResponse getById(Long id);

    /**
     * Get user by email.
     *
     * @param email User email
     * @return User response
     */
    UserResponse getByEmail(String email);

    /**
     * Update user (admin operation).
     *
     * @param id      User ID
     * @param request Update data
     * @return Updated user response
     */
    UserResponse update(Long id, UserUpdateRequest request);

    /**
     * Update user profile (user self-update).
     * SECURITY: This method only allows safe field updates (email, password, department).
     * Sensitive fields (role, isBanned) cannot be modified through this method.
     *
     * @param id      User ID
     * @param request Profile update data
     * @return Updated user response
     */
    UserResponse updateProfile(Long id, UserProfileUpdateRequest request);

    /**
     * Delete user by ID.
     *
     * @param id User ID
     */
    void delete(Long id);

    // ============ LISTING ============

    /**
     * Get all users with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of user responses
     */
    PageResponse<UserResponse> getAll(Pageable pageable);

    /**
     * Get users by role with pagination.
     *
     * @param role     User role
     * @param pageable Pagination parameters
     * @return Page of user responses
     */
    PageResponse<UserResponse> getByRole(UserRole role, Pageable pageable);

    /**
     * Search users by email.
     *
     * @param search   Search term
     * @param pageable Pagination parameters
     * @return Page of user responses
     */
    PageResponse<UserResponse> search(String search, Pageable pageable);

    /**
     * Get users by banned status with pagination.
     *
     * @param isBanned Banned status
     * @param pageable Pagination parameters
     * @return Page of user responses
     */
    PageResponse<UserResponse> getByBannedStatus(Boolean isBanned, Pageable pageable);

    // ============ ROLE OPERATIONS ============

    /**
     * Change user role (admin operation).
     * SECURITY: This is a privileged operation that should only be called from admin endpoints.
     *
     * @param id   User ID
     * @param role New role
     * @return Updated user response
     */
    UserResponse changeRole(Long id, UserRole role);

    // ============ BAN OPERATIONS ============

    /**
     * Ban a user.
     *
     * @param id     User ID
     * @param reason Ban reason
     * @return Updated user response
     */
    UserResponse ban(Long id, String reason);

    /**
     * Unban a user.
     *
     * @param id User ID
     * @return Updated user response
     */
    UserResponse unban(Long id);

    // ============ BALANCE OPERATIONS ============

    /**
     * Adjust user balance (add or subtract).
     *
     * @param id     User ID
     * @param amount Amount to add (positive) or subtract (negative)
     * @return Updated user response
     */
    UserResponse adjustBalance(Long id, BigDecimal amount);

    /**
     * Get users with balance greater than or equal to threshold.
     *
     * @param minBalance Minimum balance
     * @return List of user summaries
     */
    List<UserSummary> getUsersWithBalanceGreaterThan(BigDecimal minBalance);

    // ============ STATISTICS ============

    /**
     * Count users by role.
     *
     * @param role User role
     * @return Number of users with the role
     */
    long countByRole(UserRole role);

    /**
     * Get total balance across all non-banned users.
     *
     * @return Total balance
     */
    BigDecimal getTotalUserBalance();

    /**
     * Get average balance across all non-banned users.
     *
     * @return Average balance
     */
    BigDecimal getAverageUserBalance();

    // ============ VALIDATION ============

    /**
     * Check if email is already in use.
     *
     * @param email Email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if email is already in use by another user.
     *
     * @param email  Email to check
     * @param userId User ID to exclude
     * @return true if email exists for another user
     */
    boolean existsByEmailExcludingUser(String email, Long userId);

    // ============ LOGIN TRACKING ============

    /**
     * Record user login.
     * Updates lastLoginAt and increments loginCount.
     *
     * @param id User ID
     */
    void recordLogin(Long id);
}
