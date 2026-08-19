CREATE TABLE supervisors (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    manager_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_supervisors_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_supervisors_manager FOREIGN KEY (manager_id) REFERENCES managers (id),
    CONSTRAINT uq_supervisors_organization_code UNIQUE (organization_id, code)
);

CREATE INDEX idx_supervisors_organization_active ON supervisors (organization_id, active);
CREATE INDEX idx_supervisors_manager ON supervisors (manager_id);
