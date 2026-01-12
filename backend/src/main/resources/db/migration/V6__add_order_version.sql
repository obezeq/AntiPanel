-- Migration V6: Add version column to orders table for optimistic locking
-- Purpose: Prevent concurrent modification issues (lost updates)

-- Add version column for optimistic locking (idempotent)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Add comment for documentation
COMMENT ON COLUMN orders.version IS 'Optimistic locking version - automatically incremented on each update';
