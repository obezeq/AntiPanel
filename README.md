# 🎯 AntiPanel - SMM Panel

Panel de marketing para redes sociales con enfoque en UX/UI minimalista y alto rendimiento.

**Tecnologías:** Spring Boot 4 + PostgreSQL 18 + Angular 21

---

## 🚀 Quick Start con Docker

```bash
# Clonar repositorio
git clone <repo-url>
cd AntiPanel

# Iniciar en modo desarrollo
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build

# Acceder a los servicios
# Backend:  http://localhost:8080
# pgAdmin:  http://localhost:5050
```

**Ver guía completa:** [📚 DOCKER_GUIDE.md](docs/DOCKER_GUIDE.md)

---

## 📦 Estructura del Proyecto

```
AntiPanel/
├── backend/                    # Spring Boot 4 + Java 25
│   ├── src/
│   │   ├── entity/            # Entidades JPA (11)
│   │   ├── dto/               # DTOs (43 archivos)
│   │   ├── repository/        # Repositories (11 con 150+ queries)
│   │   └── resources/         # Configuración
│   ├── docs/                  # Documentación
│   ├── sql/                   # Scripts de base de datos
│   └── Dockerfile             # Imagen Docker del backend
├── frontend/                   # Angular 21 (próximamente)
├── docs/                      # Documentación del proyecto
├── docker-compose.yml         # Configuración Docker base
├── docker-compose.dev.yml     # Override para desarrollo
└── .env.example               # Variables de entorno ejemplo
```

---

## 🗄️ Backend (Spring Boot 4 + Java 25)

### Arquitectura
- ✅ **11 Entidades** con relaciones JPA
- ✅ **43 DTOs** con validación Jakarta
- ✅ **11 Repositories** con 150+ queries personalizadas
- ✅ **PostgreSQL 18** con tipos ENUM y constraints
- ✅ **Docker** multi-stage con mejores prácticas

### Tecnologías
- Spring Boot 4.0.0
- Java 25
- PostgreSQL 18
- Lombok
- Jakarta Validation
- Gradle 9.2.1

**Ver documentación completa:** [Backend Docs](/backend/docs/README.md)

---

## 🎨 Frontend (Angular 21)

*Próximamente...*

---

## 🐳 Docker

### Servicios Disponibles

| Servicio | Puerto | URL |
|----------|--------|-----|
| Backend (Spring Boot) | 8080 | http://localhost:8080 |
| PostgreSQL | 5432 | localhost:5432 |
| pgAdmin (dev) | 5050 | http://localhost:5050 |

### Comandos Principales

```bash
# Iniciar servicios
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up

# Ver logs
docker-compose logs -f backend

# Detener servicios
docker-compose down

# Limpiar todo
docker-compose down -v

# Ejecutar tests
docker-compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit
```

**Helper script para Windows:** `docker-dev.bat`

---

## 📚 Documentación

### Backend (Spring Boot 4)
- [📖 Backend Documentation](backend/docs/README.md) - **Entry Point**
- [📦 Setup Guide](backend/docs/SETUP.md) - Java 25, Gradle, PostgreSQL
- [🧪 Testing Guide](backend/docs/TESTING.md) - Spring Boot 4.0 testing
- [🗄️ Data Model](backend/docs/modelo_de_datos.md) - Database schema

### Docker
- [📋 Quick Start](DOCKER_QUICKSTART.md)
- [📖 Docker Guide](docs/DOCKER_GUIDE.md)

### General
- [🎯 Entender el Problema](entender_el_problema_real.md)

---

## 🛠️ Desarrollo

### Requisitos
- Docker Desktop 20.10+
- (Opcional) Java 25 JDK para desarrollo local
- (Opcional) PostgreSQL 18 para desarrollo local
- (Opcional) Node.js 20+ para frontend

### Configuración Local

Ver [docs/DOCKER_GUIDE.md](docs/DOCKER_GUIDE.md) para instrucciones detalladas.

---

## 👨‍💻 Autor

**Ezequiel**
Instituto - 2º Desarrollo de Aplicaciones Web
