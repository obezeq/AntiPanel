# 🧪 Testing Guide - AntiPanel

Guía completa para ejecutar tests de Spring Boot en Docker.

---

## 🚀 Quick Start

```bash
# Ejecutar todos los tests
docker-compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit

# Ver reportes HTML
start backend/build/reports/tests/test/index.html  # Windows
```

**O usa el helper script:**
```bash
docker-test.bat  # Windows - Menú interactivo
```

---

## 📦 Archivos de Configuración

### Archivos Docker para Testing

```
AntiPanel/
├── docker-compose.test.yml          # Configuración de tests
├── docker-test.bat                  # Helper script (Windows)
└── backend/
    ├── Dockerfile                   # Incluye stage 'tester'
    └── src/
        ├── main/resources/
        │   └── application-test.yml # Perfil Spring Boot para tests
        └── test/java/               # Tests aquí
            └── com/antipanel/backend/
                ├── repository/      # Tests de repositorios
                ├── service/         # Tests de servicios (futuro)
                └── controller/      # Tests de controladores (futuro)
```

### Características del Entorno de Test

✅ **Base de datos PostgreSQL 18** aislada en puerto 5433
✅ **Esquema recreado** automáticamente para cada ejecución (create-drop)
✅ **Reportes HTML** exportados a `backend/build/reports/tests/test/`
✅ **Resultados JUnit XML** en `backend/build/test-results/test/`
✅ **Red Docker aislada** - No interfiere con desarrollo ni producción
✅ **Contenedores temporales** - Se destruyen al finalizar

---

## 🎯 Formas de Ejecutar Tests

### 1. Tests Completos con Docker Compose (Recomendado)

```bash
# Ejecutar todos los tests con PostgreSQL
docker-compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit

# Ver reportes generados
start backend/build/reports/tests/test/index.html
```

**Ventajas:**
- Entorno completo (backend + PostgreSQL)
- Base de datos real (no H2)
- Reportes automáticos
- Reproducible en CI/CD

### 2. Tests con Docker Build

```bash
# Build con stage 'tester'
cd backend
docker build --target tester -t antipanel-tests .

# Ver resultados
docker run --rm antipanel-tests sh -c "cat build/test-results/test/*.xml"
```

### 3. Tests Locales con DB Docker

```bash
# 1. Levantar solo PostgreSQL de test
docker-compose -f docker-compose.test.yml up -d postgres-test

# 2. Ejecutar tests localmente (requiere Java 25)
cd backend
./gradlew test

# 3. Ver reportes
start build/reports/tests/test/index.html

# 4. Detener DB
docker-compose -f docker-compose.test.yml down
```

### 4. Helper Script Interactivo (Windows)

```bash
# Ejecutar el script
docker-test.bat
```

**Opciones disponibles:**
1. Ejecutar TODOS los tests
2. Ejecutar tests con reportes detallados
3. Ejecutar tests de repositorios solamente
4. Ver reportes HTML de tests
5. Limpiar reportes antiguos
6. Ver logs de tests
7. Ejecutar tests en modo continuo (watch)

---

## 📝 Comandos Útiles

### Ejecutar Tests Específicos

```bash
# Tests de un repository específico
docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --tests UserRepositoryTest"

# Tests de un método específico
docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --tests UserRepositoryTest.testFindByEmail_WhenUserExists_ShouldReturnUser"

# Todos los repository tests
docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --tests *RepositoryTest"
```

### Tests con Coverage (JaCoCo)

```bash
# Ejecutar tests con coverage
docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test jacocoTestReport"

# Ver reporte de coverage
start backend/build/reports/jacoco/test/html/index.html
```

### Tests en Modo Continuo (Watch)

```bash
# Re-ejecuta tests automáticamente al detectar cambios
docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --continuous"
```

### Limpiar Reportes

```bash
# Windows
rmdir /s /q backend\build\reports
rmdir /s /q backend\build\test-results

# Linux/macOS
rm -rf backend/build/reports backend/build/test-results
```

---

## 🧩 Estructura de Tests

### 1. Repository Tests (@DataJpaTest)

**Ubicación:** `backend/src/test/java/com/antipanel/backend/repository/`

**Ejemplo:** [UserRepositoryTest.java](backend/src/test/java/com/antipanel/backend/repository/UserRepositoryTest.java)

```java
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
        User user = createTestUser("test@example.com", UserRole.USER);
        entityManager.persistAndFlush(user);
        entityManager.clear();

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    private User createTestUser(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed_password");
        user.setRole(role);
        user.setBalance(BigDecimal.ZERO);
        user.setIsActive(true);
        return user;
    }
}
```

**Características:**
- Usa PostgreSQL real (no H2 in-memory)
- TestEntityManager para setup de datos
- Limpia el persistence context con `.clear()` para tests realistas
- Naming convention: `test[MethodName]_When[Condition]_Should[ExpectedResult]`

### 2. Service Tests (@SpringBootTest)

**Ubicación:** `backend/src/test/java/com/antipanel/backend/service/` (futuro)

```java
@SpringBootTest
@Transactional  // Auto-rollback después de cada test
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateUser_WithValidData_ShouldReturnUserResponse() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .email("test@example.com")
            .password("securePassword123")
            .role(UserRole.USER)
            .build();

        // When
        UserResponse response = userService.createUser(request);

        // Then
        assertNotNull(response.getId());
        assertEquals("test@example.com", response.getEmail());
    }
}
```

### 3. Controller Tests (@WebMvcTest)

**Ubicación:** `backend/src/test/java/com/antipanel/backend/controller/` (futuro)

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testGetUser_WhenUserExists_ShouldReturnOk() throws Exception {
        // Given
        UserResponse mockUser = UserResponse.builder()
            .id(1)
            .email("test@example.com")
            .build();

        when(userService.getUserById(1)).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
```

---

## 🔧 Configuración del Entorno de Test

### application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres-test:5432/antipanel_test
    username: antipanel_test_user
    password: antipanel_test_password

  jpa:
    hibernate:
      ddl-auto: create-drop  # Recrear esquema para cada ejecución
    show-sql: true

logging:
  level:
    com.antipanel: DEBUG
    org.hibernate.SQL: DEBUG
```

### docker-compose.test.yml

```yaml
services:
  postgres-test:
    image: postgres:18-alpine
    environment:
      POSTGRES_DB: antipanel_test
      POSTGRES_USER: antipanel_test_user
      POSTGRES_PASSWORD: antipanel_test_password
    ports:
      - "5433:5432"  # Puerto diferente para no interferir
    tmpfs:
      - /var/lib/postgresql/data  # BD temporal en RAM

  backend-test:
    build:
      context: ./backend
      target: tester  # Stage de tests en Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: test
    volumes:
      - ./backend/build/reports:/app/build/reports
      - ./backend/build/test-results:/app/build/test-results
```

---

## 🐛 Troubleshooting

### Error: "Connection refused to postgres-test:5432"

```bash
# Verificar estado de PostgreSQL
docker-compose -f docker-compose.test.yml ps

# Ver logs
docker-compose -f docker-compose.test.yml logs postgres-test

# Verificar health
docker-compose -f docker-compose.test.yml exec postgres-test pg_isready -U antipanel_test_user
```

### Error: "Tests failed" sin detalles

```bash
# Ver reportes HTML completos
start backend/build/reports/tests/test/index.html

# Ver resultados JUnit XML
type backend\build\test-results\test\*.xml  # Windows
cat backend/build/test-results/test/*.xml   # Linux/macOS
```

### Tests muy lentos

**Optimizaciones:**
1. Reducir logging a WARN en `docker-compose.test.yml`
2. Usar `@Transactional` para auto-rollback
3. Preferir `@DataJpaTest` sobre `@SpringBootTest`
4. Usar `@DirtiesContext` solo cuando sea necesario

```yaml
# En docker-compose.test.yml
environment:
  LOGGING_LEVEL_ROOT: WARN  # En vez de INFO
  LOGGING_LEVEL_COM_ANTIPANEL: INFO  # En vez de DEBUG
```

### Reportes no se generan

```bash
# Verificar permisos del volumen
docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "ls -la /app/build"

# Ejecutar con verbose para ver errores
docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --info"
```

---

## 🚦 Integración con CI/CD

### GitHub Actions

```yaml
# .github/workflows/tests.yml
name: Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Run tests
        run: docker-compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit

      - name: Upload test reports
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-reports
          path: backend/build/reports/tests/test/

      - name: Upload test results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-results
          path: backend/build/test-results/test/
```

### GitLab CI

```yaml
# .gitlab-ci.yml
test:
  stage: test
  image: docker/compose:latest
  services:
    - docker:dind
  script:
    - docker-compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit
  artifacts:
    when: always
    paths:
      - backend/build/reports/tests/test/
      - backend/build/test-results/test/
    reports:
      junit: backend/build/test-results/test/*.xml
```

---

## 📊 Reportes de Tests

### Estructura de Reportes

```
backend/build/
├── reports/
│   └── tests/
│       └── test/
│           ├── index.html          # Reporte principal
│           ├── classes/            # Reportes por clase
│           ├── packages/           # Reportes por paquete
│           └── css/                # Estilos
└── test-results/
    └── test/
        ├── TEST-UserRepositoryTest.xml
        └── binary/
```

### Ver Reportes

```bash
# Windows
start backend\build\reports\tests\test\index.html

# macOS
open backend/build/reports/tests/test/index.html

# Linux
xdg-open backend/build/reports/tests/test/index.html
```

---

## 🎯 Mejores Prácticas

### Naming Conventions

```java
// Formato: test[MethodName]_When[Condition]_Should[ExpectedResult]
testFindByEmail_WhenUserExists_ShouldReturnUser()
testFindByEmail_WhenUserDoesNotExist_ShouldReturnEmpty()
testCreateUser_WithInvalidEmail_ShouldThrowException()
```

### Estructura AAA (Arrange-Act-Assert)

```java
@Test
void testExample() {
    // Given (Arrange) - Setup
    User user = createTestUser();

    // When (Act) - Ejecutar acción
    Optional<User> result = userRepository.findByEmail(user.getEmail());

    // Then (Assert) - Verificar resultado
    assertTrue(result.isPresent());
}
```

### Limpiar Persistence Context

```java
// Después de persist, flush y clear para tests realistas
entityManager.persistAndFlush(user);
entityManager.clear();  // Limpia el cache L1

// Ahora las queries son reales a BD
Optional<User> found = userRepository.findByEmail(email);
```

### Usar Builders para DTOs

```java
UserCreateRequest request = UserCreateRequest.builder()
    .email("test@example.com")
    .password("secure123")
    .role(UserRole.USER)
    .build();
```

---

## 📚 Recursos

- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Testcontainers](https://www.testcontainers.org/) (futuro)

---

**¡Happy Testing! 🧪**

Última actualización: 2025-01-13
