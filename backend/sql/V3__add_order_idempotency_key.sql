-- Migration V3: Add idempotency_key column to orders table
-- Purpose: Prevent duplicate order submissions (double-click, network retry)

-- Add idempotency_key column (nullable, unique)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(64);

-- Create unique index for idempotency key (allows NULL values)
CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_idempotency_key
    ON orders (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

-- Add comment for documentation
COMMENT ON COLUMN orders.idempotency_key IS 'Client-generated UUID for idempotent order creation - prevents duplicate submissions';
