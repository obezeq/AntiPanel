package com.antipanel.backend.repository;

import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRepository using Testcontainers PostgreSQL.
 *
 * Spring Boot 4.0 Testing Strategy (2025 Best Practices):
 * - @Testcontainers: JUnit 5 extension for container lifecycle management
 * - @ServiceConnection: Auto-configures datasource from container (no @DynamicPropertySource needed)
 * - @AutoConfigureTestDatabase(replace = NONE): Prevents Spring from using embedded H2
 * - PostgreSQL 18: Same database as production for accurate testing
 *
 * Note: Requires Docker to be running for Testcontainers to work.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        User user = createTestUser("test@example.com", UserRole.USER);
        entityManager.persistAndFlush(user);
        entityManager.clear();

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail_WhenUserExists_ShouldReturnTrue() {
        // Given
        User user = createTestUser("existing@example.com", UserRole.USER);
        entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsByEmail("existing@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_WhenUserDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void testFindByRole_ShouldReturnUsersWithSpecificRole() {
        // Given
        User admin1 = createTestUser("admin1@example.com", UserRole.ADMIN);
        User admin2 = createTestUser("admin2@example.com", UserRole.ADMIN);
        User user = createTestUser("user@example.com", UserRole.USER);

        entityManager.persist(admin1);
        entityManager.persist(admin2);
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        // When
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);

        // Then
        assertThat(admins).hasSize(2);
        assertThat(admins).allMatch(u -> u.getRole() == UserRole.ADMIN);
    }

    @Test
    void testSaveUser_ShouldPersistUserToDatabase() {
        // Given
        User user = createTestUser("newuser@example.com", UserRole.USER);

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertNotNull(savedUser.getId());
        Optional<User> found = userRepository.findById(savedUser.getId());
        assertTrue(found.isPresent());
        assertEquals("newuser@example.com", found.get().getEmail());
    }

    @Test
    void testUpdateUser_ShouldModifyExistingUser() {
        // Given
        User user = createTestUser("update@example.com", UserRole.USER);
        user.setBalance(BigDecimal.valueOf(100.00));
        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();

        // When
        savedUser.setBalance(BigDecimal.valueOf(200.00));
        userRepository.save(savedUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<User> updated = userRepository.findById(savedUser.getId());
        assertTrue(updated.isPresent());
        assertThat(updated.get().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
    }

    @Test
    void testDeleteUser_ShouldRemoveUserFromDatabase() {
        // Given
        User user = createTestUser("delete@example.com", UserRole.USER);
        User savedUser = entityManager.persistAndFlush(user);
        Long userId = savedUser.getId();
        entityManager.clear();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<User> deleted = userRepository.findById(userId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testCountAll_ShouldReturnNumberOfUsers() {
        // Given
        entityManager.persist(createTestUser("user1@example.com", UserRole.USER));
        entityManager.persist(createTestUser("user2@example.com", UserRole.USER));
        entityManager.persist(createTestUser("admin@example.com", UserRole.ADMIN));
        entityManager.flush();

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(3);
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Creates a test user with required fields.
     */
    private User createTestUser(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed_password_" + System.currentTimeMillis());
        user.setRole(role);
        user.setBalance(BigDecimal.ZERO);
        return user;
    }
}
