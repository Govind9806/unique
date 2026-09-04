-- Flyway Migration V4: Balance Adjustment Audit Logs Table

CREATE TABLE IF NOT EXISTS balance_adjustment_logs (
    id VARCHAR(36) PRIMARY KEY,
    previous_balance DECIMAL(12,2) NOT NULL,
    new_balance DECIMAL(12,2) NOT NULL,
    difference DECIMAL(12,2) NOT NULL,
    reason TEXT NOT NULL,
    modified_by VARCHAR(36) NOT NULL REFERENCES users(id),
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
