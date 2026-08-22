-- Global unique organization name (V45)

ALTER TABLE organizations ADD CONSTRAINT uq_organizations_name UNIQUE (name);
