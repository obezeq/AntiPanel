package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.dto.user.UserUpdateRequest;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.mapper.UserMapper;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of UserService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PageMapper pageMapper;
    private final PasswordEncoder passwordEncoder;

    // ============ CRUD OPERATIONS ============

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        log.debug("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setBalance(BigDecimal.ZERO);
        user.setIsBanned(false);
        user.setLoginCount(0);

        User saved = userRepository.save(user);
        log.info("Created user with ID: {}", saved.getId());

        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse getById(Long id) {
        log.debug("Getting user by ID: {}", id);
        User user = findUserById(id);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        log.debug("Updating user with ID: {}", id);

        User user = findUserById(id);

        // Check email uniqueness if email is being changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email already exists: " + request.getEmail());
            }
        }

        userMapper.updateEntityFromDto(request, user);

        // Handle password update separately
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        log.info("Updated user with ID: {}", saved.getId());

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting user with ID: {}", id);
        User user = findUserById(id);
        userRepository.delete(user);
        log.info("Deleted user with ID: {}", id);
    }

    // ============ LISTING ============

    @Override
    public PageResponse<UserResponse> getAll(Pageable pageable) {
        log.debug("Getting all users with pagination");
        Page<User> page = userRepository.findAll(pageable);
        return pageMapper.toPageResponse(page, userMapper::toResponse);
    }

    @Override
    public PageResponse<UserResponse> getByRole(UserRole role, Pageable pageable) {
        log.debug("Getting users by role: {}", role);
        Page<User> page = userRepository.findByRole(role, pageable);
        return pageMapper.toPageResponse(page, userMapper::toResponse);
    }

    @Override
    public PageResponse<UserResponse> search(String search, Pageable pageable) {
        log.debug("Searching users with term: {}", search);
        Page<User> page = userRepository.searchUsers(search, pageable);
        return pageMapper.toPageResponse(page, userMapper::toResponse);
    }

    @Override
    public PageResponse<UserResponse> getByBannedStatus(Boolean isBanned, Pageable pageable) {
        log.debug("Getting users by banned status: {}", isBanned);
        Page<User> page = userRepository.findByIsBanned(isBanned, pageable);
        return pageMapper.toPageResponse(page, userMapper::toResponse);
    }

    // ============ BAN OPERATIONS ============

    @Override
    @Transactional
    public UserResponse ban(Long id, String reason) {
        log.debug("Banning user with ID: {}", id);
        User user = findUserById(id);
        user.setIsBanned(true);
        user.setBannedReason(reason);
        User saved = userRepository.save(user);
        log.info("Banned user with ID: {}", id);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse unban(Long id) {
        log.debug("Unbanning user with ID: {}", id);
        User user = findUserById(id);
        user.setIsBanned(false);
        user.setBannedReason(null);
        User saved = userRepository.save(user);
        log.info("Unbanned user with ID: {}", id);
        return userMapper.toResponse(saved);
    }

    // ============ BALANCE OPERATIONS ============

    @Override
    @Transactional
    public UserResponse adjustBalance(Long id, BigDecimal amount) {
        log.debug("Adjusting balance for user ID: {} by amount: {}", id, amount);
        User user = findUserById(id);
        BigDecimal newBalance = user.getBalance().add(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ConflictException("Balance cannot be negative. Current: " + user.getBalance() + ", Adjustment: " + amount);
        }

        user.setBalance(newBalance);
        User saved = userRepository.save(user);
        log.info("Adjusted balance for user ID: {} from {} to {}", id, user.getBalance().subtract(amount), newBalance);
        return userMapper.toResponse(saved);
    }

    @Override
    public List<UserSummary> getUsersWithBalanceGreaterThan(BigDecimal minBalance) {
        log.debug("Getting users with balance >= {}", minBalance);
        List<User> users = userRepository.findUsersWithBalanceGreaterThan(minBalance);
        return userMapper.toSummaryList(users);
    }

    // ============ STATISTICS ============

    @Override
    public long countByRole(UserRole role) {
        log.debug("Counting users by role: {}", role);
        return userRepository.countByRole(role);
    }

    @Override
    public BigDecimal getTotalUserBalance() {
        log.debug("Getting total user balance");
        BigDecimal total = userRepository.getTotalUserBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAverageUserBalance() {
        log.debug("Getting average user balance");
        BigDecimal avg = userRepository.getAverageUserBalance();
        return avg != null ? avg : BigDecimal.ZERO;
    }

    // ============ VALIDATION ============

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailExcludingUser(String email, Long userId) {
        return userRepository.findByEmail(email)
                .map(user -> !user.getId().equals(userId))
                .orElse(false);
    }

    // ============ LOGIN TRACKING ============

    @Override
    @Transactional
    public void recordLogin(Long id) {
        log.debug("Recording login for user ID: {}", id);
        User user = findUserById(id);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);
        log.info("Recorded login for user ID: {}, login count: {}", id, user.getLoginCount());
    }

    // ============ HELPER METHODS ============

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
