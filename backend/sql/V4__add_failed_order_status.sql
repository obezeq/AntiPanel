-- Migration V4: Add FAILED status to order_status_enum
-- Purpose: Track orders that failed during provider submission

-- Add 'failed' value to order_status_enum
ALTER TYPE order_status_enum ADD VALUE IF NOT EXISTS 'failed';

-- Add comment for documentation
COMMENT ON TYPE order_status_enum IS 'Order lifecycle states: pending, processing, in_progress, completed, partial, cancelled, refunded, failed';
