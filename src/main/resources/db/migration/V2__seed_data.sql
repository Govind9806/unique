-- Aproova Residency Clean Initial Seed Data V2 (Single Admin User Only)

-- 1. Apartment Configuration
INSERT INTO apartments (id, name, address, total_flats, currency, timezone, opening_balance, created_at, updated_at)
VALUES ('apt-aproova-001', 'Aproova Residency', 'Plot 42, Road No 5, Jubilee Hills, Hyderabad, Telangana 500033', 16, 'INR', 'Asia/Kolkata', 50000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Apartment Settings
INSERT INTO apartment_settings (id, maintenance_amount, water_rate_per_unit, due_day_of_month, currency, timezone, notification_enabled, created_at, updated_at)
VALUES ('settings-aproova-001', 2000.00, 40.00, 10, 'INR', 'Asia/Kolkata', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. App Version
INSERT INTO app_versions (id, latest_version, minimum_supported_version, apk_url, release_notes, force_update, created_at, updated_at)
VALUES ('ver-001', '1.0.0', '1.0.0', 'https://aproova.app/downloads/aproova-residency-v1.0.0.apk', 'Initial release of Aproova Residency mobile application.', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. 16 Flats Structure: 001-004 (Ground), 101-104 (1st), 201-204 (2nd), 301-304 (3rd)
INSERT INTO flats (id, flat_number, floor, status, created_at, updated_at) VALUES
('flat-001', '001', 0, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-002', '002', 0, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-003', '003', 0, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-004', '004', 0, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-101', '101', 1, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-102', '102', 1, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-103', '103', 1, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-104', '104', 1, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-201', '201', 2, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-202', '202', 2, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-203', '203', 2, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-204', '204', 2, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-301', '301', 3, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-302', '302', 3, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-303', '303', 3, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('flat-304', '304', 3, 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 5. ONLY 1 SINGLE ADMIN USER SEEDED (Phone: 7702444411, Pass: 78466106)
-- BCrypt hash for '78466106': $2a$10$dYk77P37NAXJ7Hz0/DZU5OdXNgcUE84YKe3QVH.8U9nFNgZRKwKwm
INSERT INTO users (id, name, email, phone, password, flat_id, role, status, language, created_at, updated_at) VALUES
('user-admin', 'Admin', 'admin@aproova.com', '7702444411', '$2a$10$dYk77P37NAXJ7Hz0/DZU5OdXNgcUE84YKe3QVH.8U9nFNgZRKwKwm', 'flat-101', 'ADMIN', 'ACTIVE', 'EN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
