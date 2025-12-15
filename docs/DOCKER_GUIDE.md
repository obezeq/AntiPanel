# 🐳 Guía Docker - AntiPanel Backend

Guía completa para ejecutar AntiPanel en entorno de desarrollo con Docker.

---

## 📋 **Requisitos Previos**

Antes de comenzar, asegúrate de tener instalado:

- ✅ **Docker Desktop** (versión 20.10 o superior)
  - Windows: [Descargar Docker Desktop](https://www.docker.com/products/docker-desktop)
  - Verificar: `docker --version`

- ✅ **Docker Compose v2** (incluido en Docker Desktop)
  - Verificar: `docker compose version`

---

## 🚀 **Inicio Rápido (Quick Start)**

### **Opción 1: Desarrollo (Recomendado)**

```bash
# 1. Navegar a la raíz del proyecto
cd d:\ezequiel\INSTITUTO\AntiPanel

# 2. Construir y levantar todos los servicios
docker compose -f docker compose.yml -f docker compose.dev.yml up --build

# 3. Acceder a la aplicación
# Backend:  http://localhost:8080
# Database: localhost:5432
# pgAdmin:  http://localhost:5050
```

### **Opción 2: Producción (Básico)**

```bash
# Levantar solo backend + database (sin pgAdmin)
docker compose up --build
```

---

## 📦 **Estructura de Archivos Docker**

```
AntiPanel/
├── docker compose.yml          # Configuración base (producción)
├── docker compose.dev.yml      # Override para desarrollo
├── .env.example                # Variables de entorno de ejemplo
├── backend/
│   ├── Dockerfile              # Imagen del backend
│   ├── .dockerignore          # Archivos excluidos del contexto
│   └── sql/
│       ├── init.sql           # Script de inicialización de BD
│       └── example.sql        # Datos de ejemplo
└── docs/
    └── DOCKER_GUIDE.md        # Esta guía
```

---

## 🔧 **Comandos Principales**

### **Levantar Servicios**

```bash
# Desarrollo (con logs en consola)
docker compose -f docker compose.yml -f docker compose.dev.yml up

# Desarrollo (en segundo plano)
docker compose -f docker compose.yml -f docker compose.dev.yml up -d

# Producción básica
docker compose up -d
```

### **Reconstruir Imágenes**

```bash
# Reconstruir backend (después de cambios en código)
docker compose -f docker compose.yml -f docker compose.dev.yml up --build backend

# Reconstruir todo
docker compose -f docker compose.yml -f docker compose.dev.yml up --build
```

### **Detener Servicios**

```bash
# Detener servicios (mantiene datos)
docker compose down

# Detener y eliminar volúmenes (BORRA datos de BD)
docker compose down -v

# Detener servicios de desarrollo
docker compose -f docker compose.yml -f docker compose.dev.yml down
```

### **Ver Logs**

```bash
# Ver logs de todos los servicios
docker compose logs -f

# Ver logs solo del backend
docker compose logs -f backend

# Ver logs solo de la base de datos
docker compose logs -f postgres

# Ver últimas 100 líneas
docker compose logs --tail=100 backend
```

### **Reiniciar Servicios**

```bash
# Reiniciar backend
docker compose restart backend

# Reiniciar base de datos
docker compose restart postgres
```

### **Ejecutar Comandos en Contenedores**

```bash
# Acceder a shell del backend
docker exec -it antipanel-backend sh

# Acceder a PostgreSQL
docker exec -it antipanel-postgres psql -U antipanel_user -d antipanel

# Ver tablas en PostgreSQL
docker exec -it antipanel-postgres psql -U antipanel_user -d antipanel -c "\dt"
```

---

## 🗄️ **Gestión de Base de Datos**

### **Acceder a PostgreSQL desde el Host**

```bash
# Usando psql (si está instalado en tu máquina)
psql -h localhost -p 5432 -U antipanel_user -d antipanel

# Contraseña: antipanel_password
```

### **Usar pgAdmin (Development)**

1. Abrir navegador: `http://localhost:5050`
2. Login:
   - Email: `admin@antipanel.local`
   - Password: `admin`
3. Conectar a servidor:
   - Host: `postgres` (nombre del servicio en Docker)
   - Puerto: `5432`
   - Usuario: `antipanel_user`
   - Contraseña: `antipanel_password`

### **Ejecutar Scripts SQL**

```bash
# Ejecutar script SQL desde archivo
docker exec -i antipanel-postgres psql -U antipanel_user -d antipanel < backend/sql/init.sql

# Ejecutar comando SQL directo
docker exec -it antipanel-postgres psql -U antipanel_user -d antipanel -c "SELECT * FROM users;"
```

### **Backup de Base de Datos**

```bash
# Crear backup
docker exec antipanel-postgres pg_dump -U antipanel_user antipanel > backup.sql

# Restaurar backup
docker exec -i antipanel-postgres psql -U antipanel_user -d antipanel < backup.sql
```

---

## 🔍 **Verificación y Diagnóstico**

### **Health Checks**

```bash
# Ver estado de los contenedores
docker compose ps

# Ver health status
docker ps

# Verificar salud del backend
curl http://localhost:8080/actuator/health

# Verificar conexión a PostgreSQL
docker exec antipanel-postgres pg_isready -U antipanel_user -d antipanel
```

### **Inspeccionar Recursos**

```bash
# Ver uso de recursos
docker stats

# Ver volúmenes
docker volume ls

# Ver redes
docker network ls

# Inspeccionar red de AntiPanel
docker network inspect antipanel_antipanel-network
```

### **Logs de Errores Comunes**

```bash
# Error de conexión a BD
docker compose logs postgres | grep -i error

# Error en el backend
docker compose logs backend | grep -i error

# Ver logs de inicialización de BD
docker compose logs postgres | grep -i "database system is ready"
```

---

## 🛠️ **Desarrollo con Docker**

### **Hot Reload (Opcional)**

Para habilitar hot reload con Spring Boot DevTools:

1. Agregar dependencia en `build.gradle`:
```gradle
developmentOnly 'org.springframework.boot:spring-boot-devtools'
```

2. Descomentar volumen en `docker compose.dev.yml`:
```yaml
volumes:
  - ./backend/src:/app/src:ro
```

3. Reconstruir: `docker compose -f docker compose.yml -f docker compose.dev.yml up --build`

### **Debug Remoto (Puerto 5005)**

Configurar IDE para conectar a `localhost:5005`:

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. Add New → Remote JVM Debug
3. Host: `localhost`, Port: `5005`
4. Apply → Debug

**VS Code:**
```json
{
  "type": "java",
  "request": "attach",
  "name": "Attach to Docker",
  "hostName": "localhost",
  "port": 5005
}
```

### **Variables de Entorno**

Crear archivo `.env` desde `.env.example`:

```bash
# Copiar template
cp .env.example .env

# Editar valores
nano .env  # o usar tu editor favorito
```

Docker Compose cargará automáticamente el archivo `.env`.

---

## 🧪 **Testing con Docker**

### **Ejecutar Tests en Contenedor**

AntiPanel incluye configuración completa para ejecutar tests de Spring Boot dentro de Docker con una base de datos PostgreSQL aislada.

#### **Opción 1: Tests con Docker Compose (Recomendado)**

```bash
# Ejecutar todos los tests con base de datos de test
docker compose -f docker compose.yml -f docker compose.test.yml up --build --abort-on-container-exit

# Ver reportes de tests generados
# Los reportes HTML estarán en: backend/build/reports/tests/test/index.html
start backend/build/reports/tests/test/index.html  # Windows
open backend/build/reports/tests/test/index.html   # macOS
xdg-open backend/build/reports/tests/test/index.html  # Linux
```

**Características:**
- ✅ Base de datos PostgreSQL 18 aislada en puerto 5433
- ✅ Esquema recreado automáticamente (create-drop)
- ✅ Reportes de tests exportados a `backend/build/reports/`
- ✅ Resultados XML en `backend/build/test-results/`
- ✅ Red de test aislada (no afecta dev/producción)
- ✅ Contenedores temporales (se destruyen al finalizar)

#### **Opción 2: Tests con Docker Build**

```bash
# Ejecutar tests durante el build (stage 'tester')
cd backend
docker build --target tester -t antipanel-tests .

# Ver resultados
docker run --rm antipanel-tests sh -c "cat build/test-results/test/*.xml"
```

#### **Opción 3: Tests Locales con DB Docker**

```bash
# 1. Levantar solo base de datos de test
docker compose -f docker compose.test.yml up -d postgres-test

# 2. Ejecutar tests localmente (requiere Java 25 instalado)
cd backend
./gradlew test

# 3. Ver reportes
start build/reports/tests/test/index.html  # Windows

# 4. Detener base de datos de test
docker compose -f docker compose.test.yml down
```

### **Configuración de Tests**

#### **Perfil de Test: application-test.yml**
- ✅ Base de datos PostgreSQL en puerto 5433
- ✅ DDL auto: `create-drop` (esquema nuevo para cada ejecución)
- ✅ SQL logging habilitado para debugging
- ✅ Pool de conexiones reducido (5 conexiones máx)
- ✅ Actuator deshabilitado

#### **Estructura de Archivos de Test**

```
backend/
├── src/
│   └── test/
│       └── java/
│           └── com/antipanel/backend/
│               ├── repository/        # Tests de repositorios (@DataJpaTest)
│               ├── service/           # Tests de servicios (@SpringBootTest)
│               └── controller/        # Tests de controladores (@WebMvcTest)
├── build/
│   ├── reports/
│   │   └── tests/
│   │       └── test/
│   │           └── index.html        # Reporte HTML principal
│   └── test-results/
│       └── test/
│           └── *.xml                 # Resultados en formato JUnit XML
```

### **Tipos de Tests Recomendados**

#### **1. Repository Tests (@DataJpaTest)**
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertTrue(found.isPresent());
    }
}
```

#### **2. Service Tests (@SpringBootTest)**
```java
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("test@example.com");

        UserResponse response = userService.createUser(request);
        assertNotNull(response.getId());
    }
}
```

#### **3. Controller Tests (@WebMvcTest)**
```java
@WebMvcTest(UserController.class)
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

### **Comandos Útiles de Testing**

```bash
# Tests específicos
docker compose -f docker compose.test.yml run --rm backend-test sh -c "./gradlew test --tests UserRepositoryTest"

# Tests con coverage
docker compose -f docker compose.test.yml run --rm backend-test sh -c "./gradlew test jacocoTestReport"

# Tests en modo continuo (watch)
docker compose -f docker compose.test.yml run --rm backend-test sh -c "./gradlew test --continuous"

# Limpiar reportes anteriores
rm -rf backend/build/reports backend/build/test-results

# Ver logs de tests en tiempo real
docker compose -f docker compose.test.yml logs -f backend-test
```

### **Troubleshooting de Tests**

#### **Error: "Connection refused to postgres-test:5432"**
```bash
# Verificar que postgres-test esté healthy
docker compose -f docker compose.test.yml ps

# Ver logs de postgres-test
docker compose -f docker compose.test.yml logs postgres-test
```

#### **Error: "Tests failed" pero no ves los detalles**
```bash
# Ver reportes HTML completos
start backend/build/reports/tests/test/index.html

# Ver resultados XML
cat backend/build/test-results/test/*.xml
```

#### **Tests muy lentos**
- Reducir logging: cambiar `LOGGING_LEVEL_ROOT` a `WARN` en `docker compose.test.yml`
- Usar `@Transactional` en tests para rollback automático
- Evitar `@SpringBootTest` cuando `@DataJpaTest` o `@WebMvcTest` son suficientes

### **Integración con CI/CD**

#### **GitHub Actions**
```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run tests
        run: docker compose -f docker compose.yml -f docker compose.test.yml up --build --abort-on-container-exit
      - name: Upload test results
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: backend/build/reports/tests/test/
```

---

## 🧹 **Limpieza y Mantenimiento**

### **Limpiar Contenedores**

```bash
# Eliminar contenedores detenidos
docker compose down

# Eliminar contenedores e imágenes
docker compose down --rmi all

# Eliminar todo (contenedores, redes, volúmenes, imágenes)
docker compose down -v --rmi all
```

### **Limpiar Sistema Docker**

```bash
# Eliminar contenedores detenidos
docker container prune

# Eliminar imágenes sin usar
docker image prune

# Eliminar volúmenes sin usar
docker volume prune

# Limpieza completa (¡CUIDADO!)
docker system prune -a --volumes
```

### **Reconstruir desde Cero**

```bash
# 1. Detener y eliminar todo
docker compose -f docker compose.yml -f docker compose.dev.yml down -v --rmi all

# 2. Limpiar cache de Gradle (opcional)
cd backend
./gradlew clean

# 3. Reconstruir
cd ..
docker compose -f docker compose.yml -f docker compose.dev.yml up --build
```

---

## 📊 **Servicios y Puertos**

| Servicio | Puerto | URL | Credenciales | Estado |
|----------|--------|-----|--------------|--------|
| **Backend API (Spring Boot)** | 8080 | http://localhost:8080/api | - | ✅ Activo |
| **Frontend (Angular)** | 4200 | http://localhost:4200 | - | 🔜 Futuro |
| **PostgreSQL** | 5432 | localhost:5432 | User: `antipanel_user`<br>Pass: `antipanel_password` | ✅ Activo |
| **pgAdmin** (dev only) | 5050 | http://localhost:5050 | Email: `admin@antipanel.local`<br>Pass: `admin` | ✅ Activo |
| **Debug Port** (dev only) | 5005 | localhost:5005 | Para IDEs | ✅ Activo |

### **Arquitectura de Puertos (Diseño)**

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Network                       │
│                                                         │
│  ┌─────────────┐      ┌──────────────┐                │
│  │  Frontend   │─────▶│   Backend    │                │
│  │  (Angular)  │      │ (Spring Boot)│                │
│  │   Port 4200 │      │   Port 8080  │                │
│  └─────────────┘      └──────┬───────┘                │
│                              │                         │
│                              ▼                         │
│                       ┌──────────────┐                 │
│                       │  PostgreSQL  │                 │
│                       │   Port 5432  │                 │
│                       └──────────────┘                 │
│                                                         │
└─────────────────────────────────────────────────────────┘

Usuario ─▶ localhost:4200 (Frontend) ─▶ localhost:8080/api (Backend)
```

**Nota:** El frontend Angular aún no está implementado, pero la configuración ya está preparada.

---

## 🐛 **Troubleshooting**

### **Error: "Port already in use"**

```bash
# Ver qué está usando el puerto 8080
netstat -ano | findstr :8080   # Windows
lsof -i :8080                  # Linux/Mac

# Detener el proceso o cambiar puerto en docker compose.yml
ports:
  - "8081:8080"  # Mapear a otro puerto
```

### **Error: "Cannot connect to database"**

```bash
# Verificar que postgres esté healthy
docker compose ps

# Ver logs de postgres
docker compose logs postgres

# Verificar conexión
docker exec antipanel-postgres pg_isready -U antipanel_user -d antipanel
```

### **Error: "Gradle build failed"**

```bash
# Limpiar cache de Gradle
cd backend
./gradlew clean

# Reconstruir imagen
cd ..
docker compose build --no-cache backend
```

### **Backend no inicia después de cambios**

```bash
# 1. Detener servicios
docker compose down

# 2. Eliminar volúmenes (si hay problemas con datos)
docker compose down -v

# 3. Reconstruir imagen
docker compose up --build backend
```

### **Base de datos vacía después de reiniciar**

Los scripts SQL solo se ejecutan cuando el volumen es **nuevo**:

```bash
# Eliminar volumen y recrear
docker compose down -v
docker compose up
```

---

## ✅ **Checklist de Verificación**

Después de levantar los servicios, verifica:

- [ ] Backend está corriendo: `curl http://localhost:8080/actuator/health`
- [ ] PostgreSQL está healthy: `docker compose ps`
- [ ] Base de datos tiene tablas: `docker exec -it antipanel-postgres psql -U antipanel_user -d antipanel -c "\dt"`
- [ ] Datos de ejemplo cargados: `docker exec -it antipanel-postgres psql -U antipanel_user -d antipanel -c "SELECT COUNT(*) FROM users;"`
- [ ] pgAdmin accesible (dev): `http://localhost:5050`
- [ ] Logs sin errores críticos: `docker compose logs backend | grep ERROR`

---

## 📚 **Recursos Adicionales**

- [Documentación Docker](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot con Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)

---

## 🎯 **Mejores Prácticas Implementadas**

✅ **Multi-stage build** - Imagen final ligera (solo JRE)
✅ **Non-root user** - Seguridad mejorada
✅ **Health checks** - Monitoreo automático
✅ **Layer caching** - Builds más rápidos
✅ **Environment variables** - Configuración flexible
✅ **Persistent volumes** - Datos no se pierden
✅ **Network isolation** - Comunicación segura entre contenedores
✅ **.dockerignore** - Contexto de build optimizado

---

## 📝 **Notas Importantes**

- ⚠️ **Nunca uses `docker compose down -v` en producción** (elimina datos)
- ⚠️ Las credenciales en este ejemplo son para **desarrollo local únicamente**
- ⚠️ Cambia las contraseñas en producción
- ⚠️ Los logs de SQL en desarrollo pueden afectar el rendimiento

---

## 🆘 **Soporte**

Si encuentras problemas:

1. Revisar logs: `docker compose logs -f`
2. Verificar health: `docker compose ps`
3. Consultar esta guía
4. Buscar en documentación oficial de Docker/Spring Boot

---

**¡Listo para desarrollar! 🚀**

Última actualización: 2025-01-13
