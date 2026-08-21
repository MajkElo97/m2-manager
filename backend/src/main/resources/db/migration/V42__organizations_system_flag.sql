-- Mark technical vs business organizations (V42)

ALTER TABLE organizations ADD COLUMN system_organization BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_organizations_system_organization ON organizations (system_organization);
