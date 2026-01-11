-- Migration V8: Add version column to invoices table for optimistic locking
-- Purpose: Prevent race condition where scheduler and frontend poll simultaneously causing double credit

-- Add version column for optimistic locking
ALTER TABLE invoices ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Add comment for documentation
COMMENT ON COLUMN invoices.version IS 'Optimistic locking version - prevents concurrent payment completion race conditions';
