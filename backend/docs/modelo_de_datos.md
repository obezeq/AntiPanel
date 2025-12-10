# Diseño de Base de Datos - AntiPanel SMM

## Memoria Técnica del Proyecto

**Proyecto:** AntiPanel - SMM Panel  
**Tecnologías:** Spring Boot + PostgreSQL + Angular  

---

## Índice

1. [Introducción](#1-introducción)
2. [Decisiones de Diseño](#2-decisiones-de-diseño)
3. [Modelo Entidad-Relación](#3-modelo-entidad-relación)
4. [Descripción de Tablas y Campos](#4-descripción-de-tablas-y-campos)
5. [Definición de Tipos ENUM](#5-definición-de-tipos-enum)
6. [Relaciones Entre Entidades](#6-relaciones-entre-entidades)
7. [Claves Primarias y Foráneas](#7-claves-primarias-y-foráneas)
8. [Índices](#8-índices)
9. [Constraints CHECK](#9-constraints-check)
10. [Justificación del Diseño](#10-justificación-del-diseño)
11. [Script SQL Completo](#11-script-sql-completo)

---

## 1. Introducción

Este documento describe el diseño de la base de datos para AntiPanel, un SMM Panel que prioriza la experiencia de usuario mediante un diseño minimalista y una arquitectura robusta.

### 1.1 Alcance del MVP

El MVP incluye las siguientes funcionalidades:

**Usuario:**
- Services Catalog (Catálogo de servicios)
- New Order (Realizar pedidos)
- Payments / Add Funds (Añadir fondos)
- Login & Register (Autenticación)
- User Dashboard (Panel de usuario)
- User Orders (Historial de órdenes)

**Administrador:**
- Admin Dashboard (Panel de administración)
- Manage Users (Gestión de usuarios)
- Manage Services (Gestión de servicios)
- Manage Orders (Gestión de órdenes)

### 1.2 Tecnologías de Base de Datos

- **SGBD:** PostgreSQL 18+
- **ORM:** Spring Data JPA / Hibernate
- **Migraciones:** Flyway o Liquibase (a implementar en el futuro)

---

## 2. Decisiones de Diseño

### 2.1 Almacenamiento de Campos Calculados vs. Cálculo al Vuelo

En sistemas transaccionales como un SMM Panel, los datos de una orden representan una "fotografía" del momento de la compra. Por ello, se ha decidido almacenar campos que podrían calcularse:

| Campo | ¿Almacenar? | Justificación |
|-------|-------------|---------------|
| `total_charge` | ✅ Sí | Los precios cambian. Se necesita saber cuánto pagó el usuario en ese momento exacto |
| `total_cost` | ✅ Sí | El proveedor puede cambiar precios. Se requiere el histórico real para contabilidad |
| `profit` | ✅ Sí | Para reportes rápidos y auditoría. Evita recalcular millones de filas |
| `is_refillable` | ✅ Sí | El servicio puede cambiar sus políticas. La orden debe conservar las condiciones del momento |
| `service_name` | ✅ Sí | Desnormalización intencional para consultas rápidas y persistencia si el servicio se elimina |

### 2.2 Gestión de Usuarios y Roles

Se utiliza una única tabla `users` con sistema de roles en lugar de tablas separadas para admin y usuarios:

- Evita duplicar lógica de autenticación
- Un administrador también puede actuar como usuario
- Escalable para futuros roles (support, moderator, reseller)
- Estándar en la industria

### 2.3 Uso de ENUMs para Estados

Se utilizan tipos ENUM de PostgreSQL para campos de estado en lugar de VARCHAR:

- **Type Safety:** Previene valores inválidos a nivel de base de datos
- **Rendimiento:** Mejor rendimiento en índices y comparaciones
- **Integridad:** Más estricto que VARCHAR, alineado con el principio "empezar estricto y relajar después"
- **Mantenibilidad:** Reduce errores en flujos de órdenes (e.g., refills solo en estado 'completed')

### 2.4 Constraints CHECK para Validaciones

Se implementan validaciones a nivel de base de datos:

- **Integridad Financiera:** Previene balances negativos y cantidades inválidas
- **Seguridad:** Evita datos corruptos que podrían causar problemas en el sistema
- **Defensa en Profundidad:** Complementa las validaciones de la capa de aplicación

---

## 3. Modelo Entidad-Relación

![Diagrama E/R](images/antipanel-database-erd.png)

---

## 4. Descripción de Tablas y Campos

### 4.1 Tabla: `users`

Almacena la información de todos los usuarios del sistema, incluyendo administradores y usuarios regulares.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Identificador único del usuario |
| `email` | `VARCHAR(255)` | `UNIQUE NOT NULL` | Email del usuario (usado para login) |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | Contraseña hasheada con bcrypt |
| `role` | `user_role_enum` | `NOT NULL DEFAULT 'user'` | Rol del usuario: 'user', 'admin', 'support' |
| `department` | `VARCHAR(100)` | `NULL` | Departamento (solo para staff) |
| `balance` | `DECIMAL(12,4)` | `NOT NULL DEFAULT 0.0000` | Saldo disponible del usuario |
| `is_banned` | `BOOLEAN` | `NOT NULL DEFAULT FALSE` | Indica si el usuario está baneado |
| `banned_reason` | `TEXT` | `NULL` | Motivo del baneo |
| `last_login_at` | `TIMESTAMP` | `NULL` | Fecha y hora del último inicio de sesión |
| `login_count` | `INTEGER` | `NOT NULL DEFAULT 0` | Contador de inicios de sesión |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Fecha de registro |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Última actualización del registro |

---

### 4.2 Tabla: `categories`

Define las categorías principales de servicios, correspondientes a las diferentes redes sociales.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador único de la categoría |
| `name` | `VARCHAR(50)` | `UNIQUE NOT NULL` | Nombre de la categoría (Instagram, TikTok, etc.) |
| `slug` | `VARCHAR(50)` | `UNIQUE NOT NULL` | Identificador URL-friendly (instagram, tiktok) |
| `icon_url` | `VARCHAR(500)` | `NULL` | URL del icono de la red social |
| `sort_order` | `INTEGER` | `NOT NULL DEFAULT 0` | Orden de visualización en el catálogo |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Indica si la categoría está visible |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Fecha de creación |

---

### 4.3 Tabla: `service_types`

Define los tipos de servicios disponibles dentro de cada categoría (Followers, Likes, Comments, etc.).

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador único del tipo de servicio |
| `category_id` | `INTEGER` | `NOT NULL, FK → categories(id)` | Categoría a la que pertenece |
| `name` | `VARCHAR(50)` | `NOT NULL` | Nombre del tipo (Followers, Likes, etc.) |
| `slug` | `VARCHAR(50)` | `NOT NULL` | Identificador URL-friendly |
| `sort_order` | `INTEGER` | `NOT NULL DEFAULT 0` | Orden dentro de la categoría |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Indica si está activo |

**Constraint único:** `UNIQUE(category_id, slug)`

---

### 4.4 Tabla: `providers`

Almacena la información de los proveedores externos de servicios SMM.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador único del proveedor |
| `name` | `VARCHAR(100)` | `NOT NULL` | Nombre del proveedor |
| `website` | `VARCHAR(255)` | `NULL` | URL del sitio web del proveedor |
| `api_url` | `VARCHAR(255)` | `NOT NULL` | Endpoint base de la API del proveedor |
| `api_key` | `VARCHAR(255)` | `NOT NULL` | API Key para autenticación |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Indica si el proveedor está activo |
| `balance` | `DECIMAL(12,4)` | `NULL` | Balance actual con el proveedor |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Fecha de registro |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Última actualización |

---

### 4.5 Tabla: `provider_services`

Catálogo de servicios disponibles en cada proveedor externo.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador interno |
| `provider_id` | `INTEGER` | `NOT NULL, FK → providers(id)` | Proveedor que ofrece el servicio |
| `provider_service_id` | `VARCHAR(50)` | `NOT NULL` | ID del servicio en la API del proveedor |
| `name` | `VARCHAR(255)` | `NOT NULL` | Nombre descriptivo del servicio |
| `min_quantity` | `INTEGER` | `NOT NULL` | Cantidad mínima permitida |
| `max_quantity` | `INTEGER` | `NOT NULL` | Cantidad máxima permitida |
| `cost_per_k` | `DECIMAL(10,4)` | `NOT NULL` | Precio por cada 1000 unidades |
| `refill_days` | `INTEGER` | `NOT NULL DEFAULT 0` | Días de garantía (0 = sin refill) |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Indica si está disponible |
| `last_synced_at` | `TIMESTAMP` | `NULL` | Última sincronización con la API |

**Constraint único:** `UNIQUE(provider_id, provider_service_id)`

---

### 4.6 Tabla: `services`

Catálogo público de servicios que se muestran a los usuarios.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `SERIAL` | `PRIMARY KEY` | Service ID visible para el usuario |
| `category_id` | `INTEGER` | `NOT NULL, FK → categories(id)` | Categoría (red social) |
| `service_type_id` | `INTEGER` | `NOT NULL, FK → service_types(id)` | Tipo de servicio |
| `provider_service_id` | `INTEGER` | `NOT NULL, FK → provider_services(id)` | Servicio del proveedor utilizado |
| `name` | `VARCHAR(255)` | `NOT NULL` | Nombre público del servicio |
| `description` | `TEXT` | `NULL` | Descripción detallada para el usuario |
| `quality` | `service_quality_enum` | `NOT NULL` | Calidad: 'low', 'medium', 'high', 'premium' |
| `speed` | `service_speed_enum` | `NOT NULL` | Velocidad: 'slow', 'medium', 'fast', 'instant' |
| `min_quantity` | `INTEGER` | `NOT NULL` | Cantidad mínima que puede pedir el usuario |
| `max_quantity` | `INTEGER` | `NOT NULL` | Cantidad máxima que puede pedir el usuario |
| `price_per_k` | `DECIMAL(10,4)` | `NOT NULL` | Precio al usuario por cada 1000 unidades |
| `refill_days` | `INTEGER` | `NOT NULL DEFAULT 0` | Días de garantía ofrecidos |
| `average_time` | `VARCHAR(50)` | `NULL` | Tiempo estimado de entrega (ej: "1-24 hours") |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Visible en el catálogo |
| `sort_order` | `INTEGER` | `NOT NULL DEFAULT 0` | Orden en el listado |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Fecha de creación |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Última actualización |

---

### 4.7 Tabla: `orders`

Registro de todas las órdenes realizadas por los usuarios.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Identificador único de la orden |
| `user_id` | `BIGINT` | `NOT NULL, FK → users(id)` | Usuario que realizó la orden |
| `service_id` | `INTEGER` | `NOT NULL, FK → services(id)` | Servicio comprado |
| `service_name` | `VARCHAR(255)` | `NOT NULL` | Snapshot del nombre del servicio |
| `provider_service_id` | `INTEGER` | `NOT NULL, FK → provider_services(id)` | Servicio del proveedor utilizado |
| `provider_order_id` | `VARCHAR(100)` | `NULL` | ID de la orden en el sistema del proveedor |
| `target` | `VARCHAR(500)` | `NOT NULL` | Link o username objetivo |
| `quantity` | `INTEGER` | `NOT NULL` | Cantidad solicitada |
| `start_count` | `INTEGER` | `NULL` | Conteo inicial (si aplica) |
| `remains` | `INTEGER` | `NOT NULL DEFAULT 0` | Cantidad pendiente de entregar |
| `status` | `order_status_enum` | `NOT NULL DEFAULT 'pending'` | Estado actual de la orden |
| `price_per_k` | `DECIMAL(10,4)` | `NOT NULL` | Precio por K al momento de la orden |
| `cost_per_k` | `DECIMAL(10,4)` | `NOT NULL` | Costo por K del proveedor al momento |
| `total_charge` | `DECIMAL(12,4)` | `NOT NULL` | Total cobrado al usuario |
| `total_cost` | `DECIMAL(12,4)` | `NOT NULL` | Total que cuesta al panel |
| `profit` | `DECIMAL(12,4)` | `NOT NULL` | Ganancia (total_charge - total_cost) |
| `is_refillable` | `BOOLEAN` | `NOT NULL DEFAULT FALSE` | Indica si permite refill |
| `refill_days` | `INTEGER` | `NOT NULL DEFAULT 0` | Días de garantía |
| `refill_deadline` | `TIMESTAMP` | `NULL` | Fecha límite para solicitar refill |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Fecha de creación de la orden |
| `completed_at` | `TIMESTAMP` | `NULL` | Fecha de completado de la orden |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Última actualización |

---

### 4.8 Tabla: `order_refills`

Registro de solicitudes de refill para órdenes completadas.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Identificador único del refill |
| `order_id` | `BIGINT` | `NOT NULL, FK → orders(id)` | Orden original |
| `provider_refill_id` | `VARCHAR(100)` | `NULL` | ID del refill en el proveedor |
| `quantity` | `INTEGER` | `NOT NULL` | Cantidad a rellenar |
| `status` | `refill_status_enum` | `NOT NULL DEFAULT 'pending'` | Estado del refill |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Fecha de solicitud |
| `completed_at` | `TIMESTAMP` | `NULL` | Fecha de completado |

---

### 4.9 Tabla: `payment_processors`

Configuración de los procesadores de pago disponibles.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador único |
| `name` | `VARCHAR(100)` | `NOT NULL` | Nombre del procesador (PayPal, Stripe, etc.) |
| `code` | `VARCHAR(50)` | `UNIQUE NOT NULL` | Código interno (paypal, stripe, coinbase) |
| `website` | `VARCHAR(255)` | `NULL` | URL del sitio web |
| `api_key` | `VARCHAR(255)` | `NULL` | API Key (encriptada en producción) |
| `api_secret` | `VARCHAR(255)` | `NULL` | API Secret (encriptada en producción) |
| `config_json` | `JSONB` | `NULL` | Configuración adicional en formato JSON |
| `min_amount` | `DECIMAL(10,2)` | `NOT NULL DEFAULT 1.00` | Depósito mínimo permitido |
| `max_amount` | `DECIMAL(10,2)` | `NULL` | Depósito máximo permitido |
| `fee_percentage` | `DECIMAL(5,2)` | `NOT NULL DEFAULT 0.00` | Comisión porcentual |
| `fee_fixed` | `DECIMAL(10,2)` | `NOT NULL DEFAULT 0.00` | Comisión fija |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Indica si está activo |
| `sort_order` | `INTEGER` | `NOT NULL DEFAULT 0` | Orden de visualización |

---

### 4.10 Tabla: `invoices`

Registro de facturas/depósitos de los usuarios.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Identificador interno de la factura |
| `user_id` | `BIGINT` | `NOT NULL, FK → users(id)` | Usuario que realiza el depósito |
| `processor_id` | `INTEGER` | `NOT NULL, FK → payment_processors(id)` | Procesador de pago utilizado |
| `processor_invoice_id` | `VARCHAR(255)` | `NULL` | ID de la transacción en el procesador |
| `amount` | `DECIMAL(10,2)` | `NOT NULL` | Cantidad solicitada a depositar |
| `fee` | `DECIMAL(10,2)` | `NOT NULL DEFAULT 0.00` | Comisión cobrada |
| `net_amount` | `DECIMAL(10,2)` | `NOT NULL` | Cantidad neta a acreditar al usuario |
| `currency` | `VARCHAR(3)` | `NOT NULL DEFAULT 'USD'` | Código de moneda |
| `status` | `invoice_status_enum` | `NOT NULL DEFAULT 'pending'` | Estado de la factura |
| `payment_url` | `VARCHAR(500)` | `NULL` | URL de pago generada (si aplica) |
| `paid_at` | `TIMESTAMP` | `NULL` | Fecha y hora del pago |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Fecha de creación |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Última actualización |

---

### 4.11 Tabla: `transactions`

Registro de todos los movimientos de balance de los usuarios para auditoría.

| Campo | Tipo | Constraints | Descripción |
|-------|------|-------------|-------------|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Identificador único de la transacción |
| `user_id` | `BIGINT` | `NOT NULL, FK → users(id)` | Usuario afectado |
| `type` | `transaction_type_enum` | `NOT NULL` | Tipo: 'deposit', 'order', 'refund', 'adjustment' |
| `amount` | `DECIMAL(12,4)` | `NOT NULL` | Cantidad del movimiento (positiva o negativa) |
| `balance_before` | `DECIMAL(12,4)` | `NOT NULL` | Balance antes del movimiento |
| `balance_after` | `DECIMAL(12,4)` | `NOT NULL` | Balance después del movimiento |
| `reference_type` | `VARCHAR(50)` | `NULL` | Tipo de referencia ('invoice', 'order', etc.) |
| `reference_id` | `BIGINT` | `NULL` | ID de la entidad relacionada |
| `description` | `VARCHAR(255)` | `NULL` | Descripción del movimiento |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Fecha del movimiento |

---

## 5. Definición de Tipos ENUM

Los tipos ENUM proporcionan type safety estricta, previniendo valores inválidos y mejorando el rendimiento en índices y comparaciones.

### 5.1 user_role_enum

```sql
CREATE TYPE user_role_enum AS ENUM ('user', 'admin', 'support');
```

**Valores:**
- `user`: Usuario regular del sistema
- `admin`: Administrador con acceso total
- `support`: Personal de soporte con acceso limitado

### 5.2 service_quality_enum

```sql
CREATE TYPE service_quality_enum AS ENUM ('low', 'medium', 'high', 'premium');
```

**Valores:**
- `low`: Calidad básica, precio más bajo
- `medium`: Calidad estándar
- `high`: Calidad alta
- `premium`: Máxima calidad disponible

### 5.3 service_speed_enum

```sql
CREATE TYPE service_speed_enum AS ENUM ('slow', 'medium', 'fast', 'instant');
```

**Valores:**
- `slow`: Entrega lenta (varios días)
- `medium`: Velocidad estándar (24-48h)
- `fast`: Entrega rápida (1-24h)
- `instant`: Entrega inmediata (minutos)

### 5.4 order_status_enum

```sql
CREATE TYPE order_status_enum AS ENUM (
    'pending',
    'processing', 
    'in_progress', 
    'completed', 
    'partial', 
    'cancelled', 
    'refunded'
);
```

**Valores:**
- `pending`: Orden recibida, pendiente de procesar
- `processing`: Enviada al proveedor, en proceso
- `in_progress`: El proveedor está entregando
- `completed`: Orden completada exitosamente
- `partial`: Completada parcialmente
- `cancelled`: Cancelada antes de procesar
- `refunded`: Reembolsada al usuario

### 5.5 refill_status_enum

```sql
CREATE TYPE refill_status_enum AS ENUM (
    'pending', 
    'processing', 
    'completed', 
    'rejected', 
    'cancelled'
);
```

**Valores:**
- `pending`: Solicitud de refill recibida
- `processing`: Enviada al proveedor
- `completed`: Refill completado
- `rejected`: Rechazada por el proveedor
- `cancelled`: Cancelada

### 5.6 invoice_status_enum

```sql
CREATE TYPE invoice_status_enum AS ENUM (
    'pending', 
    'processing', 
    'completed', 
    'failed', 
    'cancelled', 
    'expired'
);
```

**Valores:**
- `pending`: Factura creada, esperando pago
- `processing`: Pago en proceso de verificación
- `completed`: Pago completado y acreditado
- `failed`: Pago fallido
- `cancelled`: Cancelada por el usuario
- `expired`: Expirada por tiempo

### 5.7 transaction_type_enum

```sql
CREATE TYPE transaction_type_enum AS ENUM (
    'deposit', 
    'order', 
    'refund', 
    'adjustment'
);
```

**Valores:**
- `deposit`: Depósito de fondos
- `order`: Cargo por orden
- `refund`: Reembolso
- `adjustment`: Ajuste manual por administrador

---

## 6. Relaciones Entre Entidades

### 6.1 Diagrama de Relaciones

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        RELACIONES 1:N (Uno a Muchos)                        │
└─────────────────────────────────────────────────────────────────────────────┘

users (1) ──────────────────────< orders (N)
    └── Un usuario puede tener muchas órdenes

users (1) ──────────────────────< invoices (N)
    └── Un usuario puede tener muchas facturas

users (1) ──────────────────────< transactions (N)
    └── Un usuario tiene múltiples movimientos de balance

categories (1) ─────────────────< service_types (N)
    └── Una categoría (Instagram) tiene varios tipos (Followers, Likes)

categories (1) ─────────────────< services (N)
    └── Una categoría tiene múltiples servicios

service_types (1) ──────────────< services (N)
    └── Un tipo de servicio agrupa múltiples servicios

providers (1) ──────────────────< provider_services (N)
    └── Un proveedor ofrece múltiples servicios

provider_services (1) ──────────< services (N)
    └── Un servicio del proveedor puede usarse en varios servicios públicos

provider_services (1) ──────────< orders (N)
    └── Un servicio del proveedor tiene múltiples órdenes

services (1) ───────────────────< orders (N)
    └── Un servicio tiene múltiples órdenes

orders (1) ─────────────────────< order_refills (N)
    └── Una orden puede tener múltiples solicitudes de refill

payment_processors (1) ─────────< invoices (N)
    └── Un procesador de pago tiene múltiples facturas
```

### 6.2 Resumen de Cardinalidades

| Relación | Cardinalidad | Descripción |
|----------|--------------|-------------|
| users → orders | 1:N | Un usuario realiza múltiples órdenes |
| users → invoices | 1:N | Un usuario tiene múltiples depósitos |
| users → transactions | 1:N | Un usuario tiene múltiples movimientos |
| categories → service_types | 1:N | Una red social tiene varios tipos de servicio |
| categories → services | 1:N | Una categoría contiene múltiples servicios |
| service_types → services | 1:N | Un tipo agrupa varios servicios |
| providers → provider_services | 1:N | Un proveedor ofrece múltiples servicios |
| provider_services → services | 1:N | Un servicio externo puede mapearse a varios internos |
| provider_services → orders | 1:N | Un servicio externo recibe múltiples órdenes |
| services → orders | 1:N | Un servicio tiene múltiples órdenes |
| orders → order_refills | 1:N | Una orden puede tener varios refills |
| payment_processors → invoices | 1:N | Un procesador maneja múltiples facturas |

---

## 7. Claves Primarias y Foráneas

### 7.1 Claves Primarias

| Tabla | Clave Primaria | Tipo | Descripción |
|-------|----------------|------|-------------|
| `users` | `id` | `BIGSERIAL` | Auto-incremental, soporte para alto volumen |
| `categories` | `id` | `SERIAL` | Auto-incremental |
| `service_types` | `id` | `SERIAL` | Auto-incremental |
| `providers` | `id` | `SERIAL` | Auto-incremental |
| `provider_services` | `id` | `SERIAL` | Auto-incremental |
| `services` | `id` | `SERIAL` | Auto-incremental, visible al usuario |
| `orders` | `id` | `BIGSERIAL` | Auto-incremental, soporte para alto volumen |
| `order_refills` | `id` | `BIGSERIAL` | Auto-incremental |
| `payment_processors` | `id` | `SERIAL` | Auto-incremental |
| `invoices` | `id` | `BIGSERIAL` | Auto-incremental |
| `transactions` | `id` | `BIGSERIAL` | Auto-incremental |

### 7.2 Claves Foráneas

| Tabla | Campo FK | Referencia | ON DELETE | ON UPDATE |
|-------|----------|------------|-----------|-----------|
| `service_types` | `category_id` | `categories(id)` | `RESTRICT` | `CASCADE` |
| `provider_services` | `provider_id` | `providers(id)` | `RESTRICT` | `CASCADE` |
| `services` | `category_id` | `categories(id)` | `RESTRICT` | `CASCADE` |
| `services` | `service_type_id` | `service_types(id)` | `RESTRICT` | `CASCADE` |
| `services` | `provider_service_id` | `provider_services(id)` | `RESTRICT` | `CASCADE` |
| `orders` | `user_id` | `users(id)` | `RESTRICT` | `CASCADE` |
| `orders` | `service_id` | `services(id)` | `RESTRICT` | `CASCADE` |
| `orders` | `provider_service_id` | `provider_services(id)` | `RESTRICT` | `CASCADE` |
| `order_refills` | `order_id` | `orders(id)` | `CASCADE` | `CASCADE` |
| `invoices` | `user_id` | `users(id)` | `RESTRICT` | `CASCADE` |
| `invoices` | `processor_id` | `payment_processors(id)` | `RESTRICT` | `CASCADE` |
| `transactions` | `user_id` | `users(id)` | `RESTRICT` | `CASCADE` |

**Justificación de ON DELETE:**
- `RESTRICT`: Previene eliminación accidental de datos referenciados (usuarios, servicios, proveedores)
- `CASCADE`: Para datos dependientes que no tienen sentido sin el padre (refills de una orden)

---

## 8. Índices

### 8.1 Índices Primarios (Automáticos)

PostgreSQL crea automáticamente índices para todas las claves primarias y constraints UNIQUE.

### 8.2 Índices de Claves Foráneas

```sql
-- service_types
CREATE INDEX idx_service_types_category ON service_types(category_id);

-- provider_services
CREATE INDEX idx_provider_services_provider ON provider_services(provider_id);

-- services
CREATE INDEX idx_services_category ON services(category_id);
CREATE INDEX idx_services_service_type ON services(service_type_id);
CREATE INDEX idx_services_provider_service ON services(provider_service_id);

-- orders
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_service ON orders(service_id);
CREATE INDEX idx_orders_provider_service ON orders(provider_service_id);

-- order_refills
CREATE INDEX idx_order_refills_order ON order_refills(order_id);

-- invoices
CREATE INDEX idx_invoices_user ON invoices(user_id);
CREATE INDEX idx_invoices_processor ON invoices(processor_id);

-- transactions
CREATE INDEX idx_transactions_user ON transactions(user_id);
```

### 8.3 Índices de Búsqueda y Filtrado

```sql
-- users: búsqueda por email y filtrado por rol
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_banned ON users(is_banned) WHERE is_banned = TRUE;

-- services: catálogo público
CREATE INDEX idx_services_active ON services(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_services_category_active ON services(category_id, is_active);

-- orders: consultas frecuentes del dashboard
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at DESC);
CREATE INDEX idx_orders_provider_order ON orders(provider_order_id) WHERE provider_order_id IS NOT NULL;

-- invoices: estado de pagos
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_created ON invoices(created_at DESC);
```

### 8.4 Índices Compuestos Estratégicos

```sql
-- orders: listado de órdenes por usuario con estado
CREATE INDEX idx_orders_user_status ON orders(user_id, status);

-- orders: órdenes pendientes de refill
CREATE INDEX idx_orders_refill_deadline ON orders(refill_deadline) 
    WHERE is_refillable = TRUE AND refill_deadline IS NOT NULL;

-- orders: órdenes por usuario ordenadas por fecha
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at DESC);

-- invoices: facturas por usuario y estado
CREATE INDEX idx_invoices_user_status ON invoices(user_id, status);

-- transactions: historial por usuario
CREATE INDEX idx_transactions_user_created ON transactions(user_id, created_at DESC);

-- services: filtrado completo del catálogo
CREATE INDEX idx_services_catalog ON services(category_id, service_type_id, is_active);
```

### 8.5 Justificación de Índices

| Índice | Justificación |
|--------|---------------|
| `idx_orders_user_status` | Acelera el dashboard del usuario (órdenes filtradas por estado) |
| `idx_orders_refill_deadline` | Optimiza la búsqueda de órdenes elegibles para refill |
| `idx_orders_created` | Mejora ordenamiento cronológico en listados |
| `idx_services_catalog` | Acelera el filtrado del catálogo público |
| `idx_invoices_user_status` | Optimiza consultas de pagos pendientes por usuario |

---

## 9. Constraints CHECK

Las constraints CHECK proporcionan validación a nivel de base de datos, complementando las validaciones de la capa de aplicación.

### 9.1 Constraints en `users`

```sql
-- El balance nunca puede ser negativo
ALTER TABLE users ADD CONSTRAINT chk_users_balance_positive 
    CHECK (balance >= 0);

-- El contador de login no puede ser negativo
ALTER TABLE users ADD CONSTRAINT chk_users_login_count_positive 
    CHECK (login_count >= 0);
```

### 9.2 Constraints en `services`

```sql
-- La cantidad mínima debe ser positiva
ALTER TABLE services ADD CONSTRAINT chk_services_min_quantity_positive 
    CHECK (min_quantity > 0);

-- La cantidad máxima debe ser mayor o igual a la mínima
ALTER TABLE services ADD CONSTRAINT chk_services_max_gte_min 
    CHECK (max_quantity >= min_quantity);

-- El precio debe ser positivo
ALTER TABLE services ADD CONSTRAINT chk_services_price_positive 
    CHECK (price_per_k > 0);

-- Los días de refill no pueden ser negativos
ALTER TABLE services ADD CONSTRAINT chk_services_refill_days_positive 
    CHECK (refill_days >= 0);
```

### 9.3 Constraints en `provider_services`

```sql
-- La cantidad mínima debe ser positiva
ALTER TABLE provider_services ADD CONSTRAINT chk_provider_services_min_positive 
    CHECK (min_quantity > 0);

-- La cantidad máxima debe ser mayor o igual a la mínima
ALTER TABLE provider_services ADD CONSTRAINT chk_provider_services_max_gte_min 
    CHECK (max_quantity >= min_quantity);

-- El costo debe ser positivo
ALTER TABLE provider_services ADD CONSTRAINT chk_provider_services_cost_positive 
    CHECK (cost_per_k > 0);

-- Los días de refill no pueden ser negativos
ALTER TABLE provider_services ADD CONSTRAINT chk_provider_services_refill_positive 
    CHECK (refill_days >= 0);
```

### 9.4 Constraints en `orders`

```sql
-- La cantidad debe ser positiva
ALTER TABLE orders ADD CONSTRAINT chk_orders_quantity_positive 
    CHECK (quantity > 0);

-- Remains no puede ser negativo
ALTER TABLE orders ADD CONSTRAINT chk_orders_remains_positive 
    CHECK (remains >= 0);

-- Remains no puede exceder la cantidad original
ALTER TABLE orders ADD CONSTRAINT chk_orders_remains_lte_quantity 
    CHECK (remains <= quantity);

-- Los precios deben ser positivos
ALTER TABLE orders ADD CONSTRAINT chk_orders_prices_positive 
    CHECK (price_per_k > 0 AND cost_per_k > 0);

-- Los totales deben ser positivos
ALTER TABLE orders ADD CONSTRAINT chk_orders_totals_positive 
    CHECK (total_charge > 0 AND total_cost >= 0);

-- Los días de refill no pueden ser negativos
ALTER TABLE orders ADD CONSTRAINT chk_orders_refill_days_positive 
    CHECK (refill_days >= 0);
```

### 9.5 Constraints en `order_refills`

```sql
-- La cantidad de refill debe ser positiva
ALTER TABLE order_refills ADD CONSTRAINT chk_order_refills_quantity_positive 
    CHECK (quantity > 0);
```

### 9.6 Constraints en `invoices`

```sql
-- El monto debe ser positivo
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_amount_positive 
    CHECK (amount > 0);

-- La comisión no puede ser negativa
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_fee_positive 
    CHECK (fee >= 0);

-- El monto neto debe ser positivo
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_net_amount_positive 
    CHECK (net_amount > 0);

-- El monto neto debe ser menor o igual al monto total
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_net_lte_amount 
    CHECK (net_amount <= amount);
```

### 9.7 Constraints en `payment_processors`

```sql
-- El monto mínimo debe ser positivo
ALTER TABLE payment_processors ADD CONSTRAINT chk_processors_min_positive 
    CHECK (min_amount > 0);

-- El monto máximo debe ser mayor al mínimo (si existe)
ALTER TABLE payment_processors ADD CONSTRAINT chk_processors_max_gte_min 
    CHECK (max_amount IS NULL OR max_amount >= min_amount);

-- Las comisiones no pueden ser negativas
ALTER TABLE payment_processors ADD CONSTRAINT chk_processors_fees_positive 
    CHECK (fee_percentage >= 0 AND fee_fixed >= 0);
```

---

## 10. Justificación del Diseño

### 10.1 Separación de `categories` y `service_types`

**Problema:** Las diferentes redes sociales tienen distintos tipos de servicios (Instagram tiene Reels, TikTok tiene Duets, LinkedIn no tiene Stories).

**Solución:** Dos tablas separadas con relación 1:N permiten:
- Añadir o eliminar redes sociales fácilmente
- Definir tipos de servicio específicos por red social
- Ordenar y organizar el catálogo de forma flexible
- Cumplir con el objetivo de reducir la sobrecarga de opciones (Ley de Hick)

### 10.2 Separación de `provider_services` y `services`

**Problema:** Necesitamos mapear servicios externos de proveedores a nuestro catálogo público, manteniendo independencia.

**Solución:** Dos tablas separadas permiten:
- **Independencia de precios:** Tu margen puede variar por servicio
- **Cambio de proveedor:** Puedes cambiar el proveedor de un servicio sin afectar al usuario
- **Múltiples servicios:** Un servicio del proveedor puede usarse en varios servicios públicos con diferentes configuraciones
- **Sincronización:** Actualizar datos del proveedor sin afectar al catálogo público

### 10.3 Tabla `transactions` para Auditoría

**Problema:** En sistemas financieros, es crítico tener un registro inmutable de todos los movimientos de dinero.

**Solución:** La tabla `transactions` proporciona:
- **Auditoría completa:** Cada movimiento de balance queda registrado
- **Debugging:** Si el balance no cuadra, se puede reconstruir el historial
- **Transparencia:** El usuario ve todos sus movimientos
- **Compliance:** Facilita auditorías financieras y fiscales

### 10.4 Almacenamiento de `profit` y Campos Calculados

**Problema:** Calcular el profit en tiempo real requiere JOINs costosos sobre millones de filas.

**Solución:** Almacenar campos calculados proporciona:
- **Performance:** Dashboard del admin carga instantáneamente
- **Histórico fiel:** El margen real en el momento de la venta
- **Reportes rápidos:** Agregaciones sin recálculo
- **Integridad temporal:** Si los precios cambian, las órdenes antiguas mantienen sus valores originales

### 10.5 Uso de ENUMs para Estados

**Problema:** Los campos VARCHAR para estados permiten valores inválidos y son menos eficientes.

**Solución:** Los tipos ENUM de PostgreSQL proporcionan:
- **Type Safety estricta:** La base de datos rechaza valores no definidos
- **Mejor rendimiento:** Los ENUMs se almacenan como enteros internamente
- **Documentación implícita:** Los valores válidos están definidos en el schema
- **Prevención de errores:** Evita bugs como `status = 'Completed'` vs `status = 'completed'`
- **Facilita migraciones:** Añadir nuevos estados es una operación controlada

### 10.6 Sistema de Roles en `users`

**Problema:** Separar admins y usuarios en tablas diferentes duplica lógica y complica el sistema.

**Solución:** Una única tabla con roles proporciona:
- **Simplicidad:** Una sola lógica de autenticación
- **Flexibilidad:** Un admin puede tener balance y hacer órdenes de prueba
- **Escalabilidad:** Fácil añadir roles futuros (support, reseller, moderator)
- **Estándar de la industria:** Patrón RBAC (Role-Based Access Control)

### 10.7 Constraints CHECK para Validaciones

**Problema:** Las validaciones solo en la aplicación pueden ser bypasseadas o tener bugs.

**Solución:** Constraints a nivel de base de datos proporcionan:
- **Defensa en profundidad:** Última línea de defensa contra datos inválidos
- **Integridad garantizada:** Imposible tener balance negativo o cantidades inválidas
- **Consistencia:** Las reglas se aplican independientemente de cómo se acceda a la BD
- **Documentación:** Las reglas de negocio están documentadas en el schema

### 10.8 Índices Estratégicos

**Problema:** Consultas lentas en tablas con millones de registros.

**Solución:** Índices específicos para los casos de uso más frecuentes:
- **Índices simples:** Para búsquedas por una columna (email, status)
- **Índices compuestos:** Para consultas con múltiples filtros (user_id + status)
- **Índices parciales:** Para subconjuntos frecuentes (solo activos, solo refillables)
- **Balance:** Solo indexar columnas frecuentes en WHERE/JOIN/ORDER BY

### 10.9 Campo `completed_at` en Orders

**Problema:** Calcular tiempos promedio de entrega requiere conocer cuándo se completó cada orden.

**Solución:** El campo `completed_at` permite:
- **Cálculo de `average_time`:** Estadísticas reales vs estimadas
- **SLA monitoring:** Detectar servicios lentos
- **Reportes:** Tiempo promedio por servicio/proveedor
- **Mejora continua:** Datos para optimizar la selección de proveedores

---

## 11. Script SQL Completo

- [**SCRIPT DE CREACIÓN DE BASE DE DATOS**](../sql/init.sql)
- [**Anexo A: Datos de ejemplo**](../sql/example.sql)

