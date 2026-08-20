CREATE TABLE financial_transactions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    transaction_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    net_amount NUMERIC(14, 2) NOT NULL,
    vat_rate NUMERIC(5, 2),
    vat_amount NUMERIC(14, 2),
    gross_amount NUMERIC(14, 2) NOT NULL,
    category_id UUID NOT NULL,
    contractor_name VARCHAR(255),
    contractor_nip VARCHAR(20),
    building_id UUID,
    employee_id UUID,
    vehicle_id UUID,
    equipment_id UUID,
    chemical_id UUID,
    description TEXT,
    document_number VARCHAR(100),
    due_date DATE,
    payment_date DATE,
    payment_status VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_financial_transactions_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_financial_transactions_category FOREIGN KEY (category_id) REFERENCES financial_categories (id),
    CONSTRAINT fk_financial_transactions_building FOREIGN KEY (building_id) REFERENCES buildings (id),
    CONSTRAINT fk_financial_transactions_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_financial_transactions_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
    CONSTRAINT fk_financial_transactions_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT fk_financial_transactions_chemical FOREIGN KEY (chemical_id) REFERENCES chemicals (id),
    CONSTRAINT uq_financial_transactions_organization_code UNIQUE (organization_id, code),
    CONSTRAINT chk_financial_transactions_type CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_financial_transactions_status CHECK (status IN ('ACTIVE', 'CANCELLED')),
    CONSTRAINT chk_financial_transactions_payment_status CHECK (payment_status IN ('NOT_APPLICABLE', 'TO_PAY', 'PAID', 'OVERDUE')),
    CONSTRAINT chk_financial_transactions_net_amount CHECK (net_amount >= 0),
    CONSTRAINT chk_financial_transactions_gross_amount CHECK (gross_amount >= 0)
);

CREATE INDEX idx_financial_transactions_organization_date ON financial_transactions (organization_id, transaction_date);
CREATE INDEX idx_financial_transactions_organization_type ON financial_transactions (organization_id, type);
CREATE INDEX idx_financial_transactions_organization_status ON financial_transactions (organization_id, status);
CREATE INDEX idx_financial_transactions_payment_status ON financial_transactions (organization_id, payment_status);
CREATE INDEX idx_financial_transactions_category ON financial_transactions (category_id);
CREATE INDEX idx_financial_transactions_building ON financial_transactions (building_id);
