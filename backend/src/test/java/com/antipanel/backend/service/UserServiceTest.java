package com.antipanel.backend.service;

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
import com.antipanel.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PageMapper pageMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash("hashedPassword")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(100))
                .isBanned(false)
                .loginCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .email("user@example.com")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(100))
                .isBanned(false)
                .loginCount(0)
                .build();

        createRequest = UserCreateRequest.builder()
                .email("user@example.com")
                .password("password123")
                .role(UserRole.USER)
                .build();

        updateRequest = UserUpdateRequest.builder()
                .email("updated@example.com")
                .build();
    }

    // ============ CREATE TESTS ============

    @Test
    void create_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserCreateRequest.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUserResponse.getEmail());
        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_EmailAlreadyExists_ThrowsConflictException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    // ============ GET BY ID TESTS ============

    @Test
    void getById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.getById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ============ GET BY EMAIL TESTS ============

    @Test
    void getByEmail_Success() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.getByEmail("user@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void getByEmail_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getByEmail("notfound@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ============ UPDATE TESTS ============

    @Test
    void update_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.update(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userMapper).updateEntityFromDto(eq(updateRequest), any(User.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_EmailConflict_ThrowsConflictException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.update(1L, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void update_WithPassword_EncodesPassword() {
        // Given
        UserUpdateRequest requestWithPassword = UserUpdateRequest.builder()
                .password("newPassword123")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // When
        userService.update(1L, requestWithPassword);

        // Then
        verify(passwordEncoder).encode("newPassword123");
    }

    // ============ DELETE TESTS ============

    @Test
    void delete_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // When
        userService.delete(1L);

        // Then
        verify(userRepository).delete(testUser);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ LISTING TESTS ============

    @Test
    void getAll_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(testUser));
        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .content(List.of(testUserResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(userRepository.findAll(pageable)).thenReturn(page);
        when(pageMapper.toPageResponse(any(Page.class), any(Function.class))).thenReturn(pageResponse);

        // When
        PageResponse<UserResponse> result = userService.getAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getByRole_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(testUser));
        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .content(List.of(testUserResponse))
                .build();

        when(userRepository.findByRole(UserRole.USER, pageable)).thenReturn(page);
        when(pageMapper.toPageResponse(any(Page.class), any(Function.class))).thenReturn(pageResponse);

        // When
        PageResponse<UserResponse> result = userService.getByRole(UserRole.USER, pageable);

        // Then
        assertThat(result).isNotNull();
    }

    // ============ BAN OPERATIONS TESTS ============

    @Test
    void ban_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            return saved;
        });
        when(userMapper.toResponse(any(User.class))).thenReturn(
                UserResponse.builder().id(1L).isBanned(true).bannedReason("Violation").build()
        );

        // When
        UserResponse result = userService.ban(1L, "Violation");

        // Then
        assertThat(result.getIsBanned()).isTrue();
        verify(userRepository).save(argThat(user ->
                user.getIsBanned() && "Violation".equals(user.getBannedReason())));
    }

    @Test
    void unban_Success() {
        // Given
        testUser.setIsBanned(true);
        testUser.setBannedReason("Previous ban");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(
                UserResponse.builder().id(1L).isBanned(false).build()
        );

        // When
        UserResponse result = userService.unban(1L);

        // Then
        assertThat(result.getIsBanned()).isFalse();
        verify(userRepository).save(argThat(user ->
                !user.getIsBanned() && user.getBannedReason() == null));
    }

    // ============ BALANCE OPERATIONS TESTS ============

    @Test
    void adjustBalance_AddFunds_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // When
        userService.adjustBalance(1L, BigDecimal.valueOf(50));

        // Then
        verify(userRepository).save(argThat(user ->
                user.getBalance().compareTo(BigDecimal.valueOf(150)) == 0));
    }

    @Test
    void adjustBalance_SubtractFunds_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // When
        userService.adjustBalance(1L, BigDecimal.valueOf(-50));

        // Then
        verify(userRepository).save(argThat(user ->
                user.getBalance().compareTo(BigDecimal.valueOf(50)) == 0));
    }

    @Test
    void adjustBalance_NegativeResult_ThrowsConflictException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> userService.adjustBalance(1L, BigDecimal.valueOf(-150)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Balance cannot be negative");
    }

    @Test
    void getUsersWithBalanceGreaterThan_Success() {
        // Given
        UserSummary summary = UserSummary.builder().id(1L).email("user@example.com").build();
        when(userRepository.findUsersWithBalanceGreaterThan(BigDecimal.valueOf(50)))
                .thenReturn(List.of(testUser));
        when(userMapper.toSummaryList(List.of(testUser))).thenReturn(List.of(summary));

        // When
        List<UserSummary> result = userService.getUsersWithBalanceGreaterThan(BigDecimal.valueOf(50));

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATISTICS TESTS ============

    @Test
    void countByRole_Success() {
        // Given
        when(userRepository.countByRole(UserRole.USER)).thenReturn(10L);

        // When
        long count = userService.countByRole(UserRole.USER);

        // Then
        assertThat(count).isEqualTo(10L);
    }

    @Test
    void getTotalUserBalance_Success() {
        // Given
        when(userRepository.getTotalUserBalance()).thenReturn(BigDecimal.valueOf(1000));

        // When
        BigDecimal total = userService.getTotalUserBalance();

        // Then
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void getTotalUserBalance_NullReturnsZero() {
        // Given
        when(userRepository.getTotalUserBalance()).thenReturn(null);

        // When
        BigDecimal total = userService.getTotalUserBalance();

        // Then
        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ============ LOGIN TRACKING TESTS ============

    @Test
    void recordLogin_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.recordLogin(1L);

        // Then
        verify(userRepository).save(argThat(user ->
                user.getLoginCount() == 1 && user.getLastLoginAt() != null));
    }

    // ============ VALIDATION TESTS ============

    @Test
    void existsByEmail_ReturnsTrue() {
        // Given
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        // When
        boolean exists = userService.existsByEmail("user@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailExcludingUser_SameUser_ReturnsFalse() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // When
        boolean exists = userService.existsByEmailExcludingUser("user@example.com", 1L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmailExcludingUser_DifferentUser_ReturnsTrue() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // When
        boolean exists = userService.existsByEmailExcludingUser("user@example.com", 2L);

        // Then
        assertThat(exists).isTrue();
    }
}
