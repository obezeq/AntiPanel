-- ============================================================================
-- ANTIPANEL SMM - SCRIPT DE INICIALIZACIÓN DE BASE DE DATOS
-- PostgreSQL 18 (Latest Stable)
-- ============================================================================
-- Proyecto: AntiPanel
-- Descripción: Script idempotente para crear/recrear la base de datos completa
-- ============================================================================

-- ============================================================================
-- CONFIGURACIÓN INICIAL
-- ============================================================================

-- Asegurar que usamos UTF-8 para caracteres especiales
SET client_encoding = 'UTF8';

-- Configurar timezone (ajustar según necesidad)
SET timezone = 'UTC';

-- ============================================================================
-- PASO 0: LIMPIEZA - DROP EN ORDEN INVERSO (por dependencias FK)
-- ============================================================================
-- Usamos CASCADE para eliminar dependencias automáticamente
-- El orden es importante: primero tablas dependientes, luego las principales

-- Eliminar triggers primero (dependen de la función)
DROP TRIGGER IF EXISTS trg_invoices_updated_at ON invoices;
DROP TRIGGER IF EXISTS trg_orders_updated_at ON orders;
DROP TRIGGER IF EXISTS trg_services_updated_at ON services;
DROP TRIGGER IF EXISTS trg_providers_updated_at ON providers;
DROP TRIGGER IF EXISTS trg_users_updated_at ON users;

-- Eliminar función
DROP FUNCTION IF EXISTS update_updated_at_column();

-- Eliminar tablas en orden inverso de dependencias
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS invoices CASCADE;
DROP TABLE IF EXISTS order_refills CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS services CASCADE;
DROP TABLE IF EXISTS provider_services CASCADE;
DROP TABLE IF EXISTS providers CASCADE;
DROP TABLE IF EXISTS service_types CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS payment_processors CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Eliminar tipos ENUM
DROP TYPE IF EXISTS transaction_type_enum CASCADE;
DROP TYPE IF EXISTS invoice_status_enum CASCADE;
DROP TYPE IF EXISTS refill_status_enum CASCADE;
DROP TYPE IF EXISTS order_status_enum CASCADE;
DROP TYPE IF EXISTS service_speed_enum CASCADE;
DROP TYPE IF EXISTS service_quality_enum CASCADE;
DROP TYPE IF EXISTS user_role_enum CASCADE;

-- ============================================================================
-- PASO 1: CREAR TIPOS ENUM
-- ============================================================================
-- Los ENUMs proporcionan type safety y mejor rendimiento que VARCHAR

-- Rol de usuario en el sistema
CREATE TYPE user_role_enum AS ENUM (
    'user',      -- Usuario regular
    'admin',     -- Administrador con acceso total
    'support'    -- Personal de soporte con acceso limitado
);

-- Calidad del servicio ofrecido
CREATE TYPE service_quality_enum AS ENUM (
    'low',       -- Calidad básica, precio más bajo
    'medium',    -- Calidad estándar
    'high',      -- Calidad alta
    'premium'    -- Máxima calidad disponible
);

-- Velocidad de entrega del servicio
CREATE TYPE service_speed_enum AS ENUM (
    'slow',      -- Entrega lenta (varios días)
    'medium',    -- Velocidad estándar (24-48h)
    'fast',      -- Entrega rápida (1-24h)
    'instant'    -- Entrega inmediata (minutos)
);

-- Estado de una orden
CREATE TYPE order_status_enum AS ENUM (
    'pending',      -- Orden recibida, pendiente de procesar
    'processing',   -- Enviada al proveedor, en proceso
    'in_progress',  -- El proveedor está entregando
    'completed',    -- Orden completada exitosamente
    'partial',      -- Completada parcialmente
    'cancelled',    -- Cancelada antes de procesar
    'refunded'      -- Reembolsada al usuario
);

-- Estado de una solicitud de refill
CREATE TYPE refill_status_enum AS ENUM (
    'pending',      -- Solicitud de refill recibida
    'processing',   -- Enviada al proveedor
    'completed',    -- Refill completado
    'rejected',     -- Rechazada por el proveedor
    'cancelled'     -- Cancelada
);

-- Estado de una factura/depósito
CREATE TYPE invoice_status_enum AS ENUM (
    'pending',      -- Factura creada, esperando pago
    'processing',   -- Pago en proceso de verificación
    'completed',    -- Pago completado y acreditado
    'failed',       -- Pago fallido
    'cancelled',    -- Cancelada por el usuario
    'expired'       -- Expirada por tiempo
);

-- Tipo de transacción de balance
CREATE TYPE transaction_type_enum AS ENUM (
    'deposit',      -- Depósito de fondos
    'order',        -- Cargo por orden
    'refund',       -- Reembolso
    'adjustment'    -- Ajuste manual por administrador
);

-- ============================================================================
-- PASO 2: CREAR TABLAS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: users
-- Almacena todos los usuarios del sistema (admins, support, usuarios regulares)
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role_enum NOT NULL DEFAULT 'user',
    department VARCHAR(100),
    balance DECIMAL(12, 4) NOT NULL DEFAULT 0.0000,
    is_banned BOOLEAN NOT NULL DEFAULT FALSE,
    banned_reason TEXT,
    last_login_at TIMESTAMPTZ,
    login_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_users_login_count_non_negative CHECK (login_count >= 0),
    CONSTRAINT chk_users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Comentarios descriptivos
COMMENT ON TABLE users IS 'Usuarios del sistema: admins, soporte y usuarios regulares';
COMMENT ON COLUMN users.id IS 'Identificador único auto-incremental';
COMMENT ON COLUMN users.email IS 'Email único usado para autenticación';
COMMENT ON COLUMN users.password_hash IS 'Hash de contraseña (bcrypt recomendado)';
COMMENT ON COLUMN users.role IS 'Rol del usuario: user, admin, support';
COMMENT ON COLUMN users.department IS 'Departamento (solo para staff)';
COMMENT ON COLUMN users.balance IS 'Saldo disponible en USD (4 decimales para precisión)';
COMMENT ON COLUMN users.is_banned IS 'Indica si el usuario está baneado';
COMMENT ON COLUMN users.banned_reason IS 'Motivo del baneo (si aplica)';
COMMENT ON COLUMN users.last_login_at IS 'Fecha/hora del último inicio de sesión';
COMMENT ON COLUMN users.login_count IS 'Contador de inicios de sesión';

-- ----------------------------------------------------------------------------
-- Tabla: categories
-- Categorías principales de servicios (redes sociales)
-- ----------------------------------------------------------------------------
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL,
    icon_url VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT uq_categories_name UNIQUE (name),
    CONSTRAINT uq_categories_slug UNIQUE (slug),
    CONSTRAINT chk_categories_slug_format CHECK (slug ~* '^[a-z0-9-]+$')
);

COMMENT ON TABLE categories IS 'Categorías de servicios (redes sociales: Instagram, TikTok, etc.)';
COMMENT ON COLUMN categories.slug IS 'Identificador URL-friendly (solo minúsculas, números y guiones)';
COMMENT ON COLUMN categories.sort_order IS 'Orden de visualización en el catálogo';

-- ----------------------------------------------------------------------------
-- Tabla: service_types
-- Tipos de servicio por categoría (Followers, Likes, Comments, etc.)
-- ----------------------------------------------------------------------------
CREATE TABLE service_types (
    id SERIAL PRIMARY KEY,
    category_id INTEGER NOT NULL,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Foreign Keys
    CONSTRAINT fk_service_types_category 
        FOREIGN KEY (category_id) 
        REFERENCES categories(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    -- Constraints
    CONSTRAINT uq_service_types_category_slug UNIQUE (category_id, slug),
    CONSTRAINT chk_service_types_slug_format CHECK (slug ~* '^[a-z0-9-]+$')
);

COMMENT ON TABLE service_types IS 'Tipos de servicio por categoría (Followers, Likes, Comments, etc.)';
COMMENT ON COLUMN service_types.category_id IS 'FK a la categoría (red social) a la que pertenece';

-- ----------------------------------------------------------------------------
-- Tabla: providers
-- Proveedores externos de servicios SMM
-- ----------------------------------------------------------------------------
CREATE TABLE providers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    website VARCHAR(255),
    api_url VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    balance DECIMAL(12, 4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT chk_providers_balance_non_negative CHECK (balance IS NULL OR balance >= 0)
);

COMMENT ON TABLE providers IS 'Proveedores externos de servicios SMM';
COMMENT ON COLUMN providers.api_url IS 'Endpoint base de la API del proveedor';
COMMENT ON COLUMN providers.api_key IS 'API Key del proveedor (encriptar en producción)';
COMMENT ON COLUMN providers.balance IS 'Balance actual con el proveedor (opcional)';

-- ----------------------------------------------------------------------------
-- Tabla: provider_services
-- Catálogo de servicios disponibles en cada proveedor
-- ----------------------------------------------------------------------------
CREATE TABLE provider_services (
    id SERIAL PRIMARY KEY,
    provider_id INTEGER NOT NULL,
    provider_service_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    min_quantity INTEGER NOT NULL,
    max_quantity INTEGER NOT NULL,
    cost_per_k DECIMAL(10, 4) NOT NULL,
    refill_days INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_synced_at TIMESTAMPTZ,
    
    -- Foreign Keys
    CONSTRAINT fk_provider_services_provider 
        FOREIGN KEY (provider_id) 
        REFERENCES providers(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    -- Constraints
    CONSTRAINT uq_provider_services_provider_service UNIQUE (provider_id, provider_service_id),
    CONSTRAINT chk_provider_services_min_positive CHECK (min_quantity > 0),
    CONSTRAINT chk_provider_services_max_gte_min CHECK (max_quantity >= min_quantity),
    CONSTRAINT chk_provider_services_cost_positive CHECK (cost_per_k > 0),
    CONSTRAINT chk_provider_services_refill_non_negative CHECK (refill_days >= 0)
);

COMMENT ON TABLE provider_services IS 'Catálogo de servicios disponibles en cada proveedor externo';
COMMENT ON COLUMN provider_services.provider_service_id IS 'ID del servicio en la API del proveedor';
COMMENT ON COLUMN provider_services.cost_per_k IS 'Precio que nos cobra el proveedor por cada 1000 unidades';
COMMENT ON COLUMN provider_services.refill_days IS 'Días de garantía para refill (0 = sin refill)';
COMMENT ON COLUMN provider_services.last_synced_at IS 'Última sincronización con la API del proveedor';

-- ----------------------------------------------------------------------------
-- Tabla: services
-- Catálogo público de servicios mostrados a los usuarios
-- ----------------------------------------------------------------------------
CREATE TABLE services (
    id SERIAL PRIMARY KEY,
    category_id INTEGER NOT NULL,
    service_type_id INTEGER NOT NULL,
    provider_service_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    quality service_quality_enum NOT NULL,
    speed service_speed_enum NOT NULL,
    min_quantity INTEGER NOT NULL,
    max_quantity INTEGER NOT NULL,
    price_per_k DECIMAL(10, 4) NOT NULL,
    refill_days INTEGER NOT NULL DEFAULT 0,
    average_time VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign Keys
    CONSTRAINT fk_services_category 
        FOREIGN KEY (category_id) 
        REFERENCES categories(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT fk_services_service_type 
        FOREIGN KEY (service_type_id) 
        REFERENCES service_types(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT fk_services_provider_service 
        FOREIGN KEY (provider_service_id) 
        REFERENCES provider_services(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_services_min_positive CHECK (min_quantity > 0),
    CONSTRAINT chk_services_max_gte_min CHECK (max_quantity >= min_quantity),
    CONSTRAINT chk_services_price_positive CHECK (price_per_k > 0),
    CONSTRAINT chk_services_refill_non_negative CHECK (refill_days >= 0)
);

COMMENT ON TABLE services IS 'Catálogo público de servicios mostrados a los usuarios';
COMMENT ON COLUMN services.price_per_k IS 'Precio al usuario por cada 1000 unidades';
COMMENT ON COLUMN services.average_time IS 'Tiempo estimado de entrega (ej: "1-24 hours")';
COMMENT ON COLUMN services.quality IS 'Nivel de calidad: low, medium, high, premium';
COMMENT ON COLUMN services.speed IS 'Velocidad de entrega: slow, medium, fast, instant';

-- ----------------------------------------------------------------------------
-- Tabla: payment_processors
-- Configuración de procesadores de pago
-- ----------------------------------------------------------------------------
CREATE TABLE payment_processors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    website VARCHAR(255),
    api_key VARCHAR(255),
    api_secret VARCHAR(255),
    config_json JSONB,
    min_amount DECIMAL(10, 2) NOT NULL DEFAULT 1.00,
    max_amount DECIMAL(10, 2),
    fee_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    fee_fixed DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT uq_payment_processors_code UNIQUE (code),
    CONSTRAINT chk_processors_min_positive CHECK (min_amount > 0),
    CONSTRAINT chk_processors_max_gte_min CHECK (max_amount IS NULL OR max_amount >= min_amount),
    CONSTRAINT chk_processors_fee_percentage_valid CHECK (fee_percentage >= 0 AND fee_percentage <= 100),
    CONSTRAINT chk_processors_fee_fixed_non_negative CHECK (fee_fixed >= 0)
);

COMMENT ON TABLE payment_processors IS 'Configuración de procesadores de pago disponibles';
COMMENT ON COLUMN payment_processors.code IS 'Código interno único (paypal, stripe, coinbase)';
COMMENT ON COLUMN payment_processors.config_json IS 'Configuración adicional específica del procesador';
COMMENT ON COLUMN payment_processors.fee_percentage IS 'Comisión porcentual (0-100)';
COMMENT ON COLUMN payment_processors.fee_fixed IS 'Comisión fija por transacción';

-- ----------------------------------------------------------------------------
-- Tabla: orders
-- Registro de todas las órdenes realizadas
-- ----------------------------------------------------------------------------
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    service_id INTEGER NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    provider_service_id INTEGER NOT NULL,
    provider_order_id VARCHAR(100),
    target VARCHAR(500) NOT NULL,
    quantity INTEGER NOT NULL,
    start_count INTEGER,
    remains INTEGER NOT NULL DEFAULT 0,
    status order_status_enum NOT NULL DEFAULT 'pending',
    price_per_k DECIMAL(10, 4) NOT NULL,
    cost_per_k DECIMAL(10, 4) NOT NULL,
    total_charge DECIMAL(12, 4) NOT NULL,
    total_cost DECIMAL(12, 4) NOT NULL,
    profit DECIMAL(12, 4) NOT NULL,
    is_refillable BOOLEAN NOT NULL DEFAULT FALSE,
    refill_days INTEGER NOT NULL DEFAULT 0,
    refill_deadline TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign Keys
    CONSTRAINT fk_orders_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT fk_orders_service 
        FOREIGN KEY (service_id) 
        REFERENCES services(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT fk_orders_provider_service 
        FOREIGN KEY (provider_service_id) 
        REFERENCES provider_services(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_orders_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_orders_remains_non_negative CHECK (remains >= 0),
    CONSTRAINT chk_orders_remains_lte_quantity CHECK (remains <= quantity),
    CONSTRAINT chk_orders_prices_positive CHECK (price_per_k > 0 AND cost_per_k > 0),
    CONSTRAINT chk_orders_total_charge_positive CHECK (total_charge > 0),
    CONSTRAINT chk_orders_total_cost_non_negative CHECK (total_cost >= 0),
    CONSTRAINT chk_orders_refill_days_non_negative CHECK (refill_days >= 0)
);

COMMENT ON TABLE orders IS 'Registro de todas las órdenes realizadas por los usuarios';
COMMENT ON COLUMN orders.service_name IS 'Snapshot del nombre del servicio al momento de la orden';
COMMENT ON COLUMN orders.provider_order_id IS 'ID de la orden en el sistema del proveedor';
COMMENT ON COLUMN orders.target IS 'Link o username objetivo de la orden';
COMMENT ON COLUMN orders.start_count IS 'Conteo inicial antes de la entrega (si aplica)';
COMMENT ON COLUMN orders.remains IS 'Cantidad pendiente de entregar';
COMMENT ON COLUMN orders.total_charge IS 'Total cobrado al usuario';
COMMENT ON COLUMN orders.total_cost IS 'Total que cuesta al panel (proveedor)';
COMMENT ON COLUMN orders.profit IS 'Ganancia = total_charge - total_cost';
COMMENT ON COLUMN orders.refill_deadline IS 'Fecha límite para solicitar refill';
COMMENT ON COLUMN orders.completed_at IS 'Fecha de completado (para cálculo de average_time)';

-- ----------------------------------------------------------------------------
-- Tabla: order_refills
-- Solicitudes de refill para órdenes completadas
-- ----------------------------------------------------------------------------
CREATE TABLE order_refills (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    provider_refill_id VARCHAR(100),
    quantity INTEGER NOT NULL,
    status refill_status_enum NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    
    -- Foreign Keys (CASCADE porque un refill no tiene sentido sin su orden)
    CONSTRAINT fk_order_refills_order 
        FOREIGN KEY (order_id) 
        REFERENCES orders(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_order_refills_quantity_positive CHECK (quantity > 0)
);

COMMENT ON TABLE order_refills IS 'Solicitudes de refill para órdenes completadas';
COMMENT ON COLUMN order_refills.provider_refill_id IS 'ID del refill en el sistema del proveedor';

-- ----------------------------------------------------------------------------
-- Tabla: invoices
-- Registro de facturas/depósitos de usuarios
-- ----------------------------------------------------------------------------
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    processor_id INTEGER NOT NULL,
    processor_invoice_id VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    net_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status invoice_status_enum NOT NULL DEFAULT 'pending',
    payment_url VARCHAR(500),
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign Keys
    CONSTRAINT fk_invoices_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT fk_invoices_processor 
        FOREIGN KEY (processor_id) 
        REFERENCES payment_processors(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_invoices_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_invoices_fee_non_negative CHECK (fee >= 0),
    CONSTRAINT chk_invoices_net_amount_positive CHECK (net_amount > 0),
    CONSTRAINT chk_invoices_net_lte_amount CHECK (net_amount <= amount),
    CONSTRAINT chk_invoices_currency_format CHECK (currency ~* '^[A-Z]{3}$')
);

COMMENT ON TABLE invoices IS 'Registro de facturas/depósitos de usuarios';
COMMENT ON COLUMN invoices.processor_invoice_id IS 'ID de la transacción en el procesador de pago';
COMMENT ON COLUMN invoices.net_amount IS 'Cantidad neta a acreditar después de comisiones';
COMMENT ON COLUMN invoices.payment_url IS 'URL de pago generada (si aplica)';

-- ----------------------------------------------------------------------------
-- Tabla: transactions
-- Registro de auditoría de todos los movimientos de balance
-- ----------------------------------------------------------------------------
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type transaction_type_enum NOT NULL,
    amount DECIMAL(12, 4) NOT NULL,
    balance_before DECIMAL(12, 4) NOT NULL,
    balance_after DECIMAL(12, 4) NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign Keys
    CONSTRAINT fk_transactions_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_transactions_balance_consistency 
        CHECK (
            (type = 'deposit' AND amount > 0 AND balance_after = balance_before + amount) OR
            (type = 'order' AND amount < 0 AND balance_after = balance_before + amount) OR
            (type = 'refund' AND amount > 0 AND balance_after = balance_before + amount) OR
            (type = 'adjustment' AND balance_after = balance_before + amount)
        )
);

COMMENT ON TABLE transactions IS 'Registro de auditoría de todos los movimientos de balance';
COMMENT ON COLUMN transactions.reference_type IS 'Tipo de entidad relacionada (invoice, order, etc.)';
COMMENT ON COLUMN transactions.reference_id IS 'ID de la entidad relacionada';
COMMENT ON COLUMN transactions.balance_before IS 'Balance del usuario antes del movimiento';
COMMENT ON COLUMN transactions.balance_after IS 'Balance del usuario después del movimiento';

-- ============================================================================
-- PASO 3: CREAR ÍNDICES
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Índices para users
-- ---------------------------------------------------------------------------
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
-- Índice parcial: solo usuarios baneados (optimiza consultas de moderación)
CREATE INDEX idx_users_banned ON users(is_banned) WHERE is_banned = TRUE;

-- ---------------------------------------------------------------------------
-- Índices para categories
-- ---------------------------------------------------------------------------
CREATE INDEX idx_categories_active ON categories(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_categories_sort ON categories(sort_order);

-- ---------------------------------------------------------------------------
-- Índices para service_types
-- ---------------------------------------------------------------------------
CREATE INDEX idx_service_types_category ON service_types(category_id);
CREATE INDEX idx_service_types_active ON service_types(is_active) WHERE is_active = TRUE;

-- ---------------------------------------------------------------------------
-- Índices para providers
-- ---------------------------------------------------------------------------
CREATE INDEX idx_providers_active ON providers(is_active) WHERE is_active = TRUE;

-- ---------------------------------------------------------------------------
-- Índices para provider_services
-- ---------------------------------------------------------------------------
CREATE INDEX idx_provider_services_provider ON provider_services(provider_id);
CREATE INDEX idx_provider_services_active ON provider_services(is_active) WHERE is_active = TRUE;

-- ---------------------------------------------------------------------------
-- Índices para services (críticos para el catálogo público)
-- ---------------------------------------------------------------------------
CREATE INDEX idx_services_category ON services(category_id);
CREATE INDEX idx_services_service_type ON services(service_type_id);
CREATE INDEX idx_services_provider_service ON services(provider_service_id);
CREATE INDEX idx_services_active ON services(is_active) WHERE is_active = TRUE;
-- Índice compuesto para filtrado del catálogo
CREATE INDEX idx_services_catalog ON services(category_id, service_type_id, is_active);
-- Índice compuesto para ordenamiento
CREATE INDEX idx_services_category_sort ON services(category_id, sort_order) WHERE is_active = TRUE;

-- ---------------------------------------------------------------------------
-- Índices para orders (tabla con mayor volumen esperado)
-- ---------------------------------------------------------------------------
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_service ON orders(service_id);
CREATE INDEX idx_orders_provider_service ON orders(provider_service_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at DESC);
-- Índice parcial para órdenes con provider_order_id (solo las procesadas)
CREATE INDEX idx_orders_provider_order ON orders(provider_order_id) 
    WHERE provider_order_id IS NOT NULL;
-- Índices compuestos para consultas frecuentes del dashboard
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at DESC);
-- Índice parcial para órdenes con refill pendiente
CREATE INDEX idx_orders_refill_deadline ON orders(refill_deadline) 
    WHERE is_refillable = TRUE AND refill_deadline IS NOT NULL AND status = 'completed';
-- Índice para reportes de profit
CREATE INDEX idx_orders_completed_at ON orders(completed_at DESC) 
    WHERE status = 'completed';

-- ---------------------------------------------------------------------------
-- Índices para order_refills
-- ---------------------------------------------------------------------------
CREATE INDEX idx_order_refills_order ON order_refills(order_id);
CREATE INDEX idx_order_refills_status ON order_refills(status);
CREATE INDEX idx_order_refills_pending ON order_refills(status) 
    WHERE status IN ('pending', 'processing');

-- ---------------------------------------------------------------------------
-- Índices para payment_processors
-- ---------------------------------------------------------------------------
CREATE INDEX idx_payment_processors_active ON payment_processors(is_active, sort_order) 
    WHERE is_active = TRUE;

-- ---------------------------------------------------------------------------
-- Índices para invoices
-- ---------------------------------------------------------------------------
CREATE INDEX idx_invoices_user ON invoices(user_id);
CREATE INDEX idx_invoices_processor ON invoices(processor_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_created ON invoices(created_at DESC);
-- Índice compuesto para historial de pagos del usuario
CREATE INDEX idx_invoices_user_status ON invoices(user_id, status);
CREATE INDEX idx_invoices_user_created ON invoices(user_id, created_at DESC);
-- Índice parcial para facturas pendientes
CREATE INDEX idx_invoices_pending ON invoices(status, created_at) 
    WHERE status = 'pending';

-- ---------------------------------------------------------------------------
-- Índices para transactions
-- ---------------------------------------------------------------------------
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_created ON transactions(created_at DESC);
-- Índice compuesto para historial del usuario
CREATE INDEX idx_transactions_user_created ON transactions(user_id, created_at DESC);
-- Índice para referencias (búsqueda por entidad relacionada)
CREATE INDEX idx_transactions_reference ON transactions(reference_type, reference_id) 
    WHERE reference_type IS NOT NULL;

-- ============================================================================
-- PASO 4: CREAR FUNCIÓN Y TRIGGERS PARA updated_at
-- ============================================================================

-- Función para actualizar automáticamente updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_updated_at_column() IS 
    'Actualiza automáticamente el campo updated_at en cada UPDATE';

-- Aplicar trigger a todas las tablas con updated_at
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_providers_updated_at
    BEFORE UPDATE ON providers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_services_updated_at
    BEFORE UPDATE ON services
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_invoices_updated_at
    BEFORE UPDATE ON invoices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- PASO 5: CREAR VISTAS ÚTILES (OPCIONAL)
-- ============================================================================

-- Vista: Resumen de servicios activos para el catálogo público
CREATE OR REPLACE VIEW v_active_services AS
SELECT 
    s.id,
    s.name,
    s.description,
    c.name AS category_name,
    c.slug AS category_slug,
    st.name AS service_type_name,
    st.slug AS service_type_slug,
    s.quality,
    s.speed,
    s.min_quantity,
    s.max_quantity,
    s.price_per_k,
    s.refill_days,
    s.average_time
FROM services s
INNER JOIN categories c ON s.category_id = c.id
INNER JOIN service_types st ON s.service_type_id = st.id
WHERE s.is_active = TRUE 
  AND c.is_active = TRUE 
  AND st.is_active = TRUE
ORDER BY c.sort_order, st.sort_order, s.sort_order;

COMMENT ON VIEW v_active_services IS 'Vista de servicios activos para el catálogo público';

-- Vista: Resumen de órdenes con información completa
CREATE OR REPLACE VIEW v_orders_summary AS
SELECT 
    o.id,
    o.user_id,
    u.email AS user_email,
    o.service_name,
    o.target,
    o.quantity,
    o.remains,
    o.status,
    o.total_charge,
    o.total_cost,
    o.profit,
    o.is_refillable,
    o.refill_deadline,
    o.created_at,
    o.completed_at
FROM orders o
INNER JOIN users u ON o.user_id = u.id
ORDER BY o.created_at DESC;

COMMENT ON VIEW v_orders_summary IS 'Vista de órdenes con información del usuario';

-- ============================================================================
-- VERIFICACIÓN FINAL
-- ============================================================================

-- Mostrar resumen de objetos creados
DO $$
DECLARE
    table_count INTEGER;
    index_count INTEGER;
    enum_count INTEGER;
    trigger_count INTEGER;
    view_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count 
    FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
    
    SELECT COUNT(*) INTO index_count 
    FROM pg_indexes 
    WHERE schemaname = 'public';
    
    SELECT COUNT(*) INTO enum_count 
    FROM pg_type 
    WHERE typtype = 'e' AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'public');
    
    SELECT COUNT(*) INTO trigger_count 
    FROM information_schema.triggers 
    WHERE trigger_schema = 'public';
    
    SELECT COUNT(*) INTO view_count 
    FROM information_schema.views 
    WHERE table_schema = 'public';
    
    RAISE NOTICE '============================================';
    RAISE NOTICE 'ANTIPANEL - Base de datos creada exitosamente';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Tablas creadas: %', table_count;
    RAISE NOTICE 'Índices creados: %', index_count;
    RAISE NOTICE 'ENUMs creados: %', enum_count;
    RAISE NOTICE 'Triggers creados: %', trigger_count;
    RAISE NOTICE 'Vistas creadas: %', view_count;
    RAISE NOTICE '============================================';
END $$;

-- ============================================================================
-- FIN DEL SCRIPT DE INICIALIZACIÓN
-- ============================================================================
