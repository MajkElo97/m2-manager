CREATE TABLE chemicals (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL DEFAULT 0,
    unit VARCHAR(20) NOT NULL,
    minimum_stock NUMERIC(12, 3),
    location VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chemicals_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT uq_chemicals_organization_code UNIQUE (organization_id, code),
    CONSTRAINT chk_chemicals_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_chemicals_minimum_stock CHECK (minimum_stock IS NULL OR minimum_stock >= 0),
    CONSTRAINT chk_chemicals_unit CHECK (unit IN ('LITER', 'KILOGRAM', 'PIECE', 'PACK', 'OTHER'))
);

CREATE INDEX idx_chemicals_organization_active ON chemicals (organization_id, active);
