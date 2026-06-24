-- Vehicle Inspection System - Initial Schema

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    mfa_secret      VARCHAR(64),
    mfa_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE customers (
    id              BIGSERIAL PRIMARY KEY,
    customer_code   VARCHAR(20) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    name_kana       VARCHAR(100),
    email           VARCHAR(255),
    phone           VARCHAR(20),
    postal_code     VARCHAR(10),
    address         VARCHAR(500),
    birth_date      DATE,
    notes           TEXT,
    consent_marketing BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      BIGINT REFERENCES users(id),
    updated_by      BIGINT REFERENCES users(id)
);

CREATE TABLE vehicles (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT NOT NULL REFERENCES customers(id),
    registration_number VARCHAR(20) NOT NULL,
    chassis_number      VARCHAR(17) NOT NULL,
    maker               VARCHAR(50),
    model               VARCHAR(100),
    model_year          INT,
    engine_displacement INT,
    fuel_type           VARCHAR(20),
    color               VARCHAR(30),
    first_registration  DATE,
    inspection_expiry   DATE NOT NULL,
    mileage             INT DEFAULT 0,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          BIGINT REFERENCES users(id),
    updated_by          BIGINT REFERENCES users(id),
    CONSTRAINT uq_vehicles_registration UNIQUE (registration_number),
    CONSTRAINT uq_vehicles_chassis UNIQUE (chassis_number)
);

CREATE INDEX idx_vehicles_expiry ON vehicles(inspection_expiry) WHERE active = TRUE;
CREATE INDEX idx_vehicles_customer ON vehicles(customer_id);

CREATE TABLE vehicle_inspections (
    id                  BIGSERIAL PRIMARY KEY,
    vehicle_id          BIGINT NOT NULL REFERENCES vehicles(id),
    inspection_type     VARCHAR(30) NOT NULL DEFAULT 'REGULAR',
    inspection_date     DATE NOT NULL,
    expiry_date         DATE NOT NULL,
    result              VARCHAR(20) NOT NULL DEFAULT 'PASS',
    inspector_name      VARCHAR(100),
    mileage_at_inspection INT,
    electronic_cert_id  VARCHAR(100),
    electronic_data     JSONB,
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          BIGINT REFERENCES users(id)
);

CREATE INDEX idx_vehicle_inspections_vehicle ON vehicle_inspections(vehicle_id);

CREATE TABLE inspection_reservations (
    id              BIGSERIAL PRIMARY KEY,
    vehicle_id      BIGINT NOT NULL REFERENCES vehicles(id),
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    reserved_at     TIMESTAMPTZ NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    service_type    VARCHAR(30) NOT NULL DEFAULT 'INSPECTION',
    assigned_user_id BIGINT REFERENCES users(id),
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reservations_date ON inspection_reservations(reserved_at);
CREATE INDEX idx_reservations_status ON inspection_reservations(status);

CREATE TABLE inspection_items (
    id              BIGSERIAL PRIMARY KEY,
    inspection_id   BIGINT NOT NULL REFERENCES vehicle_inspections(id) ON DELETE CASCADE,
    category        VARCHAR(50) NOT NULL,
    item_name       VARCHAR(100) NOT NULL,
    result          VARCHAR(20) NOT NULL DEFAULT 'OK',
    measured_value  VARCHAR(50),
    standard_value  VARCHAR(50),
    notes           TEXT
);

CREATE TABLE maintenance_records (
    id              BIGSERIAL PRIMARY KEY,
    vehicle_id      BIGINT NOT NULL REFERENCES vehicles(id),
    performed_at    TIMESTAMPTZ NOT NULL,
    work_type       VARCHAR(50) NOT NULL,
    description     TEXT NOT NULL,
    mileage         INT,
    technician_id   BIGINT REFERENCES users(id),
    parts_used      JSONB,
    labor_hours     DECIMAL(5,2),
    content_hash    VARCHAR(64) NOT NULL,
    previous_hash   VARCHAR(64),
    is_locked       BOOLEAN NOT NULL DEFAULT FALSE,
    locked_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      BIGINT REFERENCES users(id)
);

CREATE INDEX idx_maintenance_vehicle ON maintenance_records(vehicle_id);

CREATE TABLE parts (
    id              BIGSERIAL PRIMARY KEY,
    part_code       VARCHAR(30) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    category        VARCHAR(50),
    unit_price      DECIMAL(12,2) NOT NULL DEFAULT 0,
    stock_quantity  INT NOT NULL DEFAULT 0,
    supplier        VARCHAR(100),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE estimates (
    id              BIGSERIAL PRIMARY KEY,
    estimate_number VARCHAR(20) NOT NULL UNIQUE,
    vehicle_id      BIGINT NOT NULL REFERENCES vehicles(id),
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    subtotal        DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount      DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount    DECIMAL(12,2) NOT NULL DEFAULT 0,
    valid_until     DATE,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      BIGINT REFERENCES users(id)
);

CREATE TABLE estimate_items (
    id              BIGSERIAL PRIMARY KEY,
    estimate_id     BIGINT NOT NULL REFERENCES estimates(id) ON DELETE CASCADE,
    item_type       VARCHAR(20) NOT NULL DEFAULT 'LABOR',
    description     VARCHAR(255) NOT NULL,
    quantity        DECIMAL(10,2) NOT NULL DEFAULT 1,
    unit_price      DECIMAL(12,2) NOT NULL DEFAULT 0,
    amount          DECIMAL(12,2) NOT NULL DEFAULT 0,
    part_id         BIGINT REFERENCES parts(id)
);

CREATE TABLE invoices (
    id              BIGSERIAL PRIMARY KEY,
    invoice_number  VARCHAR(20) NOT NULL UNIQUE,
    estimate_id     BIGINT REFERENCES estimates(id),
    vehicle_id      BIGINT NOT NULL REFERENCES vehicles(id),
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    subtotal        DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount      DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount    DECIMAL(12,2) NOT NULL DEFAULT 0,
    issued_at       DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date        DATE,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      BIGINT REFERENCES users(id)
);

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    invoice_id      BIGINT NOT NULL REFERENCES invoices(id),
    amount          DECIMAL(12,2) NOT NULL,
    payment_method  VARCHAR(30) NOT NULL DEFAULT 'CASH',
    paid_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reference       VARCHAR(100),
    created_by      BIGINT REFERENCES users(id)
);

CREATE TABLE documents (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(30) NOT NULL,
    entity_id       BIGINT NOT NULL,
    document_type   VARCHAR(30) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    storage_key     VARCHAR(500) NOT NULL,
    content_type    VARCHAR(100),
    file_size       BIGINT,
    checksum_sha256 VARCHAR(64),
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    uploaded_by     BIGINT REFERENCES users(id)
);

CREATE INDEX idx_documents_entity ON documents(entity_type, entity_id);

CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT REFERENCES customers(id),
    vehicle_id      BIGINT REFERENCES vehicles(id),
    channel         VARCHAR(20) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    recipient       VARCHAR(255) NOT NULL,
    subject         VARCHAR(255),
    body            TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    scheduled_at    TIMESTAMPTZ,
    sent_at         TIMESTAMPTZ,
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_status ON notifications(status);

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id),
    action          VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       BIGINT,
    old_value       JSONB,
    new_value       JSONB,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);

-- Seed roles
INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'システム管理者'),
    ('MANAGER', '店舗管理者'),
    ('INSPECTOR', '検査員'),
    ('MECHANIC', '整備士'),
    ('RECEPTION', '受付');
