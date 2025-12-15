# Testing Guide - Spring Boot 4.0

## Overview

This project uses **Spring Boot 4.0** with modern testing practices:
- ✅ **Real PostgreSQL 18** via Testcontainers (no H2 in-memory database)
- ✅ **@DataJpaTest** for fast repository testing
- ✅ **@ServiceConnection** for automatic DataSource configuration
- ✅ **JUnit 5** (Jupiter) as testing framework
- ✅ **Docker** required for running tests (Testcontainers)

---

## Quick Start

### Run All Tests
```bash
cd backend
./gradlew test
```

### Run with Clean Build
```bash
./gradlew clean test
```

### Run Tests via Docker
```bash
cd ..  # Go to project root
docker compose -f docker-compose.test.yml up backend-test --build --abort-on-container-exit
```

---

## Spring Boot 4.0 Testing Changes

### Package Relocations

Spring Boot 4.0 modularized the test infrastructure. **Import packages changed:**

| Annotation | Spring Boot 3.x | Spring Boot 4.0 |
|-----------|-----------------|-----------------|
| `@DataJpaTest` | `org.springframework.boot.test.autoconfigure.orm.jpa` | `org.springframework.boot.data.jpa.test.autoconfigure` |
| `TestEntityManager` | `org.springframework.boot.test.autoconfigure.orm.jpa` | `org.springframework.boot.jpa.test.autoconfigure` |
| `@AutoConfigureTestDatabase` | `org.springframework.boot.test.autoconfigure.jdbc` | `org.springframework.boot.jdbc.test.autoconfigure` |

### Required Test Dependencies

```gradle
// Modular test starters (Spring Boot 4.0)
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
testImplementation 'org.springframework.boot:spring-boot-starter-jdbc'

// Testcontainers
testImplementation 'org.springframework.boot:spring-boot-testcontainers'
testImplementation 'org.testcontainers:postgresql'
testImplementation 'org.testcontainers:junit-jupiter'

// Testcontainers BOM for version management
dependencyManagement {
    imports {
        mavenBom 'org.testcontainers:testcontainers-bom:1.20.4'
    }
}
```

---

## Test Structure

### Repository Test Example

```java
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest  // Spring Boot 4.0: org.springframework.boot.data.jpa.test.autoconfigure
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    @ServiceConnection  // Auto-configures DataSource from container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testSaveUser_ShouldPersistUserToDatabase() {
        // Given
        User user = User.builder()
            .email("test@example.com")
            .password("password123")
            .role(UserRole.USER)
            .build();

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }
}
```

### Key Annotations Explained

#### `@DataJpaTest`
- **Purpose:** Sliced test that loads only JPA components
- **Benefits:** Faster than `@SpringBootTest` (doesn't load full context)
- **Auto-configuration:** Configures Hibernate, EntityManager, TestEntityManager
- **Transactions:** Automatically rolls back after each test

#### `@Testcontainers`
- **Purpose:** JUnit 5 extension for managing container lifecycle
- **Lifecycle:** Starts containers before tests, stops after all tests complete
- **Cleanup:** Automatically removes containers via Ryuk

#### `@AutoConfigureTestDatabase(replace = NONE)`
- **Purpose:** Prevents Spring from replacing with embedded H2 database
- **Effect:** Forces use of Testcontainers PostgreSQL
- **Best Practice:** Always use for production parity

#### `@Container`
- **Purpose:** Marks PostgreSQLContainer for lifecycle management
- **Static:** Reuses same container across all tests in class (faster)
- **Non-static:** Creates new container per test method (slower, isolated)

#### `@ServiceConnection` (Spring Boot 4.0+)
- **Purpose:** Auto-configures DataSource from Testcontainers
- **Replaces:** `@DynamicPropertySource` from Spring Boot 3.x
- **Benefit:** Zero manual configuration needed

---

## Test Results

### Current Test Coverage

**Total Tests:** 10 ✅
- `AntiPanelBackendApplicationTests`: 1 test (context loads)
- `UserRepositoryTest`: 9 repository tests

### Test Reports

After running tests, view HTML report:
```bash
open build/reports/tests/test/index.html
```

Or view XML results:
```bash
cat build/test-results/test/*.xml
```

---

## Testcontainers Details

### How It Works

1. **Detects Docker:** Testcontainers finds local Docker daemon
2. **Pulls Image:** Downloads `postgres:18-alpine` if not cached
3. **Starts Container:** Creates ephemeral PostgreSQL instance on random port
4. **Configures DataSource:** `@ServiceConnection` auto-wires connection
5. **Runs Tests:** Executes all test methods
6. **Cleanup:** Stops and removes container automatically

### Container Configuration

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:18-alpine")
        .withDatabaseName("testdb")          // Optional: custom DB name
        .withUsername("testuser")            // Optional: custom user
        .withPassword("testpass");           // Optional: custom password
```

**Note:** With `@ServiceConnection`, Spring Boot auto-configures from container defaults.

### Requirements

- ✅ **Docker** must be running
- ✅ **Docker socket** accessible at `/var/run/docker.sock` (Linux/Mac) or named pipe (Windows)
- ✅ **Network access** to pull images from Docker Hub

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Backend Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 25
      uses: actions/setup-java@v4
      with:
        java-version: '25'
        distribution: 'temurin'

    - name: Run tests
      run: |
        cd backend
        ./gradlew test

    - name: Publish Test Report
      uses: mikepenz/action-junit-report@v4
      if: always()
      with:
        report_paths: '**/build/test-results/test/TEST-*.xml'
```

### GitLab CI Example

```yaml
test:
  image: eclipse-temurin:25-jdk
  services:
    - docker:dind
  script:
    - cd backend
    - ./gradlew test
  artifacts:
    reports:
      junit: backend/build/test-results/test/TEST-*.xml
```

---

## Troubleshooting

### Docker not found
```
Error: Could not find a valid Docker environment
```

**Solution:** Ensure Docker Desktop is running:
```bash
docker ps  # Should list containers
```

### Port conflicts
```
Error: Bind for 0.0.0.0:XXXXX failed: port is already allocated
```

**Solution:** Testcontainers uses random ports automatically. Ensure no containers are stuck:
```bash
docker ps -a
docker rm -f $(docker ps -aq)  # Remove all containers
```

### Image pull failure
```
Error: Failed to pull image postgres:18-alpine
```

**Solution:** Check network/proxy settings:
```bash
docker pull postgres:18-alpine
```

### Tests hang indefinitely
```
Tests run for > 5 minutes
```

**Solution:** Check Gradle daemon:
```bash
./gradlew --stop
./gradlew test --no-daemon
```

### JAVA_HOME not set
```
Error: JAVA_HOME is not set
```

**Solution:** Configure Java 25:
```bash
export JAVA_HOME=~/.jdks/openjdk-25.0.1
export PATH=$JAVA_HOME/bin:$PATH
```

---

## Performance Tips

### Reuse Containers (Faster)
Use `static` containers (already implemented):
```java
@Container
static PostgreSQLContainer<?> postgres = ...  // ✅ Reused across tests
```

vs

```java
@Container
PostgreSQLContainer<?> postgres = ...  // ❌ New container per test
```

### Parallel Test Execution
Enable in `build.gradle`:
```gradle
test {
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
}
```

### Skip Tests During Build
```bash
./gradlew build -x test
```

---

## Best Practices

### ✅ DO
- Use real PostgreSQL via Testcontainers
- Use `@DataJpaTest` for repository tests
- Use `static` containers for speed
- Clear EntityManager between assertions: `entityManager.clear()`
- Use AssertJ for fluent assertions
- Follow AAA pattern (Arrange-Act-Assert)

### ❌ DON'T
- Don't use H2 in-memory database
- Don't use `@SpringBootTest` for simple repository tests
- Don't create non-static containers unless needed
- Don't skip `@AutoConfigureTestDatabase(replace = NONE)`
- Don't hardcode database properties (use `@ServiceConnection`)

---

## References

- [Spring Boot 4.0 Testing Documentation](https://docs.spring.io/spring-boot/reference/testing/)
- [Testcontainers Spring Boot Guide](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [@DataJpaTest API (SB 4.0)](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/data/jpa/test/autoconfigure/DataJpaTest.html)
- [@ServiceConnection API](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/testcontainers/service/connection/ServiceConnection.html)
