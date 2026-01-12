-- Migration V11: Add FAILED (uppercase) to order_status_enum
-- Purpose: Fix case mismatch - V4 added 'failed' (lowercase) but Java uses 'FAILED' (uppercase)
-- Note: PostgreSQL enums are case-sensitive

-- Add the correctly-cased value
-- IF NOT EXISTS ensures idempotency (safe to run multiple times)
ALTER TYPE order_status_enum ADD VALUE IF NOT EXISTS 'FAILED';

-- The lowercase 'failed' from V4 will remain in the enum but won't be used
-- Java's @Enumerated(EnumType.STRING) stores the constant name (FAILED), not the value
