# 🚀 Docker Quick Start - AntiPanel

## Inicio Rápido (3 pasos)

```bash
# 1. Navegar a la raíz del proyecto
cd ~\AntiPanel

# 2. Levantar servicios en modo desarrollo
docker compose -f docker compose.yml -f docker compose.dev.yml up --build

# 3. Acceder a la aplicación
# Backend:  http://localhost:8080
# Database: localhost:5432
# pgAdmin:  http://localhost:5050
```

## 🧪 Ejecutar Tests

```bash
# Ejecutar tests en Docker con base de datos aislada
docker compose -f docker compose.yml -f docker compose.test.yml up --build --abort-on-container-exit

# Ver reportes HTML generados
start backend/build/reports/tests/test/index.html
```

## 🎯 Servicios Disponibles

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Backend API** | http://localhost:8080 | - |
| **Health Check** | http://localhost:8080/actuator/health | - |
| **PostgreSQL** | localhost:5432 | User: `antipanel_user`<br>Pass: `antipanel_password` |
| **pgAdmin** | http://localhost:5050 | Email: `admin@antipanel.local`<br>Pass: `admin` |

## 📝 Comandos Útiles

```bash
# Ver logs
docker compose logs -f backend

# Reiniciar backend
docker compose restart backend

# Detener todo
docker compose down

# Eliminar todo (incluye datos de BD)
docker compose down -v
```

## 📚 Documentación Completa

Ver [docs/DOCKER_GUIDE.md](docs/DOCKER_GUIDE.md) para la guía completa.

---

**¿Problemas?** Revisa los logs: `docker compose logs -f`
