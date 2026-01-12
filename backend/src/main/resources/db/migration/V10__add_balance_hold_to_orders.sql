-- Migration V10: Add balance_hold_id column to orders table
-- Purpose: Link orders to their balance holds for the Balance Reservation Pattern
-- Nullable: Legacy orders created before this feature won't have a hold

ALTER TABLE orders ADD COLUMN IF NOT EXISTS balance_hold_id BIGINT;

-- Note: No foreign key constraint added because:
-- 1. It's just a reference ID (not a full relationship)
-- 2. Legacy orders have NULL values
-- 3. Keeps the migration simple and reversible
