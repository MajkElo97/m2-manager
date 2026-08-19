CREATE TABLE activity_scopes (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    building_id UUID NOT NULL,
    activity_id UUID NOT NULL,
    planning_type VARCHAR(20) NOT NULL,
    frequency INTEGER,
    weekdays VARCHAR(255),
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_scopes_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_activity_scopes_building FOREIGN KEY (building_id, organization_id) REFERENCES buildings (id, organization_id),
    CONSTRAINT fk_activity_scopes_activity FOREIGN KEY (activity_id) REFERENCES activities (id),
    CONSTRAINT uq_activity_scopes_organization_code UNIQUE (organization_id, code),
    CONSTRAINT chk_activity_scopes_planning_type CHECK (planning_type IN ('WEEKLY', 'MONTHLY', 'YEARLY', 'EVENT')),
    CONSTRAINT chk_activity_scopes_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_activity_scopes_frequency CHECK (frequency IS NULL OR frequency >= 0)
);

CREATE INDEX idx_activity_scopes_organization_building ON activity_scopes (organization_id, building_id);
CREATE INDEX idx_activity_scopes_activity ON activity_scopes (activity_id);
CREATE INDEX idx_activity_scopes_organization_status ON activity_scopes (organization_id, status);
