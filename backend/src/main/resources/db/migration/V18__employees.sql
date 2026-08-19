CREATE TABLE employees (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(255),
    google_email VARCHAR(255),
    position VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    employment_type VARCHAR(20),
    employment_start_date DATE,
    remuneration_amount NUMERIC(10, 2),
    remuneration_unit VARCHAR(20),
    remuneration_net BOOLEAN,
    calendar_color VARCHAR(7),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employees_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT uq_employees_organization_code UNIQUE (organization_id, code),
    CONSTRAINT chk_employees_role CHECK (role IN ('PRACOWNIK', 'ADMIN')),
    CONSTRAINT chk_employees_employment_type CHECK (employment_type IS NULL OR employment_type IN ('ZLECENIE')),
    CONSTRAINT chk_employees_remuneration_unit CHECK (remuneration_unit IS NULL OR remuneration_unit IN ('HOURLY'))
);

CREATE INDEX idx_employees_organization_active ON employees (organization_id, active);
CREATE INDEX idx_employees_organization_role ON employees (organization_id, role);
