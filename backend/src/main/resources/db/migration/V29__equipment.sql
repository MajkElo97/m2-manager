CREATE TABLE equipment (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    manufacturer VARCHAR(255),
    model VARCHAR(255),
    serial_number VARCHAR(100),
    quantity INTEGER NOT NULL DEFAULT 1,
    condition_status VARCHAR(20) NOT NULL DEFAULT 'GOOD',
    location VARCHAR(255),
    employee_id UUID,
    purchase_date DATE,
    purchase_value NUMERIC(12, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipment_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_equipment_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT uq_equipment_organization_code UNIQUE (organization_id, code),
    CONSTRAINT chk_equipment_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_equipment_condition CHECK (condition_status IN ('GOOD', 'USED', 'DAMAGED', 'OUT_OF_SERVICE'))
);

CREATE INDEX idx_equipment_organization_active ON equipment (organization_id, active);
CREATE INDEX idx_equipment_employee ON equipment (employee_id);
