# Backend Setup Guide

## Prerequisites

### Java 25 (OpenJDK)
The project requires Java 25 for Spring Boot 4.0 compatibility.

#### Installation via IntelliJ IDEA (Recommended)
IntelliJ automatically downloads JDKs:
1. Open IntelliJ IDEA
2. File → Project Structure → SDKs
3. Add (+) → Download JDK → Select "Oracle OpenJDK 25"
4. Default location: `~/.jdks/openjdk-25.0.1`

#### Manual Installation
Download from [jdk.java.net/25](https://jdk.java.net/25/)

### Configure JAVA_HOME for Terminal

**Linux/Mac:**
Add to `~/.bashrc` or `~/.zshrc`:
```bash
export JAVA_HOME=~/.jdks/openjdk-25.0.1
export PATH=$JAVA_HOME/bin:$PATH
```

Reload:
```bash
source ~/.bashrc
```

**Windows:**
Set environment variables:
```cmd
JAVA_HOME=C:\Users\YourUser\.jdks\openjdk-25.0.1
PATH=%JAVA_HOME%\bin;%PATH%
```

Verify:
```bash
java -version
# Output: openjdk version "25.0.1" 2025-10-21
```

---

## Gradle

The project uses Gradle 9.2.1 with Gradle Wrapper (no installation needed).

### Available Commands

```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Run application
./gradlew bootRun

# Clean build artifacts
./gradlew clean

# Check dependencies
./gradlew dependencies

# View all tasks
./gradlew tasks
```

---

## PostgreSQL

### Option 1: Docker (Recommended)
```bash
docker compose up postgres -d
```

### Option 2: Local Installation
- Install PostgreSQL 18
- Create database: `antipanel_dev`
- Create user: `antipanel_user` / `antipanel_password`

---

## Running the Application

### Development Mode (Local)
```bash
cd backend
./gradlew bootRun
```

Access at: http://localhost:8080

### With Docker
```bash
docker compose up backend --build
```

### With Docker (Development Profile)
```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up backend --build
```

---

## Configuration Profiles

The application supports multiple Spring profiles:

| Profile | File | Use Case |
|---------|------|----------|
| `default` | `application.yml` | Production settings |
| `dev` | `application-dev.yml` | Local development |
| `test` | `application-test.yml` | Test environment |
| `docker` | `application-docker.yml` | Docker containers |

### Activate Profile

**Via Gradle:**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Via Environment Variable:**
```bash
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

**Via IntelliJ:**
Run/Debug Configurations → Active profiles: `dev`

---

## Database Connection

### Development (Local PostgreSQL)
```yaml
# application-dev.yml
datasource:
  url: jdbc:postgresql://localhost:5432/antipanel_dev
  username: antipanel_user
  password: antipanel_password
```

### Docker
```yaml
# application-docker.yml
datasource:
  url: jdbc:postgresql://postgres:5432/antipanel
  username: ${POSTGRES_USER}
  password: ${POSTGRES_PASSWORD}
```

---

## IntelliJ IDEA Setup

### Import Project
1. File → Open
2. Select `AntiPanel/backend` directory
3. IntelliJ auto-detects Gradle project
4. Wait for Gradle sync to complete

### Configure JDK
1. File → Project Structure → Project
2. SDK: Select Java 25 (openjdk-25.0.1)
3. Language level: 25

### Run Configuration
1. Run → Edit Configurations
2. Add (+) → Spring Boot
3. Main class: `com.antipanel.backend.AntiPanelBackendApplication`
4. Active profiles: `dev`
5. Working directory: `$MODULE_WORKING_DIR$`

### Enable Lombok
1. File → Settings → Plugins
2. Install "Lombok" plugin
3. File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
4. Enable annotation processing ✓

---

## Troubleshooting

### "JAVA_HOME is not set"
```bash
export JAVA_HOME=~/.jdks/openjdk-25.0.1
export PATH=$JAVA_HOME/bin:$PATH
```

### "Could not find or load main class"
```bash
./gradlew clean build
```

### "Connection refused" to PostgreSQL
Check if PostgreSQL is running:
```bash
# Docker
docker compose ps

# Local
sudo systemctl status postgresql
```

### Port 8080 already in use
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

---

## Next Steps

- [Testing Guide](TESTING.md) - Run tests with Testcontainers
- [Development Guide](DEVELOPMENT.md) - Development workflow
- [Data Model](modelo_de_datos.md) - Database schema
