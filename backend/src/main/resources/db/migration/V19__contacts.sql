CREATE TABLE contacts (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    building_id UUID NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    function_title VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(255),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contacts_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_contacts_building FOREIGN KEY (building_id) REFERENCES buildings (id)
);

CREATE INDEX idx_contacts_organization_active ON contacts (organization_id, active);
CREATE INDEX idx_contacts_building ON contacts (building_id);
