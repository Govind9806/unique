-- Aproova Residency Initial Schema V1

CREATE TABLE IF NOT EXISTS apartments (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    total_flats INT NOT NULL DEFAULT 16,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    opening_balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flats (
    id VARCHAR(36) PRIMARY KEY,
    flat_number VARCHAR(20) NOT NULL UNIQUE,
    floor INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OCCUPIED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    flat_id VARCHAR(36) REFERENCES flats(id) ON DELETE SET NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'RESIDENT',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    language VARCHAR(10) NOT NULL DEFAULT 'EN',
    fcm_token TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS responsibilities (
    id VARCHAR(36) PRIMARY KEY,
    responsibility_type VARCHAR(50) NOT NULL,
    flat_id VARCHAR(36) NOT NULL REFERENCES flats(id),
    assigned_user_id VARCHAR(36) REFERENCES users(id),
    start_date DATE NOT NULL,
    end_date DATE,
    assigned_by_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS maintenance_bills (
    id VARCHAR(36) PRIMARY KEY,
    flat_id VARCHAR(36) NOT NULL REFERENCES flats(id),
    billing_month INT NOT NULL,
    billing_year INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_flat_month_year UNIQUE (flat_id, billing_month, billing_year)
);

CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(36) PRIMARY KEY,
    bill_id VARCHAR(36) REFERENCES maintenance_bills(id),
    flat_id VARCHAR(36) NOT NULL REFERENCES flats(id),
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    gateway_order_id VARCHAR(255),
    gateway_payment_id VARCHAR(255),
    transaction_reference VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS receipts (
    id VARCHAR(36) PRIMARY KEY,
    receipt_number VARCHAR(100) NOT NULL UNIQUE,
    payment_id VARCHAR(36) NOT NULL REFERENCES payments(id),
    flat_id VARCHAR(36) NOT NULL REFERENCES flats(id),
    resident_name VARCHAR(255) NOT NULL,
    bill_period VARCHAR(100) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    paid_date TIMESTAMP NOT NULL,
    pdf_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS water_meters (
    id VARCHAR(36) PRIMARY KEY,
    flat_id VARCHAR(36) NOT NULL UNIQUE REFERENCES flats(id),
    meter_number VARCHAR(100) NOT NULL UNIQUE,
    installation_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS water_readings (
    id VARCHAR(36) PRIMARY KEY,
    meter_id VARCHAR(36) NOT NULL REFERENCES water_meters(id),
    flat_id VARCHAR(36) NOT NULL REFERENCES flats(id),
    previous_reading DECIMAL(10,2) NOT NULL,
    current_reading DECIMAL(10,2) NOT NULL,
    consumption DECIMAL(10,2) NOT NULL,
    reading_date DATE NOT NULL,
    recorded_by_user_id VARCHAR(36) REFERENCES users(id),
    meter_photo_document_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS water_bills (
    id VARCHAR(36) PRIMARY KEY,
    flat_id VARCHAR(36) NOT NULL REFERENCES flats(id),
    reading_id VARCHAR(36) REFERENCES water_readings(id),
    previous_reading DECIMAL(10,2) NOT NULL,
    current_reading DECIMAL(10,2) NOT NULL,
    consumption DECIMAL(10,2) NOT NULL,
    rate_per_unit DECIMAL(10,2) NOT NULL,
    fixed_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(12,2) NOT NULL,
    billing_period VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS water_tankers (
    id VARCHAR(36) PRIMARY KEY,
    supplier VARCHAR(255) NOT NULL,
    tanker_date DATE NOT NULL,
    quantity_liters INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    reason TEXT,
    invoice_document_id VARCHAR(36),
    recorded_by_user_id VARCHAR(36) REFERENCES users(id),
    approval_status VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS works (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    responsible_flat_id VARCHAR(36) REFERENCES flats(id),
    responsible_user_id VARCHAR(36) REFERENCES users(id),
    vendor_name VARCHAR(255),
    estimated_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    actual_cost DECIMAL(12,2) DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    start_date DATE,
    completion_date DATE,
    created_by_user_id VARCHAR(36) REFERENCES users(id),
    approved_by_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS work_timelines (
    id VARCHAR(36) PRIMARY KEY,
    work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    description TEXT,
    created_by_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expenses (
    id VARCHAR(36) PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    amount DECIMAL(12,2) NOT NULL,
    expense_date DATE NOT NULL,
    vendor VARCHAR(255),
    related_work_id VARCHAR(36) REFERENCES works(id),
    created_by_user_id VARCHAR(36) REFERENCES users(id),
    responsible_flat_id VARCHAR(36) REFERENCES flats(id),
    approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    approved_by_user_id VARCHAR(36) REFERENCES users(id),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS documents (
    id VARCHAR(36) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_path TEXT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id VARCHAR(36),
    uploaded_by_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ledger_transactions (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    description TEXT,
    reference_type VARCHAR(50),
    reference_id VARCHAR(36),
    transaction_date TIMESTAMP NOT NULL,
    created_by_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS electricity_bills (
    id VARCHAR(36) PRIMARY KEY,
    billing_month VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    document_id VARCHAR(36),
    created_by_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    joining_date DATE NOT NULL,
    monthly_salary DECIMAL(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS salary_records (
    id VARCHAR(36) PRIMARY KEY,
    employee_id VARCHAR(36) NOT NULL REFERENCES employees(id),
    month_year VARCHAR(50) NOT NULL,
    base_salary DECIMAL(12,2) NOT NULL,
    advance DECIMAL(12,2) DEFAULT 0.00,
    deduction DECIMAL(12,2) DEFAULT 0.00,
    bonus DECIMAL(12,2) DEFAULT 0.00,
    final_amount DECIMAL(12,2) NOT NULL,
    payment_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    proof_document_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS meetings (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    meeting_date DATE NOT NULL,
    meeting_time VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    agenda TEXT,
    minutes_decisions TEXT,
    minutes_notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    created_by_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notices (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    expiry_date DATE,
    created_by_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    reference_type VARCHAR(50),
    reference_id VARCHAR(36),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS apartment_histories (
    id VARCHAR(36) PRIMARY KEY,
    event_category VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    reference_type VARCHAR(50),
    reference_id VARCHAR(36),
    created_by_user_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(36),
    old_value TEXT,
    new_value TEXT,
    reason TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS apartment_settings (
    id VARCHAR(36) PRIMARY KEY,
    maintenance_amount DECIMAL(12,2) NOT NULL DEFAULT 2000.00,
    water_rate_per_unit DECIMAL(10,2) NOT NULL DEFAULT 40.00,
    due_day_of_month INT NOT NULL DEFAULT 10,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS app_versions (
    id VARCHAR(36) PRIMARY KEY,
    latest_version VARCHAR(20) NOT NULL,
    minimum_supported_version VARCHAR(20) NOT NULL,
    apk_url TEXT NOT NULL,
    release_notes TEXT,
    force_update BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for optimal performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_flats_number ON flats(flat_number);
CREATE INDEX IF NOT EXISTS idx_bills_status ON maintenance_bills(status);
CREATE INDEX IF NOT EXISTS idx_bills_flat_month ON maintenance_bills(flat_id, billing_month, billing_year);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_ref ON payments(transaction_reference);
CREATE INDEX IF NOT EXISTS idx_ledger_date ON ledger_transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_ledger_type ON ledger_transactions(type);
CREATE INDEX IF NOT EXISTS idx_expenses_status ON expenses(approval_status);
CREATE INDEX IF NOT EXISTS idx_water_readings_date ON water_readings(reading_date);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_histories_created ON apartment_histories(created_at);
