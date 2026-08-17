-- Organization slug for tenant login identification (V5)

ALTER TABLE organizations ADD COLUMN slug VARCHAR(100);

UPDATE organizations
SET slug = 'm2-manager-dev'
WHERE id = 'a0000000-0000-4000-8000-000000000001';

ALTER TABLE organizations ALTER COLUMN slug SET NOT NULL;

ALTER TABLE organizations ADD CONSTRAINT uq_organizations_slug UNIQUE (slug);
