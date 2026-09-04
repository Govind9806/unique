-- Migration V5: Add is_emergency column and voting tables to expenses
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS is_emergency BOOLEAN DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS expense_approval_flats (
    expense_id VARCHAR(36) NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    flat_number VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS expense_rejection_flats (
    expense_id VARCHAR(36) NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    flat_number VARCHAR(50) NOT NULL
);
