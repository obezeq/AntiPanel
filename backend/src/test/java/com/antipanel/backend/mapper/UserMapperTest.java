package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.dto.user.UserUpdateRequest;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserMapper.
 */
class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        User user = createTestUser();

        // When
        UserResponse response = mapper.toResponse(user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getRole()).isEqualTo(user.getRole());
        assertThat(response.getDepartment()).isEqualTo(user.getDepartment());
        assertThat(response.getBalance()).isEqualByComparingTo(user.getBalance());
        assertThat(response.getIsBanned()).isEqualTo(user.getIsBanned());
        assertThat(response.getLoginCount()).isEqualTo(user.getLoginCount());
        assertThat(response.getCreatedAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    void toEntity_ShouldMapBasicFields() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .role(UserRole.USER)
                .department("Sales")
                .build();

        // When
        User user = mapper.toEntity(request);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();  // ID should be null for new entity
        assertThat(user.getEmail()).isEqualTo(request.getEmail());
        assertThat(user.getRole()).isEqualTo(request.getRole());
        assertThat(user.getDepartment()).isEqualTo(request.getDepartment());
        assertThat(user.getPasswordHash()).isNull();  // Password hash should be set by service
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {
        // Given
        User user = createTestUser();
        String originalEmail = user.getEmail();
        UserRole originalRole = user.getRole();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .department("Engineering")  // Only update department
                .isBanned(true)
                .bannedReason("Violation of terms")
                .build();

        // When
        mapper.updateEntityFromDto(request, user);

        // Then
        assertThat(user.getEmail()).isEqualTo(originalEmail);  // Unchanged
        assertThat(user.getRole()).isEqualTo(originalRole);    // Unchanged
        assertThat(user.getDepartment()).isEqualTo("Engineering");  // Updated
        assertThat(user.getIsBanned()).isTrue();
        assertThat(user.getBannedReason()).isEqualTo("Violation of terms");
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        User user = createTestUser();

        // When
        UserSummary summary = mapper.toSummary(user);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(user.getId());
        assertThat(summary.getEmail()).isEqualTo(user.getEmail());
        assertThat(summary.getRole()).isEqualTo(user.getRole());
        assertThat(summary.getBalance()).isEqualByComparingTo(user.getBalance());
    }

    @Test
    void toResponseList_ShouldMapAllUsers() {
        // Given
        List<User> users = List.of(
                createTestUser(),
                createTestUser()
        );
        users.get(1).setId(2L);
        users.get(1).setEmail("user2@example.com");

        // When
        List<UserResponse> responses = mapper.toResponseList(users);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(responses.get(1).getEmail()).isEqualTo("user2@example.com");
    }

    @Test
    void toSummaryList_ShouldMapAllUsersToSummaries() {
        // Given
        List<User> users = List.of(createTestUser());

        // When
        List<UserSummary> summaries = mapper.toSummaryList(users);

        // Then
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).getId()).isEqualTo(1L);
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setRole(UserRole.USER);
        user.setDepartment("Sales");
        user.setBalance(BigDecimal.valueOf(100.00));
        user.setIsBanned(false);
        user.setLoginCount(5);
        user.setCreatedAt(LocalDateTime.now().minusDays(30));
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
