-- Migration V5: Add version column to orders table for optimistic locking
-- Purpose: Prevent concurrent modification issues (lost updates)

-- Add version column for optimistic locking
ALTER TABLE orders ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Add comment for documentation
COMMENT ON COLUMN orders.version IS 'Optimistic locking version - automatically incremented on each update';
