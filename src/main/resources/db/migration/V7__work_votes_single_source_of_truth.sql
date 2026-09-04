-- Flyway Migration V7: Create work_votes table as single authoritative source of truth for repair voting
CREATE TABLE IF NOT EXISTS work_votes (
    id VARCHAR(36) PRIMARY KEY,
    work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE,
    flat_number VARCHAR(50) NOT NULL,
    vote VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    voted_by_user_id VARCHAR(36) REFERENCES users(id),
    voted_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_work_vote_flat UNIQUE (work_id, flat_number)
);

CREATE INDEX IF NOT EXISTS idx_work_votes_work_id ON work_votes(work_id);
CREATE INDEX IF NOT EXISTS idx_work_votes_flat_number ON work_votes(flat_number);
