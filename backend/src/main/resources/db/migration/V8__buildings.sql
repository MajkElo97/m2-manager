CREATE TABLE buildings (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    nip VARCHAR(20),
    phone VARCHAR(50),
    email VARCHAR(255),
    manager_code VARCHAR(50),
    supervisor_code VARCHAR(50),
    employee_code VARCHAR(50),
    contract_signed_at DATE,
    service_start_date DATE,
    notice_period_months INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_buildings_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT uq_buildings_organization_code UNIQUE (organization_id, code),
    CONSTRAINT chk_buildings_notice_period_months CHECK (notice_period_months >= 0 AND notice_period_months <= 120),
    CONSTRAINT chk_buildings_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_buildings_organization_status ON buildings (organization_id, status);
