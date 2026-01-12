-- Migration V2: Add version column for optimistic locking on users table
-- Purpose: Prevent race conditions in concurrent balance updates (double-spending)

-- Add version column with default value of 0
ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Add comment for documentation
COMMENT ON COLUMN users.version IS 'Version for optimistic locking - prevents concurrent balance modification race conditions';
