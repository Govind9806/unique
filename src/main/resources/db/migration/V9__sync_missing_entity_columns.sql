-- Migration V9: Add missing entity columns and collection tables for expenses, ledger, water readings, and works

ALTER TABLE expenses ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS receipt_photo_document_id VARCHAR(255);

ALTER TABLE ledger_transactions ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE ledger_transactions ADD COLUMN IF NOT EXISTS receipt_photo_document_id VARCHAR(255);
ALTER TABLE ledger_transactions ADD COLUMN IF NOT EXISTS vendor VARCHAR(255);

ALTER TABLE water_readings ADD COLUMN IF NOT EXISTS reading_type VARCHAR(255) DEFAULT 'REGULAR';
ALTER TABLE water_readings ADD COLUMN IF NOT EXISTS meter_photo_document_id VARCHAR(255);

ALTER TABLE works ADD COLUMN IF NOT EXISTS maintenance_decision VARCHAR(255);
ALTER TABLE works ADD COLUMN IF NOT EXISTS maintenance_message VARCHAR(500);
ALTER TABLE works ADD COLUMN IF NOT EXISTS maintenance_responded_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS work_photos (
    work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE,
    photo_url VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS work_vote_reasons (
    work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE,
    flat_number VARCHAR(50) NOT NULL,
    reason VARCHAR(500)
);
