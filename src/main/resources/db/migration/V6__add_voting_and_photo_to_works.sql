-- Migration V6: Add photo_url and voting tables to works
ALTER TABLE works ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);

CREATE TABLE IF NOT EXISTS work_approval_flats (
    work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE,
    flat_number VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS work_rejection_flats (
    work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE,
    flat_number VARCHAR(50) NOT NULL
);
