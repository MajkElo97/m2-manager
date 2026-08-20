-- Organization-scoped activities alongside global system catalog (V41)

ALTER TABLE activities ADD COLUMN organization_id UUID;

ALTER TABLE activities ADD CONSTRAINT fk_activities_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id);

CREATE INDEX idx_activities_organization_id ON activities (organization_id);
CREATE INDEX idx_activities_organization_active ON activities (organization_id, active);
