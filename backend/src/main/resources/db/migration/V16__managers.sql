CREATE TABLE managers (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    address VARCHAR(500),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_managers_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT uq_managers_organization_code UNIQUE (organization_id, code),
    CONSTRAINT uq_managers_organization_name UNIQUE (organization_id, name)
);

CREATE INDEX idx_managers_organization_active ON managers (organization_id, active);
