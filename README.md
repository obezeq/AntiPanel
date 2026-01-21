# 🎯 AntiPanel - SMM Panel

**🌐 URL de Produccion:** https://antipanel.tech

Panel de marketing para redes sociales con enfoque en UX/UI minimalista y alto rendimiento.

**Tecnologías:** Spring Boot 4 + PostgreSQL 18 + Angular 21

---

## 🚀 Quick Start con Docker

```bash
# Clonar repositorio
git clone https://github.com/obezeq/AntiPanel/
cd AntiPanel

# Iniciar en modo desarrollo
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build

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
│   ├── src/                   # Código fuente Java
│   ├── docs/                  # Documentación backend
│   ├── sql/                   # Scripts SQL (init, examples)
│   └── Dockerfile             # Imagen Docker backend
├── frontend/                   # Angular 21
│   ├── src/                   # Código fuente Angular
│   ├── public/                # Assets estáticos (favicon)
│   └── docs/                  # Documentación frontend
│       ├── design/            # Fases 1-3 Diseño (CSS, Componentes)
│       └── client/            # Fases 1-7 Cliente (DOM, HTTP, Estado, Tests)
├── docs/                      # Documentación general
├── nginx/                     # Configuración Nginx
├── caddy/                     # Configuración Caddy (reverse proxy + SSL)
├── scripts/                   # Scripts de utilidad
├── docker-compose.yml         # Configuración Docker base
├── docker-compose.dev.yml     # Override para desarrollo
├── docker-dev.bat             # Helper script Windows
├── docker-dev.sh              # Helper script Linux/Mac
└── .env.example               # Variables de entorno ejemplo
```

---

## 🗄️ Backend (Spring Boot 4 + Java 25)

### Arquitectura
- ✅ **11 Entidades** con relaciones JPA
- ✅ **43 DTOs** con validación Jakarta
- ✅ **11 Repositories** con 150+ queries personalizadas
- ✅ **13 Controllers** (Auth, User, Orders, Admin...)
- ✅ **13 Services** con lógica de negocio
- ✅ **10 Mappers** (Entity ↔ DTO)
- ✅ **Seguridad:** JWT + Rate Limiting
- ✅ **44 Tests** (Controllers, Services, Mappers)
- ✅ **API Docs:** Swagger/OpenAPI
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

### Arquitectura
- ✅ **24 Componentes** reutilizables con BEM
- ✅ **Sistema ITCSS** (5 capas CSS organizadas)
- ✅ **Design Tokens** con CSS Custom Properties
- ✅ **22 Mixins SCSS** para responsive y utilidades
- ✅ **Sistema de Temas** Dark/Light mode preparado
- ✅ **Style Guide** en `/style-guide` con todos los componentes
- ✅ **Accesibilidad WCAG AA** (focus visible, ARIA, reduced motion)
- ✅ **9 Animaciones @keyframes** optimizadas
- ✅ **Desktop-First** con Mobile Responsive (5 breakpoints)

### Tecnologías
- Angular 21 (Standalone components, Signals, Control Flow)
- TypeScript 5.7
- SCSS con arquitectura ITCSS + BEM
- ng-icons (Material Icons + Iconoir + Simple Icons)
- Reactive Forms con ControlValueAccessor

### Componentes Principales
| Categoría | Componentes |
|-----------|-------------|
| Layout | Header (6 variantes), Footer, MainContent, Sidebar |
| Forms | FormInput, FormTextarea, FormSelect, AuthForm |
| UI | Button, Alert, Modal, Badge |
| Cards | ServiceCard, StatsCard, ServiceItemCard, OrderCard, RecentOrderCard |
| Orders | OrderInput, OrderReady, OrderPlaced, AdminOrderTable, UserOrderRow |
| Dashboard | DashboardHeader, DashboardSectionHeader |

**Ver documentación completa:** [📖 Frontend Design Docs](frontend/docs/design/DOCUMENTACION.md)

---

## 🐳 Docker

### Servicios Disponibles

| Servicio | Puerto | URL |
|----------|--------|-----|
| Frontend (Angular) | 4200 | http://localhost:4200 |
| Backend (Spring Boot) | 8080 | http://localhost:8080 |
| Swagger UI | 8080 | http://localhost:8080/swagger-ui.html |
| PostgreSQL | 5432 | localhost:5432 |
| pgAdmin (dev) | 5050 | http://localhost:5050 |
| Java Debug | 5005 | localhost:5005 |

### Comandos Principales

```bash
# Iniciar servicios
docker compose -f docker-compose.yml -f docker-compose.dev.yml up

# Ver logs
docker compose logs -f backend

# Detener servicios
docker compose down

# Limpiar todo
docker compose down -v

# Ejecutar tests
docker compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit
```

**Helper scripts:**
- Windows: `docker-dev.bat`
- Linux/Mac: `./docker-dev.sh`

---

## 📚 Documentación

### Frontend - Diseño (Fases 1-7)
- [📖 Documentación de Diseño](frontend/docs/design/DOCUMENTACION.md) - **Arquitectura CSS, Componentes, Accesibilidad**
  - Sección 1: Arquitectura CSS (ITCSS + BEM + Design Tokens)
  - Sección 2: HTML Semántico y Estructura
  - Sección 3: Sistema de Componentes UI (24 componentes)
  - Sección 4: Estrategia Responsive (Mobile-first)
  - Sección 5: Optimización Multimedia
  - Sección 6: Sistema de Temas (Dark/Light)
  - Sección 7: Informe de Accesibilidad (WCAG AA)
- [🎨 Justificación de Decisiones de Diseño](frontend/docs/design/justificacion_decisiones_de_diseno.md)

### Frontend - Cliente (Fases 1-7)
- [📖 Documentación de Cliente](frontend/docs/client/DOCUMENTACION.md) - **DOM, Eventos, Servicios, HTTP, Estado, Testing**
  - Fase 1: DOM y Eventos (Signals, Control Flow, Event Binding)
  - Fase 2: Servicios e Inyección de Dependencias (HttpClient, Interceptors)
  - Fase 3: Formularios Reactivos (FormGroup, FormArray, Validators)
  - Fase 4: Sistema de Rutas y Navegación (Guards, Resolvers, Lazy Loading)
  - Fase 5: Comunicación HTTP (Interceptores, CRUD, Retry Logic, Error Handling)
  - Fase 6: Gestión de Estado (Signals, computed, effect, Paginación, Debounce)
  - Fase 7: Testing y Calidad (Vitest, 79 tests, Docker, Build Producción)

### Backend (Spring Boot 4)
- [📖 Backend Documentation](backend/docs/README.md) - **Entry Point**
- [📦 Setup Guide](backend/docs/SETUP.md) - Java 25, Gradle, PostgreSQL
- [🧪 Testing Guide](backend/docs/TESTING.md) - Spring Boot 4.0 testing
- [🗄️ Data Model](backend/docs/modelo_de_datos.md) - Database schema
- [🔄 Spring Boot 4 Migration](backend/docs/SPRING_BOOT_4_MIGRATION.md) - Migration guide

### Docker
- [📋 Quick Start](DOCKER_QUICKSTART.md)
- [📖 Docker Guide](docs/DOCKER_GUIDE.md)
- [🅰️ Angular Docker Setup](docs/ANGULAR_DOCKER_SETUP.md) - Frontend container config

### General
- [🎯 Entender el Problema](frontend/docs/design/entender_el_problema_real.md)

---

## 🛠️ Desarrollo

### Requisitos
- Docker Desktop 20.10+
- (Opcional) Java 25 JDK para desarrollo local
- (Opcional) PostgreSQL 18 para desarrollo local
- (Opcional) Node.js 24+ para frontend

### Configuración Local

Ver [docs/DOCKER_GUIDE.md](docs/DOCKER_GUIDE.md) para instrucciones detalladas.

---

## 👨‍💻 Autor

**Ezequiel**
IES Rafael Alberti - 2º Desarrollo de Aplicaciones Web
