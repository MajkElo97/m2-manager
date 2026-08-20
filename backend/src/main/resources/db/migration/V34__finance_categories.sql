CREATE TABLE financial_categories (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_financial_categories_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT uq_financial_categories_organization_code UNIQUE (organization_id, code),
    CONSTRAINT chk_financial_categories_type CHECK (type IN ('INCOME', 'EXPENSE'))
);

CREATE INDEX idx_financial_categories_organization_type ON financial_categories (organization_id, type, active);
