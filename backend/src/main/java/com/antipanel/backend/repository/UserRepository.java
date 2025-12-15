package com.antipanel.backend.repository;

import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 * Handles database operations for user accounts (regular users, admins, support staff).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ============ BASIC FINDERS ============

    /**
     * Find user by email
     *
     * @param email User email
     * @return Optional user
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     *
     * @param email User email
     * @return true if exists
     */
    boolean existsByEmail(String email);

    // ============ ROLE-BASED QUERIES ============

    /**
     * Find users by role
     *
     * @param role User role
     * @return List of users
     */
    List<User> findByRole(UserRole role);

    /**
     * Find users by multiple roles
     *
     * @param roles List of roles
     * @return List of users
     */
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoles(@Param("roles") List<UserRole> roles);

    // ============ BANNED USERS ============

    /**
     * Find all banned users
     *
     * @return List of banned users
     */
    List<User> findByIsBannedTrue();

    /**
     * Find all non-banned users
     *
     * @return List of active users
     */
    List<User> findByIsBannedFalse();

    // ============ BALANCE QUERIES ============

    /**
     * Find users with balance greater than or equal to threshold
     *
     * @param minBalance Minimum balance
     * @return List of users sorted by balance descending
     */
    @Query("SELECT u FROM User u WHERE u.balance >= :minBalance ORDER BY u.balance DESC")
    List<User> findUsersWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);

    /**
     * Find non-banned users with balance below threshold
     *
     * @param threshold Balance threshold
     * @return List of users with low balance
     */
    @Query("SELECT u FROM User u WHERE u.balance < :threshold AND u.isBanned = false")
    List<User> findUsersWithLowBalance(@Param("threshold") BigDecimal threshold);

    // ============ ACTIVITY QUERIES ============

    /**
     * Find users who logged in since a specific time
     *
     * @param since Minimum login timestamp
     * @return List of active users
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since ORDER BY u.lastLoginAt DESC")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);

    /**
     * Find users who haven't logged in since a specific time or never logged in
     *
     * @param before Maximum login timestamp
     * @return List of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :before OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsersBefore(@Param("before") LocalDateTime before);

    // ============ STATISTICS ============

    /**
     * Count users by role
     *
     * @param role User role
     * @return Number of users with specified role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    /**
     * Calculate total balance across all non-banned users
     *
     * @return Sum of all user balances
     */
    @Query("SELECT SUM(u.balance) FROM User u WHERE u.isBanned = false")
    BigDecimal getTotalUserBalance();

    /**
     * Calculate average balance across all non-banned users
     *
     * @return Average user balance
     */
    @Query("SELECT AVG(u.balance) FROM User u WHERE u.isBanned = false")
    BigDecimal getAverageUserBalance();

    // ============ PAGINATION SUPPORT ============

    /**
     * Find users by role with pagination
     *
     * @param role     User role
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * Find users by banned status with pagination
     *
     * @param isBanned Banned status
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<User> findByIsBanned(Boolean isBanned, Pageable pageable);

    // ============ SEARCH ============

    /**
     * Search users by email (case-insensitive partial match)
     *
     * @param search   Search term
     * @param pageable Pagination parameters
     * @return Page of users matching search
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
}
