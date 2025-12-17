# 🐳 Guía Docker - AntiPanel

Guía completa para ejecutar AntiPanel en Docker - **Desarrollo** y **Producción**.

## 📚 **Índice de Contenidos**

- **Parte 1: DESARROLLO** (Esta sección) - Para codificar localmente
  - Inicio rápido de desarrollo
  - Comandos de desarrollo
  - Base de datos local
  - Testing
  - Debug y hot reload
  - Troubleshooting

- **Parte 2: PRODUCCIÓN** ([Ver al final](#-producción---despliegue-con-traefik--ssl)) - Para desplegar en servidor
  - Traefik + SSL automático
  - Nginx optimizado
  - Configuración de seguridad
  - Backup y monitoreo

---

## 📋 **Requisitos Previos**

Antes de comenzar, asegúrate de tener instalado Docker y Docker Compose:

### **🪟 Windows**

- ✅ **Docker Desktop para Windows** (versión 20.10 o superior)
  - [Descargar Docker Desktop para Windows](https://www.docker.com/products/docker-desktop)
  - Incluye Docker Compose v2 automáticamente
  - Requisitos: WSL 2 (Windows Subsystem for Linux 2)

### **🐧 Linux (Ubuntu/Debian)**

- ✅ **Docker Engine** (versión 20.10 o superior)
  ```bash
  # Instalar Docker
  curl -fsSL https://get.docker.com -o get-docker.sh
  sudo sh get-docker.sh

  # ⚠️ RECOMENDADO: Añadir tu usuario al grupo docker
  # (Sin esto tendrás que escribir 'sudo docker' en CADA comando)
  sudo usermod -aG docker $USER
  newgrp docker
  ```

- ✅ **Docker Compose v2** (versión 2.0 o superior)
  ```bash
  # Docker Compose ya viene incluido en Docker Engine v20.10+
  # Verifica con:
  docker compose version
  ```

### **🍎 macOS (Intel o Apple Silicon)**

**Opción 1: Homebrew (Recomendado para usuarios de terminal)**

```bash
# Instalar Docker (sin interfaz gráfica)
brew install docker

# Instalar Docker Compose
brew install docker-compose

# Necesitas un motor de contenedores. Elige uno:
# Opción A: Colima (ligero y simple)
brew install colima
colima start

# Opción B: OrbStack (más reciente y optimizado para M1/M2)
brew install orbstack

# Opción C: Docker Desktop (ver abajo)
```

**Opción 2: Docker Desktop para Mac (Interfaz gráfica oficial)**

- [Descargar Docker Desktop para Mac (Intel)](https://desktop.docker.com/mac/main/amd64/Docker.dmg)
- [Descargar Docker Desktop para Mac (Apple Silicon/M1/M2)](https://desktop.docker.com/mac/main/arm64/Docker.dmg)
- Incluye Docker Compose v2 automáticamente
- Requisitos: macOS 11 (Big Sur) o superior

**Nota técnica:** A diferencia de Linux, macOS no puede ejecutar contenedores Linux nativamente (el kernel es diferente). Por eso necesitas un motor de virtualización. Con Homebrew + Colima/OrbStack es similar a Linux pero con una VM ligera. Con Docker Desktop es más integrado pero ocupa más recursos.

---

### **✅ Verificar Instalación (Todos los SO)**

```bash
# Verificar Docker
docker --version
# Debería mostrar: Docker version 20.10 o superior

# Verificar Docker Compose
docker compose version
# Debería mostrar: Docker Compose version 2.0 o superior

# Verificar que Docker está corriendo
docker ps
# Si no hay error, ¡está todo bien!
```

---

## 🔄 **Entornos: Desarrollo vs Producción**

AntiPanel tiene **dos entornos Docker** completamente separados:

> **🚀 ¿Buscas instrucciones de PRODUCCIÓN?** [Salta directamente a la sección de Producción](#-producción---despliegue-con-traefik--ssl)

### **📌 Comparación Rápida**

| Característica | 💻 DESARROLLO | 🚀 PRODUCCIÓN |
|----------------|---------------|---------------|
| **Comando** | `docker compose -f docker-compose.yml -f docker-compose.dev.yml up` | `docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up` |
| **Puertos expuestos** | ✅ Backend: 8080<br>✅ DB: 5432<br>✅ pgAdmin: 5050<br>✅ Debug: 5005 | ❌ Solo 80 y 443<br>(Traefik maneja todo) |
| **HTTPS/SSL** | ❌ No necesario | ✅ Automático con Let's Encrypt |
| **Herramientas** | ✅ pgAdmin<br>✅ Debug remoto<br>✅ DevTools<br>✅ Logs detallados | ❌ Sin herramientas de desarrollo |
| **Datos de ejemplo** | ✅ Incluidos (example.sql) | ❌ Solo esquema (init.sql) |
| **Hot reload** | ✅ Spring DevTools<br>✅ Angular polling | ❌ Build estático |
| **Logging** | 🔊 DEBUG/INFO<br>SQL queries visibles | 🔇 WARN/ERROR<br>Mínimo logging |
| **Seguridad** | 🔓 Relajada<br>CORS permisivo | 🔒 Máxima<br>Security headers, rate limiting |
| **Optimización** | ⚡ Desarrollo rápido | 🚄 Performance máximo |
| **Reverse Proxy** | ❌ Acceso directo | ✅ Traefik + nginx |

### **💻 Cuándo usar DESARROLLO**

- ✅ Codificando nuevas features
- ✅ Debugging y troubleshooting
- ✅ Testing local
- ✅ Necesitas acceder a la base de datos directamente
- ✅ Quieres ver logs detallados

### **🚀 Cuándo usar PRODUCCIÓN**

- ✅ Desplegar en servidor público
- ✅ Testing de performance real
- ✅ Necesitas HTTPS con dominio
- ✅ Ambiente seguro y optimizado

---

## 🚀 **Inicio Rápido (Quick Start)**

### **💻 DESARROLLO (Recomendado para codificar)**

```bash
# 1. Navegar a la raíz del proyecto
cd /ruta/a/AntiPanel

# 2. Construir y levantar todos los servicios de desarrollo
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build

# 3. Acceder a la aplicación
# Backend:  http://localhost:8080
# Database: localhost:5432 (usuario: antipanel_user, pass: antipanel_password)
# pgAdmin:  http://localhost:5050 (email: admin@antipanel.local, pass: admin)
# Debug:    localhost:5005 (para conectar IDE)
```

**✅ Incluye:**
- Backend con Spring DevTools (hot reload)
- PostgreSQL con datos de ejemplo
- pgAdmin para gestionar la base de datos
- Puerto de debug remoto (5005)
- Logs detallados

---

### **🚀 PRODUCCIÓN (Para desplegar en servidor)**

```bash
# 1. Configurar variables de entorno
cp .env.prod.example .env.prod
nano .env.prod  # Editar con valores seguros

# 2. Levantar servicios de producción
docker compose -f docker-compose.yml -f docker-compose.prod.yml \
  --env-file .env.prod up -d --build

# 3. Verificar que todo funciona
curl https://antipanel.tech/api/actuator/health
```

**✅ Incluye:**
- Traefik con SSL automático (Let's Encrypt)
- Nginx optimizado para Angular
- Backend en modo producción
- PostgreSQL sin puerto expuesto
- Security headers y rate limiting

**📖 Para instrucciones completas de producción, ver [sección de Producción](#-producción---despliegue-con-traefik--ssl) al final de este documento.**

---

## 📦 **Estructura de Archivos Docker**

```
AntiPanel/
├── 💻 DESARROLLO
│   ├── docker-compose.yml          # ⚙️ Base (compartido)
│   ├── docker-compose.dev.yml      # 💻 Override DESARROLLO
│   └── docker-compose.test.yml     # 🧪 Tests
│
├── 🚀 PRODUCCIÓN
│   ├── docker-compose.yml          # ⚙️ Base (compartido)
│   ├── docker-compose.prod.yml     # 🚀 Override PRODUCCIÓN
│   ├── .env.prod.example           # 📝 Template variables producción
│   ├── traefik/
│   │   ├── traefik.yml            # Traefik config
│   │   └── dynamic/
│   │       └── middlewares.yml    # Security headers
│   └── nginx/
│       └── nginx.prod.conf        # Nginx para Angular + API
│
├── backend/
│   ├── Dockerfile                  # Multi-stage (dev + prod)
│   ├── .dockerignore
│   ├── src/main/resources/
│   │   ├── application-dev.yml     # 💻 Perfil desarrollo
│   │   ├── application-docker.yml  # 🐳 Perfil docker
│   │   ├── application-prod.yml    # 🚀 Perfil producción
│   │   └── application-test.yml    # 🧪 Perfil tests
│   └── sql/
│       ├── init.sql               # Script inicialización BD
│       └── example.sql            # Datos ejemplo (solo dev)
│
├── frontend/
│   ├── Dockerfile                  # Multi-stage (dev + prod)
│   └── .dockerignore
│
└── docs/
    └── DOCKER_GUIDE.md            # Esta guía
```

**Nota:** El mismo `docker-compose.yml` se usa como base para ambos entornos. Los archivos `.dev.yml` y `.prod.yml` aplican **overrides** específicos de cada entorno.

---

## 🔧 **Comandos Principales (DESARROLLO)**

**💡 Tip:** Todos estos comandos son para **entorno de desarrollo**. Para producción, ver la [sección de Producción](#-producción---despliegue-con-traefik--ssl).

### **Levantar Servicios de Desarrollo**

```bash
# Desarrollo (con logs en consola) - RECOMENDADO para ver errores
docker compose -f docker-compose.yml -f docker-compose.dev.yml up

# Desarrollo (en segundo plano) - Útil cuando no necesitas ver logs
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

### **Reconstruir Imágenes (después de cambios en código)**

```bash
# Reconstruir backend (después de cambios en Java)
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build backend

# Reconstruir todo (después de cambios importantes)
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

### **Detener Servicios de Desarrollo**

```bash
# Detener servicios (mantiene datos de BD)
docker compose -f docker-compose.yml -f docker-compose.dev.yml down

# Detener y eliminar volúmenes (⚠️ BORRA todos los datos de BD)
docker compose -f docker-compose.yml -f docker-compose.dev.yml down -v
```

### **Ver Logs de Desarrollo**

```bash
# Ver logs de todos los servicios (desarrollo)
docker compose -f docker-compose.yml -f docker-compose.dev.yml logs -f

# Ver logs solo del backend
docker compose -f docker-compose.yml -f docker-compose.dev.yml logs -f backend

# Ver logs solo de la base de datos
docker compose -f docker-compose.yml -f docker-compose.dev.yml logs -f postgres

# Ver últimas 100 líneas
docker compose -f docker-compose.yml -f docker-compose.dev.yml logs --tail=100 backend
```

### **Reiniciar Servicios de Desarrollo**

```bash
# Reiniciar backend
docker compose -f docker-compose.yml -f docker-compose.dev.yml restart backend

# Reiniciar base de datos
docker compose -f docker-compose.yml -f docker-compose.dev.yml restart postgres
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

## 🗄️ **Gestión de Base de Datos (DESARROLLO)**

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

## 🔍 **Verificación y Diagnóstico (DESARROLLO)**

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

## 🛠️ **Desarrollo con Docker (Hot Reload y Debug)**

### **Hot Reload (Opcional)**

Para habilitar hot reload con Spring Boot DevTools:

1. Agregar dependencia en `build.gradle`:
```gradle
developmentOnly 'org.springframework.boot:spring-boot-devtools'
```

2. Descomentar volumen en `docker-compose.dev.yml`:
```yaml
volumes:
  - ./backend/src:/app/src:ro
```

3. Reconstruir: `docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build`

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

## 🧪 **Testing con Docker (DESARROLLO)**

### **Ejecutar Tests en Contenedor**

AntiPanel incluye configuración completa para ejecutar tests de Spring Boot dentro de Docker con una base de datos PostgreSQL aislada.

#### **Opción 1: Tests con Docker Compose (Recomendado)**

```bash
# Ejecutar todos los tests con base de datos de test
docker compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit

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
docker compose -f docker-compose.test.yml up -d postgres-test

# 2. Ejecutar tests localmente (requiere Java 25 instalado)
cd backend
./gradlew test

# 3. Ver reportes
start build/reports/tests/test/index.html  # Windows

# 4. Detener base de datos de test
docker compose -f docker-compose.test.yml down
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
docker compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --tests UserRepositoryTest"

# Tests con coverage
docker compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test jacocoTestReport"

# Tests en modo continuo (watch)
docker compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --continuous"

# Limpiar reportes anteriores
rm -rf backend/build/reports backend/build/test-results

# Ver logs de tests en tiempo real
docker compose -f docker-compose.test.yml logs -f backend-test
```

### **Troubleshooting de Tests**

#### **Error: "Connection refused to postgres-test:5432"**
```bash
# Verificar que postgres-test esté healthy
docker compose -f docker-compose.test.yml ps

# Ver logs de postgres-test
docker compose -f docker-compose.test.yml logs postgres-test
```

#### **Error: "Tests failed" pero no ves los detalles**
```bash
# Ver reportes HTML completos
start backend/build/reports/tests/test/index.html

# Ver resultados XML
cat backend/build/test-results/test/*.xml
```

#### **Tests muy lentos**
- Reducir logging: cambiar `LOGGING_LEVEL_ROOT` a `WARN` en `docker-compose.test.yml`
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
        run: docker compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit
      - name: Upload test results
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: backend/build/reports/tests/test/
```

---

## 🧹 **Limpieza y Mantenimiento (DESARROLLO)**

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
docker compose -f docker-compose.yml -f docker-compose.dev.yml down -v --rmi all

# 2. Limpiar cache de Gradle (opcional)
cd backend
./gradlew clean

# 3. Reconstruir
cd ..
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

---

## 📊 **Servicios y Puertos (DESARROLLO)**

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

## 🐛 **Troubleshooting (DESARROLLO)**

### **Error: "Port already in use"**

```bash
# Ver qué está usando el puerto 8080
netstat -ano | findstr :8080   # Windows
lsof -i :8080                  # Linux/Mac

# Detener el proceso o cambiar puerto en docker-compose.yml
ports:
  - "8081:8080"  # Mapear a otro puerto
```

### **Error: "bind: address already in use" en PostgreSQL (puerto 5432)**

Este error ocurre frecuentemente después de reiniciar el PC, cuando tienes **PostgreSQL instalado localmente** además de Docker:

```
Error response from daemon: driver failed programming external connectivity on endpoint antipanel-postgres:
failed to bind port 0.0.0.0:5432/tcp: Error starting userland proxy: listen tcp4 0.0.0.0:5432: bind: address already in use
```

**Solución 1: Detener PostgreSQL local (Recomendado)**

```bash
# Linux (Ubuntu/Debian)
sudo systemctl stop postgresql
sudo systemctl disable postgresql   # Evita que arranque automáticamente

# Para volver a habilitarlo después:
sudo systemctl enable postgresql
sudo systemctl start postgresql
```

```powershell
# Windows (PowerShell como Administrador)
Stop-Service postgresql-x64-16    # Ajusta el número de versión (14, 15, 16...)
Set-Service postgresql-x64-16 -StartupType Disabled
```

```bash
# macOS (Homebrew)
brew services stop postgresql
brew services stop postgresql@16  # Si instalaste una versión específica
```

**Solución 2: Ver qué proceso usa el puerto 5432**

```bash
# Linux/Mac
sudo lsof -i :5432
# o
sudo ss -tlnp | grep 5432

# Windows (PowerShell)
Get-NetTCPConnection -LocalPort 5432 | Select-Object OwningProcess
Get-Process -Id <PID>
```

**Solución 3: Cambiar el puerto de PostgreSQL en Docker**

Si prefieres mantener PostgreSQL local activo, edita `docker-compose.dev.yml`:

```yaml
postgres:
  ports:
    - "5433:5432"  # Mapear a puerto 5433 en el host
```

Y actualiza tus herramientas (pgAdmin, DBeaver, etc.) para conectar al puerto `5433`.

**Solución 4: Matar el proceso manualmente (último recurso)**

```bash
# Linux/Mac - Matar proceso en puerto 5432
sudo fuser -k 5432/tcp

# O encontrar el PID y matarlo
sudo lsof -i :5432 -t | xargs sudo kill -9
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

---
---
---

# ═══════════════════════════════════════════════════════════════
# 🚀 PRODUCCIÓN - Despliegue con Traefik + SSL
# ═══════════════════════════════════════════════════════════════

**⚠️ ATENCIÓN:** Esta sección es para **PRODUCCIÓN en servidor público**.

Si estás **desarrollando localmente**, usa los comandos de la sección de **DESARROLLO** arriba.

---

Esta sección cubre el despliegue de AntiPanel en un entorno de producción con HTTPS automático usando Let's Encrypt.

---

## 📋 **Requisitos de Producción**

### **Servidor**
- ✅ **VPS/Cloud Server** con Docker instalado
- ✅ **Mínimo 2GB RAM** (recomendado 4GB)
- ✅ **Puertos 80 y 443** abiertos en firewall
- ✅ **Docker Compose v2** instalado

### **Dominio**
- ✅ Dominio registrado (ej: `antipanel.tech`)
- ✅ DNS configurado apuntando al servidor:
  - `antipanel.tech` → IP del servidor
  - `www.antipanel.tech` → IP del servidor

---

## 🏗️ **Arquitectura de Producción**

```
                          Internet
                             │
                             ▼
                   ┌─────────────────┐
                   │     Traefik     │ :80 / :443
                   │  (SSL + Proxy)  │
                   │   Let's Encrypt │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │    Frontend     │
                   │    (nginx)      │
                   │   :80 interno   │
                   │                 │
                   │  /api → backend │
                   │  /* → Angular   │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │    Backend      │
                   │  (Spring Boot)  │
                   │  :8080 interno  │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │   PostgreSQL    │
                   │  :5432 interno  │
                   └─────────────────┘

Routing:
- www.antipanel.tech → 301 redirect → antipanel.tech
- antipanel.tech/* → nginx → Angular SPA
- antipanel.tech/api/* → nginx proxy_pass → backend:8080
```

---

## 📁 **Estructura de Archivos de Producción**

```
AntiPanel/
├── docker-compose.yml          # Base (compartido)
├── docker-compose.prod.yml     # Override producción
├── .env.prod.example           # Template de variables
├── .env.prod                   # Variables reales (NO commitear)
│
├── traefik/
│   ├── traefik.yml             # Config estática de Traefik
│   └── dynamic/
│       └── middlewares.yml     # Middlewares (security headers, etc.)
│
├── nginx/
│   └── nginx.prod.conf         # Config nginx para SPA + API proxy
│
└── backend/src/main/resources/
    └── application-prod.yml    # Perfil Spring Boot producción
```

---

## 🚀 **Despliegue Inicial**

### **1. Clonar repositorio en el servidor**

```bash
git clone https://github.com/tu-usuario/AntiPanel.git
cd AntiPanel
```

### **2. Configurar variables de entorno**

```bash
# Copiar template
cp .env.prod.example .env.prod

# Editar con valores seguros
nano .env.prod
```

**Generar valores seguros:**

```bash
# Generar JWT_SECRET (64 bytes base64)
openssl rand -base64 64

# Generar POSTGRES_PASSWORD (32 caracteres)
openssl rand -base64 32
```

**Ejemplo de `.env.prod`:**

```bash
DOMAIN=antipanel.tech
POSTGRES_DB=antipanel_prod
POSTGRES_USER=antipanel_prod_user
POSTGRES_PASSWORD=tu_password_seguro_aqui
JWT_SECRET=tu_jwt_secret_generado_aqui
TZ=Europe/Madrid
```

### **3. Verificar configuración DNS**

```bash
# Verificar que el dominio apunta al servidor
dig antipanel.tech +short
dig www.antipanel.tech +short
# Debe mostrar la IP de tu servidor
```

### **4. Primer despliegue (staging SSL)**

Para evitar rate limits de Let's Encrypt durante pruebas, usar servidor staging primero:

```bash
# Editar traefik/traefik.yml y descomentar la línea de caServer staging
# caServer: https://acme-staging-v02.api.letsencrypt.org/directory

# Construir y levantar
docker compose -f docker-compose.yml -f docker-compose.prod.yml \
  --env-file .env.prod up -d --build

# Verificar que todo funciona
docker compose -f docker-compose.yml -f docker-compose.prod.yml ps
```

### **5. Cambiar a SSL de producción**

Una vez verificado que todo funciona:

```bash
# Detener servicios
docker compose -f docker-compose.yml -f docker-compose.prod.yml down

# Comentar la línea de staging en traefik/traefik.yml
# # caServer: https://acme-staging-v02.api.letsencrypt.org/directory

# Eliminar certificados de staging
docker volume rm antipanel_traefik_certs

# Reiniciar con certificados reales
docker compose -f docker-compose.yml -f docker-compose.prod.yml \
  --env-file .env.prod up -d --build
```

---

## 🔧 **Comandos de Producción**

### **Levantar servicios**

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml \
  --env-file .env.prod up -d
```

### **Ver logs**

```bash
# Todos los servicios
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs -f

# Servicio específico
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs -f traefik
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs -f frontend
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs -f backend
```

### **Reconstruir y actualizar**

```bash
# Pull cambios
git pull origin main

# Reconstruir (zero-downtime con health checks)
docker compose -f docker-compose.yml -f docker-compose.prod.yml \
  --env-file .env.prod up -d --build
```

### **Detener servicios**

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml down
```

### **Reiniciar un servicio**

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml restart backend
```

---

## ✅ **Verificación de Producción**

### **Verificar servicios**

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml ps
```

Todos deben mostrar `healthy`:

```
NAME                  STATUS                   PORTS
antipanel-traefik     Up (healthy)             0.0.0.0:80->80/tcp, 0.0.0.0:443->443/tcp
antipanel-frontend    Up (healthy)
antipanel-backend     Up (healthy)
antipanel-postgres    Up (healthy)
```

### **Verificar HTTPS**

```bash
# Frontend (debe retornar 200)
curl -I https://antipanel.tech

# Redirect www (debe retornar 301)
curl -I https://www.antipanel.tech

# API health check
curl https://antipanel.tech/api/actuator/health
```

### **Verificar certificado SSL**

```bash
# Ver información del certificado
openssl s_client -connect antipanel.tech:443 -servername antipanel.tech < /dev/null 2>/dev/null | openssl x509 -noout -dates
```

---

## 🔒 **Seguridad Implementada**

| Medida | Descripción |
|--------|-------------|
| **HTTPS forzado** | HTTP redirige automáticamente a HTTPS |
| **SSL/TLS Let's Encrypt** | Certificados automáticos y renovación |
| **HSTS** | Strict-Transport-Security con preload |
| **Security Headers** | X-Frame-Options, CSP, X-Content-Type-Options |
| **Rate Limiting** | 100 requests/minuto por IP |
| **DB no expuesta** | PostgreSQL solo accesible internamente |
| **Backend no expuesto** | Solo accesible vía nginx proxy |
| **Non-root containers** | Todos los contenedores como usuario no-root |
| **Resource limits** | Límites de CPU/RAM en todos los servicios |

---

## 🗄️ **Backup de Base de Datos**

### **Crear backup**

```bash
# Backup a archivo local
docker exec antipanel-postgres pg_dump -U antipanel_prod_user antipanel_prod > backup_$(date +%Y%m%d_%H%M%S).sql
```

### **Restaurar backup**

```bash
docker exec -i antipanel-postgres psql -U antipanel_prod_user -d antipanel_prod < backup.sql
```

### **Backup automático (cron)**

Añadir a crontab (`crontab -e`):

```bash
# Backup diario a las 3:00 AM
0 3 * * * cd /path/to/AntiPanel && docker exec antipanel-postgres pg_dump -U antipanel_prod_user antipanel_prod > /path/to/backups/backup_$(date +\%Y\%m\%d).sql
```

---

## 🔄 **Renovación de Certificados SSL**

Los certificados Let's Encrypt se renuevan **automáticamente** por Traefik.

**Verificar estado:**

```bash
# Ver logs de Traefik para renovaciones
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs traefik | grep -i "certificate"
```

---

## 📊 **Monitoreo**

### **Ver uso de recursos**

```bash
docker stats
```

### **Ver espacio en disco**

```bash
docker system df
```

### **Limpiar recursos no usados**

```bash
# Limpiar imágenes huérfanas
docker image prune -f

# Limpiar todo (con cuidado)
docker system prune -f
```

---

## 🐛 **Troubleshooting de Producción**

### **Certificado SSL no funciona**

```bash
# Verificar que el dominio apunta al servidor
dig antipanel.tech +short

# Ver logs de Traefik
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs traefik | grep -i error

# Verificar que puertos 80/443 están abiertos
sudo ufw status
sudo iptables -L -n
```

### **Error 502 Bad Gateway**

```bash
# Verificar que el backend está healthy
docker compose -f docker-compose.yml -f docker-compose.prod.yml ps

# Ver logs del backend
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs backend

# Verificar conectividad entre frontend y backend
docker exec antipanel-frontend curl -v http://backend:8080/actuator/health
```

### **La aplicación está lenta**

```bash
# Ver uso de recursos
docker stats

# Ver logs de PostgreSQL
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs postgres

# Verificar conexiones de base de datos
docker exec antipanel-postgres psql -U antipanel_prod_user -d antipanel_prod -c "SELECT count(*) FROM pg_stat_activity;"
```

### **Reinicio completo**

```bash
# Detener todo
docker compose -f docker-compose.yml -f docker-compose.prod.yml down

# Limpiar (SIN eliminar datos de DB)
docker compose -f docker-compose.yml -f docker-compose.prod.yml down --rmi local

# Reconstruir desde cero
docker compose -f docker-compose.yml -f docker-compose.prod.yml \
  --env-file .env.prod up -d --build
```

---

## 📋 **Checklist de Despliegue**

Antes de ir a producción, verifica:

- [ ] DNS configurado correctamente
- [ ] Variables de entorno en `.env.prod` con valores seguros
- [ ] Puertos 80 y 443 abiertos en firewall
- [ ] Certificado SSL funcionando
- [ ] Health checks pasando en todos los servicios
- [ ] Redirect www → non-www funcionando
- [ ] API accesible en `/api/*`
- [ ] Backup de base de datos configurado
- [ ] Logs sin errores críticos

---

## 🔗 **URLs de Producción**

| Servicio | URL |
|----------|-----|
| **Frontend (Angular)** | https://antipanel.tech |
| **API Backend** | https://antipanel.tech/api |
| **Health Check** | https://antipanel.tech/api/actuator/health |
| **Swagger UI** | https://antipanel.tech/api/swagger-ui.html |

---

**Última actualización: 2025-01-13**
