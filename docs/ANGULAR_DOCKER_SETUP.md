# 🅰️ Guía: Agregar Angular al Docker Setup

**NOTA:** Esta guía es para el **futuro** cuando estés listo para dockerizar el frontend Angular.

---

## 📋 **Pre-requisitos**

Antes de continuar, asegúrate de tener:

- ✅ Proyecto Angular 21 creado en `frontend/`
- ✅ Backend funcionando en Docker
- ✅ Docker Desktop instalado y corriendo

---

## 🚀 **Paso 1: Crear Dockerfile para Angular**

Crea el archivo `frontend/Dockerfile`:

```dockerfile
# ========================================
# Multi-stage Dockerfile for Angular 21
# ========================================

# ========================================
# Stage 1: Build Stage
# ========================================
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY . .

# Build Angular app for production
RUN npm run build -- --configuration production

# ========================================
# Stage 2: Development Stage (for ng serve)
# ========================================
FROM node:20-alpine AS development

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install ALL dependencies (including dev)
RUN npm install

# Copy source code
COPY . .

# Expose Angular dev server port
EXPOSE 4200

# Start development server
CMD ["npm", "run", "start", "--", "--host", "0.0.0.0", "--poll", "2000"]

# ========================================
# Stage 3: Production Stage (nginx)
# ========================================
FROM nginx:alpine AS production

# Copy built app from builder stage
COPY --from=builder /app/dist/antipanel-frontend /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose nginx port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
```

---

## 🚀 **Paso 2: Crear .dockerignore para Angular**

Crea el archivo `frontend/.dockerignore`:

```
# Dependencies
node_modules
npm-debug.log*

# Build output
dist
.angular

# IDE
.vscode
.idea

# OS
.DS_Store
Thumbs.db

# Environment
.env
.env.local

# Testing
coverage
.nyc_output

# Misc
*.log
```

---

## 🚀 **Paso 3: Crear nginx.conf**

Crea el archivo `frontend/nginx.conf`:

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Enable gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    # SPA fallback - redirect all requests to index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy (opcional - si quieres que nginx maneje el proxy al backend)
    location /api {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    # Cache static assets
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

---

## 🚀 **Paso 4: Descomentar servicio frontend en docker-compose**

En `docker-compose.yml` y `docker-compose.dev.yml`, descomenta las secciones marcadas:

```yaml
# ========================================
# Angular Frontend (Future - Not implemented yet)
# ========================================
# QUITAR COMENTARIOS DE ESTAS LÍNEAS ↓↓↓
```

---

## 🚀 **Paso 5: Configurar environment de Angular**

Actualiza `frontend/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

Y `frontend/src/environments/environment.docker.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://backend:8080/api'  // Usa nombre del servicio Docker
};
```

---

## 🚀 **Paso 6: Actualizar package.json**

Añade scripts útiles en `frontend/package.json`:

```json
{
  "scripts": {
    "start": "ng serve",
    "start:docker": "ng serve --host 0.0.0.0 --poll 2000",
    "build": "ng build",
    "build:prod": "ng build --configuration production",
    "build:docker": "ng build --configuration docker"
  }
}
```

---

## 🚀 **Paso 7: Levantar todo junto**

```bash
# Reconstruir todo
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build

# Acceder:
# Frontend: http://localhost:4200
# Backend:  http://localhost:8080
```

---

## 📊 **Arquitectura Final**

```
┌──────────────────────────────────────────────────┐
│              Navegador del Usuario               │
└──────────────────┬───────────────────────────────┘
                   │
                   ▼
         http://localhost:4200
                   │
┌──────────────────┴───────────────────────────────┐
│              Docker Network                      │
│                                                  │
│  ┌─────────────────┐                            │
│  │    Frontend     │                            │
│  │    (Angular)    │                            │
│  │   Container     │                            │
│  │   Port: 4200    │                            │
│  └────────┬────────┘                            │
│           │                                      │
│           │ HTTP Requests                        │
│           │ /api/*                               │
│           ▼                                      │
│  ┌─────────────────┐       ┌─────────────────┐ │
│  │     Backend     │       │   PostgreSQL    │ │
│  │  (Spring Boot)  │──────▶│   Database      │ │
│  │   Container     │       │   Container     │ │
│  │   Port: 8080    │       │   Port: 5432    │ │
│  └─────────────────┘       └─────────────────┘ │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 🔧 **Comandos Útiles**

```bash
# Ver logs del frontend
docker-compose logs -f frontend

# Reiniciar solo frontend
docker-compose restart frontend

# Acceder al contenedor del frontend
docker exec -it antipanel-frontend sh

# Reconstruir solo frontend
docker-compose up --build frontend
```

---

## ⚡ **Hot Reload (Desarrollo)**

El hot reload ya está configurado en `docker-compose.dev.yml`:

```yaml
volumes:
  - ./frontend/src:/app/src
  - /app/node_modules  # Prevent overwriting
```

Cualquier cambio en `frontend/src` se reflejará automáticamente.

---

## 🎯 **Siguiente Paso**

Cuando implementes Angular:

1. Crea el proyecto Angular en `frontend/`
2. Crea los archivos mencionados arriba
3. Descomenta las secciones frontend en docker-compose
4. Ejecuta: `docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build`

---

## 📚 **Recursos**

- [Angular Docker Guide](https://angular.io/guide/deployment#docker)
- [Multi-stage Builds](https://docs.docker.com/build/building/multi-stage/)
- [Nginx Configuration](https://nginx.org/en/docs/)

---

**Última actualización:** 2025-01-13
