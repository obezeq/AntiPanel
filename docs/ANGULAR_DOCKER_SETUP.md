# Angular 21 + Bun - Docker Setup Guide

## Descripción

Este proyecto usa **Docker Puro** para el frontend Angular 21. No necesitas instalar Node.js, Angular CLI ni Bun localmente - Docker maneja todo.

## Tecnologías

- **Node.js:** 24 LTS (Krypton - soporte hasta Abril 2028)
- **Angular:** 21.0.5 (última versión estable)
- **Package Manager:** Bun 1.3.4 (5-10x más rápido que npm)
- **Runtime:** Docker

## Arquitectura

```
┌─────────────────────────────────────────┐
│        Docker Multi-Stage Build         │
├─────────────────────────────────────────┤
│ 1. Base (Node 24 LTS + Bun 1.3.4)      │
│ 2. Dependencies (bun install)           │
│ 3. Development (ng serve + hot reload)  │
│ 4. Builder (ng build --prod)            │
│ 5. Production (Nginx)                   │
└─────────────────────────────────────────┘
```

## Inicialización (Primera Vez)

### Paso 1: Ejecutar script de inicialización

```bash
# Desde el directorio raíz del proyecto
./init-frontend.sh
```

**¿Qué hace este script?**
1. Usa un contenedor temporal de Docker con Node.js 24 LTS
2. Instala Bun 1.3.4 y Angular CLI 21 en el contenedor
3. Crea el proyecto Angular con `ng new`
4. Configura `package.json` para Docker
5. Limpia el contenedor temporal

**Tiempo estimado:** 3-5 minutos

### Paso 2: Levantar los servicios

```bash
# Modo foreground (ver logs)
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build

# Modo background (detached)
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
```

### Paso 3: Acceder a la aplicación

- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **pgAdmin:** http://localhost:5050

## Desarrollo Diario

### Levantar servicios

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

### Detener servicios

```bash
# Graceful stop
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down

# Con limpieza de volúmenes
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down -v
```

### Ver logs

```bash
# Todos los servicios
docker-compose -f docker-compose.yml -f docker-compose.dev.yml logs -f

# Solo frontend
docker-compose -f docker-compose.yml -f docker-compose.dev.yml logs -f frontend

# Solo backend
docker-compose -f docker-compose.yml -f docker-compose.dev.yml logs -f backend
```

### Reconstruir solo frontend

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build frontend
```

### Entrar al contenedor

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend sh

# Dentro del contenedor puedes:
bun --version
node --version
ng version
ls -la
```

## Hot Reload

El hot reload está configurado y funciona automáticamente:

1. Edita archivos en `frontend/src/`
2. Los cambios se sincronizan con el contenedor (volúmenes Docker)
3. Angular dev server detecta cambios (polling habilitado)
4. El navegador se recarga automáticamente

**Archivos monitoreados:**
- `frontend/src/**/*`
- `frontend/public/**/*`
- `frontend/angular.json`
- `frontend/tsconfig*.json`

**Archivos NO sincronizados (protegidos):**
- `node_modules/` (se usa la versión del contenedor)
- `.angular/` (caché de Angular)

## Comandos Útiles

### Instalar nueva dependencia

```bash
# Opción 1: Desde el host (requiere editar package.json)
# 1. Editar frontend/package.json manualmente
# 2. Reconstruir contenedor:
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build frontend

# Opción 2: Desde dentro del contenedor
docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend bun add <package>
docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend bun add -D <package-dev>
```

### Generar componentes/servicios

```bash
# Entrar al contenedor
docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend sh

# Generar componente
ng generate component components/nombre

# Generar servicio
ng generate service services/nombre

# Generar guard
ng generate guard guards/nombre
```

### Ejecutar tests

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend bun test
```

### Ejecutar linter

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend bun run lint
```

## Troubleshooting

### Hot reload no funciona

**Síntomas:** Cambios no se reflejan en el navegador

**Solución:**
```bash
# 1. Verificar que polling está habilitado
docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend env | grep POLL

# Debe mostrar:
# WATCHPACK_POLLING=true
# CHOKIDAR_USEPOLLING=true

# 2. Verificar volúmenes montados
docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend ls -la /app/src

# 3. Reconstruir contenedor
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build frontend
```

### Error de permisos

**Síntomas:** `EACCES: permission denied`

**Solución:**
```bash
# Reconstruir sin caché
docker-compose -f docker-compose.yml -f docker-compose.dev.yml build --no-cache frontend
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d frontend
```

### Puerto 4200 en uso

**Síntomas:** `Error: listen EADDRINUSE: address already in use :::4200`

**Solución 1:** Cambiar puerto en `docker-compose.dev.yml`
```yaml
ports:
  - "4201:4200"  # Ahora accede por localhost:4201
```

**Solución 2:** Detener el proceso que usa el puerto
```bash
# Linux/Mac
lsof -ti:4200 | xargs kill -9

# O detener todos los contenedores
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down
```

### CORS errors

**Síntomas:** `Access to XMLHttpRequest at 'http://localhost:8080/api/...' from origin 'http://localhost:4200' has been blocked by CORS policy`

**Solución:** Verificar que el backend está configurado correctamente

1. Verificar que el backend está corriendo:
   ```bash
   docker-compose -f docker-compose.yml -f docker-compose.dev.yml logs backend | grep "Started"
   ```

2. Verificar CORS en `backend/src/main/resources/application-dev.yml`:
   ```yaml
   spring:
     web:
       cors:
         allowed-origins: http://localhost:4200,http://frontend:4200
   ```

3. Reiniciar backend:
   ```bash
   docker-compose -f docker-compose.yml -f docker-compose.dev.yml restart backend
   ```

## Mejores Prácticas Implementadas

### Docker Best Practices

1. **Multi-stage builds** - Optimización de capas y tamaño de imagen
2. **Alpine base images** - Imágenes mínimas (Node 24 Alpine ~55MB vs ~900MB)
3. **Layer caching** - Dependencias separadas del código fuente
4. **Non-root user** - Seguridad (futuro en producción)
5. **Healthchecks** - Monitoreo automático de contenedores
6. **.dockerignore** - Excluir archivos innecesarios del build
7. **Specific versions** - Node 24 LTS, Angular 21, Bun 1.3.4 (reproducibilidad)

### Angular + Bun Best Practices

1. **Bun 1.3.4 package manager** - 5-10x más rápido que npm
2. **Node.js 24 LTS (Krypton)** - Soporte a largo plazo hasta Abril 2028
3. **Angular 21.0.5** - Última versión estable
4. **Hot reload con polling** - Compatible con Docker volumes
5. **SCSS preprocessor** - Mejor que CSS puro
6. **Routing habilitado** - Aplicaciones SPA modernas

## Flujo de Trabajo Rápido

```bash
# 1. Inicializar proyecto
./init-frontend.sh

# 2. Levantar servicios
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build

# 3. Abrir http://localhost:4200
```

## Recursos

- [Angular 21 Documentation](https://angular.dev)
- [Bun Documentation](https://bun.sh/docs)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Node.js 24 LTS](https://nodejs.org/en/blog/release/v24.11.0)

---

**Última actualización:** 2025-12-15
