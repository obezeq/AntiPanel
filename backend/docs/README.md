# Backend Documentation

## 🚀 Quick Links

| Guide | Description |
|-------|-------------|
| [📦 Setup Guide](SETUP.md) | Java 25, Gradle, PostgreSQL, IntelliJ setup |
| [🧪 Testing Guide](TESTING.md) | Spring Boot 4.0 testing with Testcontainers |
| [🗄️ Data Model](modelo_de_datos.md) | Database schema and entities |

---

## Technology Stack

### Core Framework
- **Spring Boot:** 4.0.0
- **Java:** 25 (OpenJDK)
- **Build Tool:** Gradle 9.2.1

### Database
- **PostgreSQL:** 18
- **ORM:** Hibernate 7 (via Spring Data JPA)
- **Migrations:** Manual SQL scripts (see `sql/` directory)

### Testing
- **Framework:** JUnit 5 (Jupiter)
- **Database:** Testcontainers with PostgreSQL 18
- **Assertions:** AssertJ

### Dependencies
- **Lombok:** Code generation (@Data, @Builder, etc.)
- **Jakarta Validation:** DTO validation
- **Spring Security:** JWT authentication & authorization
- **Swagger/OpenAPI:** API documentation

---

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/antipanel/backend/
│   │   │   ├── entity/           # JPA Entities (11 files)
│   │   │   ├── enums/            # Enum types (7 files)
│   │   │   ├── dto/              # Data Transfer Objects (43 files)
│   │   │   ├── repository/       # Spring Data JPA Repositories (11 files)
│   │   │   ├── service/          # Business logic (13 services)
│   │   │   ├── controller/       # REST endpoints (13 controllers)
│   │   │   ├── mapper/           # Entity ↔ DTO mappers (10 files)
│   │   │   ├── security/         # JWT, filters, auth config
│   │   │   └── config/           # Application configuration
│   │   └── resources/
│   │       ├── application.yml                # Default config
│   │       ├── application-dev.yml            # Development profile
│   │       ├── application-test.yml           # Test profile
│   │       └── application-docker.yml         # Docker profile
│   └── test/
│       ├── java/com/antipanel/backend/
│       │   └── repository/       # Repository tests
│       └── resources/
│           └── application.yml   # Test configuration
├── docs/                         # Documentation (this folder)
├── sql/                          # Database scripts
├── build.gradle                  # Gradle build configuration
├── Dockerfile                    # Multi-stage Docker build
└── README.md                     # This file
```

---

## Architecture Overview

### Entities (11 Total)

| Entity | Description | Key Features |
|--------|-------------|--------------|
| `User` | System users | Email authentication, roles, balance |
| `Service` | SMM services catalog | Pricing, quality tiers, speed levels |
| `Provider` | External service providers | API integration, balance tracking |
| `Order` | Service orders | Status tracking, refills, profit calculation |
| `Invoice` | Payment deposits | Multi-processor support, fee calculation |
| `Transaction` | Financial ledger | Balance tracking, audit trail |
| `PaymentProcessor` | Payment gateways | Fee structure, JSONB config |
| `Category` | Service categories | Hierarchical organization |
| `ServiceType` | Service subcategories | Category grouping |
| `ProviderService` | Provider-service mapping | Cost tracking, sync status |
| `OrderRefill` | Order refills | Refill tracking for services |

### Enums (7 Total)

- `UserRole`: USER, ADMIN, SUPPORT
- `OrderStatus`: PENDING, PROCESSING, IN_PROGRESS, COMPLETED, PARTIAL, CANCELLED, REFUNDED
- `InvoiceStatus`: PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, EXPIRED
- `TransactionType`: DEPOSIT, ORDER, REFUND, ADJUSTMENT
- `ServiceQuality`: LOW, MEDIUM, HIGH, PREMIUM
- `ServiceSpeed`: SLOW, MEDIUM, FAST, INSTANT
- `RefillStatus`: PENDING, PROCESSING, COMPLETED, REJECTED, CANCELLED

### Repositories (11 Total, 150+ Queries)

Each repository extends `JpaRepository` and includes:
- Derived query methods (Spring Data auto-generated)
- Custom `@Query` JPQL queries
- Native SQL queries (for PostgreSQL-specific features)

**Example Repository:**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    List<User> findUsersRegisteredBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
```

### DTOs (43 Total)

Organized by entity:
- **Request DTOs:** For creating/updating entities
- **Response DTOs:** For API responses
- **Search DTOs:** For filtering and pagination

All DTOs include:
- Jakarta Validation annotations (`@NotNull`, `@Email`, etc.)
- Lombok annotations (`@Data`, `@Builder`, etc.)
- Custom validation logic where needed

---

## Database Schema

### Key Features

- **PostgreSQL 18** with modern features
- **ENUM types** for type-safe status fields
- **Constraints:** Foreign keys, unique indexes, check constraints
- **Precision:** `NUMERIC(12,4)` for monetary values
- **Audit fields:** `createdAt`, `updatedAt` on all entities
- **JSONB:** For flexible configuration storage

See [modelo_de_datos.md](modelo_de_datos.md) for complete schema documentation.

---

## Configuration Profiles

### Default (`application.yml`)
- Production settings
- Database: localhost:5432
- `ddl-auto: validate` (no schema changes)

### Development (`application-dev.yml`)
- DevTools enabled
- CORS permissive (localhost:4200, localhost:3000)
- Verbose SQL logging
- Actuator endpoints exposed

### Test (`application-test.yml`)
- Testcontainers PostgreSQL
- `ddl-auto: create-drop`
- Separate database on port 5433

### Docker (`application-docker.yml`)
- Container-optimized settings
- Environment variable configuration
- Health checks enabled

---

## Getting Started

### 1. Setup Environment
See [SETUP.md](SETUP.md) for:
- Java 25 installation
- JAVA_HOME configuration
- PostgreSQL setup
- IntelliJ IDEA configuration

### 2. Run Tests
See [TESTING.md](TESTING.md) for:
- Running tests locally
- Understanding Testcontainers
- Spring Boot 4.0 test changes
- CI/CD integration

### 3. Start Development
```bash
# Clone repository
git clone <repo-url>
cd AntiPanel/backend

# Run tests
./gradlew test

# Start application
./gradlew bootRun
```

---

## Development Workflow

### Local Development
1. Start PostgreSQL: `docker compose up postgres -d`
2. Run application: `./gradlew bootRun --args='--spring.profiles.active=dev'`
3. Access: http://localhost:8080

### Docker Development
1. Start services: `docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build`
2. View logs: `docker compose logs -f backend`
3. Access pgAdmin: http://localhost:5050
4. Access Swagger UI: http://localhost:8080/swagger-ui.html

### Testing
1. Run all tests: `./gradlew test`
2. Run specific test: `./gradlew test --tests UserRepositoryTest`
3. View report: `open build/reports/tests/test/index.html`

---

## Important Notes

### Spring Boot 4.0 Breaking Changes

**Testing packages relocated:**
- `@DataJpaTest`: `org.springframework.boot.data.jpa.test.autoconfigure`
- `TestEntityManager`: `org.springframework.boot.jpa.test.autoconfigure`
- `@AutoConfigureTestDatabase`: `org.springframework.boot.jdbc.test.autoconfigure`

See [TESTING.md](TESTING.md) for migration details.

### JPQL Syntax Fixes

Three repository methods were fixed for PostgreSQL compatibility:
1. `TransactionRepository.findFirstByUserIdOrderByCreatedAtDesc()` - Removed `LIMIT` clause
2. `TransactionRepository.getDailyTransactionSummary()` - Native query for `CAST(... AS DATE)`
3. `ProviderServiceRepository.findServicesNeedingSync()` - Native query for `NULLS FIRST`

### Database Type Safety

Enums are stored as `VARCHAR` in PostgreSQL using:
```java
@Enumerated(EnumType.STRING)
@JdbcTypeCode(SqlTypes.VARCHAR)
private UserRole role;
```

This ensures:
- Type-safe Java code
- Readable database values
- Easy data migrations

---

## Troubleshooting

Common issues and solutions:

| Problem | Solution |
|---------|----------|
| `JAVA_HOME is not set` | See [SETUP.md](SETUP.md) - Configure environment variables |
| Tests fail with Docker error | Ensure Docker Desktop is running |
| Port 8080 already in use | `lsof -i :8080` then `kill -9 <PID>` |
| Gradle build fails | `./gradlew clean build` |
| Database connection refused | Check PostgreSQL: `docker compose ps` |

---

## Next Steps

- [x] ~~Implement service layer~~ (13 services)
- [x] ~~Create REST controllers~~ (13 controllers)
- [x] ~~Add Spring Security~~ (JWT + Rate Limiting)
- [x] ~~Implement authentication/authorization~~
- [x] ~~Add API documentation~~ (Swagger/OpenAPI)
- [ ] Implement caching (Redis)
- [ ] Add monitoring (Actuator + Prometheus)

---

## Resources

- [Spring Boot 4.0 Documentation](https://docs.spring.io/spring-boot/docs/4.0.0/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [PostgreSQL 18 Documentation](https://www.postgresql.org/docs/18/)
- [Testcontainers](https://testcontainers.com/)
- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html)