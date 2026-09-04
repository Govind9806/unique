-- Flyway Migration V3: Add provider-independent payment fields

ALTER TABLE payments ADD COLUMN IF NOT EXISTS provider VARCHAR(50) DEFAULT 'DIRECT_UPI';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS failure_reason TEXT;

CREATE INDEX IF NOT EXISTS idx_payments_provider ON payments(provider);
