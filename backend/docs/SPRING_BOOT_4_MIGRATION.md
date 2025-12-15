# 🧪 Spring Boot 4.0 Testing - Best Practices

**Official Spring Boot 4.0 Testing Guide** for AntiPanel Backend with Java 25 and PostgreSQL 18.

---

## 📚 Official Documentation

- **Spring Boot 4.0 @DataJpaTest API:** [Official Docs](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/data/jpa/test/autoconfigure/DataJpaTest.html)
- **Spring Boot 4.0 Migration Guide:** [GitHub Wiki](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- **What's New in Spring Boot 4.0 Testing:** [Rieckpil Blog](https://rieckpil.de/whats-new-for-testing-in-spring-boot-4-0-and-spring-framework-7/)

---

## ⚠️ CRITICAL CHANGES from Spring Boot 3 → 4

### 1. **@SpringBootTest No Longer Provides Auto-Configuration**
   - ❌ MockMVC no longer auto-configured
   - ✅ Must explicitly add `@AutoConfigureMockMvc`
   - ✅ Must explicitly add `@AutoConfigureWebTestClient`
   - ✅ Must explicitly add `@AutoConfigureTestRestTemplate`

### 2. **JUnit 6 Required (JUnit 5/Jupiter)**
   - ❌ JUnit 4 support completely removed
   - ✅ JUnit Jupiter 6.0+ mandatory
   - ✅ Mockito 5.20+

### 3. **Test Dependency Changes**
   - ❌ `spring-boot-starter-test` alone is not enough
   - ✅ Use technology-specific test starters: `spring-boot-starter-data-jpa-test`

### 4. **MockitoTestExecutionListener Removed**
   - ❌ `MockitoTestExecutionListener` deprecated and removed
   - ✅ Use `MockitoExtension` from Mockito itself

### 5. **Bean Override Improvements**
   - ✅ Can now override prototype and custom-scoped beans (not just singletons)

---

## 📦 Correct Dependencies (Spring Boot 4.0)

### **build.gradle**

```gradle
dependencies {
    // Main dependencies
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Testing dependencies - Spring Boot 4.0 Best Practices
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test' // ← REQUIRED!
    testImplementation 'org.springframework.security:spring-security-test'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform() // JUnit 6 (Jupiter)
}
```

---

## 🧩 Repository Tests with @DataJpaTest

### **Correct Imports (Spring Boot 4.0)**

⚠️ **IMPORTANT:** Package paths are **different** from Spring Boot 3!

```java
// Spring Boot 4.0 Imports
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// ❌ WRONG (Spring Boot 3 packages):
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
```

### **Example: UserRepositoryTest.java**

```java
package com.antipanel.backend.repository;

import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository test using Spring Boot 4.0 @DataJpaTest.
 *
 * Features:
 * - Focuses ONLY on JPA components (repositories + entities)
 * - Transactional by default (auto-rollback after each test)
 * - Uses real PostgreSQL database (not H2)
 * - SQL queries logged by default
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setRole(UserRole.USER);
        user.setBalance(BigDecimal.ZERO);
        user.setActive(true);

        entityManager.persistAndFlush(user);
        entityManager.clear(); // Clear L1 cache for realistic test

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testExistsByEmail_WhenUserExists_ShouldReturnTrue() {
        // Given
        User user = new User();
        user.setEmail("existing@example.com");
        user.setPasswordHash("password");
        user.setRole(UserRole.USER);
        user.setBalance(BigDecimal.ZERO);
        user.setActive(true);

        entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsByEmail("existing@example.com");

        // Then
        assertTrue(exists);
    }
}
```

### **@DataJpaTest Features (Spring Boot 4.0)**

✅ **Auto-Configuration:**
- Only loads JPA-related configuration
- Scans for `@Entity` classes
- Configures Spring Data JPA repositories
- Uses embedded database by default (can be overridden)

✅ **Transactional:**
- Each test runs in a transaction
- Auto-rollback after each test (no database pollution)
- Can be disabled with `@Transactional(propagation = Propagation.NOT_SUPPORTED)`

✅ **TestEntityManager:**
- Alternative to standard JPA `EntityManager`
- Provides test-specific methods:
  - `persistAndFlush(entity)` - persist and flush immediately
  - `clear()` - clear persistence context (L1 cache)
  - `flush()` - flush pending changes to DB
  - `detach(entity)` - detach entity from context

✅ **PostgreSQL Integration:**
- `@AutoConfigureTestDatabase(replace = Replace.NONE)` uses real PostgreSQL
- Requires database running (via docker-compose-test.yml)
- Schema created via `spring.jpa.hibernate.ddl-auto=create-drop`

---

## 🧪 Service Tests with @SpringBootTest

```java
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateUser() {
        // Test logic here
    }
}
```

**Note:** `@SpringBootTest` loads the **entire application context** (slower than `@DataJpaTest`).

---

## 🎯 Controller Tests with @WebMvcTest

**Spring Boot 4.0 IMPORTANT:** Must explicitly add `@AutoConfigureMockMvc`!

```java
import org.springframework.boot.test.autoconfigure.webmvc.WebMvcTest;
import org.springframework.boot.test.autoconfigure.webmvc.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc  // ← REQUIRED in Spring Boot 4.0!
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk());
    }
}
```

---

## 📋 Testing Best Practices (Spring Boot 4.0 + Java 25)

### ✅ 1. Use Correct Test Slices
- `@DataJpaTest` for repository tests (JPA only)
- `@WebMvcTest` for controller tests (Web layer only)
- `@SpringBootTest` for integration tests (full context)

### ✅ 2. Prefer Slices Over @SpringBootTest
- Faster test execution
- Focused on specific layers
- Less memory usage

### ✅ 3. Use TestEntityManager Methods
```java
// ✅ Good
entityManager.persistAndFlush(user);
entityManager.clear();

// ❌ Avoid
entityManager.persist(user);
entityManager.getEntityManager().flush();
```

### ✅ 4. Clear Persistence Context for Realistic Tests
```java
@Test
void test() {
    User user = createUser();
    entityManager.persistAndFlush(user);
    entityManager.clear(); // ← Force reload from DB

    Optional<User> found = repository.findById(user.getId());
    // Now this is a real DB query, not L1 cache hit
}
```

### ✅ 5. Use AssertJ for Fluent Assertions
```java
// ✅ Fluent and readable
assertThat(users)
    .hasSize(2)
    .allMatch(u -> u.getRole() == UserRole.ADMIN);

// ❌ Less readable
assertEquals(2, users.size());
assertTrue(users.stream().allMatch(u -> u.getRole() == UserRole.ADMIN));
```

### ✅ 6. Test Naming Convention
```java
// ✅ Clear naming
void testFindByEmail_WhenUserExists_ShouldReturnUser()
void testFindByEmail_WhenUserDoesNotExist_ShouldReturnEmpty()

// ❌ Unclear
void testFindByEmail()
void test1()
```

### ✅ 7. Use @Transactional for Auto-Rollback
```java
// Spring Boot 4.0: @DataJpaTest is already @Transactional
@DataJpaTest // ← Transactional by default
class RepositoryTest {
    // Tests auto-rollback
}

// For @SpringBootTest, add explicitly
@SpringBootTest
@Transactional // ← Add for rollback
class ServiceTest {
    // Tests auto-rollback
}
```

---

## 🚀 Running Tests in Docker

See [TESTING.md](TESTING.md) for complete Docker testing guide.

**Quick Command:**
```bash
docker-compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit
```

---

## 📚 Additional Resources

- [Spring Boot 4.0 Testing Documentation](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Mockito 5.20 Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [What's New in Spring Boot 4.0 Testing](https://rieckpil.de/whats-new-for-testing-in-spring-boot-4-0-and-spring-framework-7/)

---

**Last Updated:** 2025-01-13 (Spring Boot 4.0.0)
