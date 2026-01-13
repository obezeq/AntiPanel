# Changelog

Todas las modificaciones relevantes de este proyecto estan documentadas en este archivo.

El formato esta basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

---

## [1.0.0] - 2026-01-13

### Agregado
- Sistema completo de rutas con 13 rutas principales y child routes
- Lazy loading en todas las rutas de funcionalidad
- 4 guards funcionales: `authGuard`, `guestGuard`, `rootGuard`, `pendingChangesGuard`
- Resolver `orderResolver` para precarga de datos
- Sistema de breadcrumbs dinamicos con `BreadcrumbService`
- 3 interceptores HTTP: autenticacion, loading y logging
- Gestion de estado con Angular Signals (50+ archivos)
- Paginacion completa con `OrderPaginationComponent`
- Busqueda con debounce (300ms) en filtros
- Polling de estado de pedidos con visibility API
- Seccion de demos en `/cliente` con HTTP demos y State demos
- 8 archivos de tests unitarios (79 tests totales)
- Test coverage superior al 50%
- Documentacion tecnica completa (DOCUMENTACION.md, ROUTES.md, API_ENDPOINTS.md, etc.)
- Verificacion cross-browser documentada en CROSS_BROWSER.md
- Build de produccion optimizado con budgets configurados
- Dockerfile multi-stage para desarrollo y produccion

### Cambiado
- Migracion completa a Angular 21 con standalone components
- OnPush change detection en 73 componentes
- Uso de `@for` con `track` en lugar de `*ngFor` con `trackBy`
- Interceptores funcionales en lugar de class-based

---

## [0.9.0] - 2026-01-10

### Agregado
- Seccion de demos cliente (`/cliente`, `/cliente/http`, `/cliente/state`)
- Componentes demo: `HostListenerDemoComponent`, `ViewChildDemoComponent`, `Renderer2DemoComponent`
- Documentacion de patrones tradicionales vs modernos Angular 21
- Breadcrumbs para rutas de demo

### Corregido
- Normalizacion de valores rem a grid base-8 en todas las paginas
- Tipografia fluida para responsividad en wallet page

---

## [0.8.0] - 2026-01-08

### Agregado
- Polling de estado de pedidos con notificaciones toast
- Servicio de pedidos pendientes (`PendingOrderService`) con signals
- Integracion de pedido pendiente en dashboard tras login
- Scheduler de polling de estado de pedidos en backend

### Corregido
- Redireccion a dashboard en lugar de ruta inexistente `/new-order`
- Uso de routerLink con fragment para navegacion de anclas

---

## [0.7.0] - 2026-01-05

### Agregado
- Input de cantidad inicial en seccion add-funds de wallet
- Lectura de query params y navegacion dinamica en wallet
- Emision de cantidad requerida desde dashboard order section
- Soporte de texto inicial en input de dashboard order section

### Corregido
- Parsing inteligente de input para todos los tipos de servicio

---

## [0.6.0] - 2025-12-28

### Agregado
- Verificacion de pago basada en polling para Paymento
- Variables CSS faltantes (`z-notification`)
- Estilos definidos para estados de invoice

### Corregido
- Mantener invoice como pending hasta detectar pago
- Ancho de columna de status badge y manejo de overflow
- Layout de grid de invoices en mobile
- Espaciado mobile de seccion add-funds
- Legibilidad de badge de estado de invoice en mobile

---

## [0.5.0] - 2025-12-20

### Agregado
- Sistema de balance holds para reserva de saldo (`BalanceHoldService`)
- Entidad `BalanceHold` y repositorio
- `OrderCreationFacade` con boundaries de transaccion
- Limpieza programada de holds expirados

### Corregido
- Condicion de carrera al liberar hold en requests concurrentes
- Manejo de excepciones en `GlobalExceptionHandler`
- Permitir invoices pending en polling de estado de pago

---

## [0.4.0] - 2025-12-15

### Agregado
- Migracion automatica SQL con Flyway y Spring Boot 4
- Tabla `balance_holds` para produccion
- Columna `balance_hold_id` y enum `FAILED`

### Corregido
- Nombre de variable CORS para produccion
- Variables de entorno faltantes de Paymento/DripfeedPanel en docker-compose produccion

---

## [0.3.0] - 2025-12-10

### Agregado
- Website MVP beta completado
- Sistema de autenticacion completo (login, register, logout)
- Dashboard con estadisticas de usuario
- Pagina de pedidos con filtrado y paginacion
- Pagina de wallet con historial de invoices
- Formularios reactivos con validacion completa

### Corregido
- Color de fondo de inputs de formularios de auth
- Servicios de ejemplo.sql actualizados con dripfeedpanel

---

## [0.2.0] - 2025-12-01

### Agregado
- Estructura base de componentes Angular 21
- Sistema de rutas con lazy loading
- Servicios core: AuthService, TokenService, OrderService
- Interceptores HTTP basicos
- Tema claro/oscuro con ThemeService
- Componentes compartidos: Button, Modal, Spinner, Tabs, Accordion

---

## [0.1.0] - 2025-11-20

### Agregado
- Configuracion inicial del proyecto Angular 21
- Integracion con Bun como package manager
- Estructura de carpetas siguiendo arquitectura por features
- Configuracion de SCSS con ITCSS + BEM
- Variables CSS para sistema de diseno
- Configuracion de Vitest para testing

---

## Tipos de Cambios

- `Agregado` para nuevas funcionalidades.
- `Cambiado` para cambios en funcionalidades existentes.
- `Obsoleto` para funcionalidades que seran eliminadas proximamente.
- `Eliminado` para funcionalidades eliminadas.
- `Corregido` para correcciones de bugs.
- `Seguridad` para vulnerabilidades corregidas.

---

## Enlaces

- [Repositorio](https://github.com/obezeq/AntiPanel)
- [Documentacion Tecnica](./DOCUMENTACION.md)
- [Rutas y Navegacion](./ROUTES.md)
- [Endpoints API](./API_ENDPOINTS.md)
- [Gestion de Estado](./STATE_MANAGEMENT.md)
- [Compatibilidad Cross-Browser](./CROSS_BROWSER.md)
