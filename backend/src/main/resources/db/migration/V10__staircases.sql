-- Composite unique key required for tenant-safe FK from staircases to buildings.
ALTER TABLE buildings
    ADD CONSTRAINT uq_buildings_id_organization UNIQUE (id, organization_id);

CREATE TABLE staircases (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    building_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    designation VARCHAR(50) NOT NULL,
    intercom_code VARCHAR(255),
    key_required BOOLEAN NOT NULL DEFAULT FALSE,
    elevator BOOLEAN NOT NULL DEFAULT FALSE,
    floors INTEGER NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staircases_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_staircases_building FOREIGN KEY (building_id, organization_id) REFERENCES buildings (id, organization_id),
    CONSTRAINT uq_staircases_organization_code UNIQUE (organization_id, code),
    CONSTRAINT uq_staircases_building_designation UNIQUE (building_id, designation),
    CONSTRAINT chk_staircases_floors CHECK (floors >= 0)
);

CREATE INDEX idx_staircases_organization_building ON staircases (organization_id, building_id);
