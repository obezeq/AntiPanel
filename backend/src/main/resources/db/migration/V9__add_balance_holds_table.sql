-- V9__add_balance_holds_table.sql
-- Creates the balance_holds table for the Balance Reservation Pattern
-- This table is used to temporarily reserve funds before order submission (ACID compliance)

CREATE TABLE IF NOT EXISTS balance_holds (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    amount NUMERIC(12, 4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'HELD',
    idempotency_key VARCHAR(64) UNIQUE,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    expires_at TIMESTAMP NOT NULL,
    release_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_balance_holds_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT chk_balance_holds_amount
        CHECK (amount > 0),

    CONSTRAINT chk_balance_holds_status
        CHECK (status IN ('HELD', 'CAPTURED', 'RELEASED', 'EXPIRED'))
);

-- Indexes for performance (idempotent)
CREATE INDEX IF NOT EXISTS idx_balance_holds_user_status ON balance_holds(user_id, status);
CREATE INDEX IF NOT EXISTS idx_balance_holds_idempotency ON balance_holds(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_balance_holds_expires ON balance_holds(expires_at);

COMMENT ON TABLE balance_holds IS 'Balance reservations for order creation (ACID compliance)';
