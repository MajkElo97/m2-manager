-- Dev seed: technical ADMIN organization for SUPER_ADMIN (system tenant).
-- Runs only when the dev/test Flyway location is enabled.

INSERT INTO organizations (id, name, slug, active, timezone, system_organization)
VALUES (
    '00000000-0000-4000-8000-000000000001',
    'ADMIN',
    'admin',
    TRUE,
    'Europe/Warsaw',
    TRUE
)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    slug = EXCLUDED.slug,
    system_organization = TRUE;

UPDATE organizations
SET system_organization = FALSE
WHERE id IN (
    'a0000000-0000-4000-8000-000000000001',
    'a0000000-0000-4000-8000-000000000002'
);

UPDATE users
SET organization_id = '00000000-0000-4000-8000-000000000001'
WHERE id = 'b0000000-0000-4000-8000-000000000001';

DELETE FROM user_roles
WHERE user_id = 'b0000000-0000-4000-8000-000000000001'
  AND role_id = 'b0000000-0000-4000-8000-000000000002';

UPDATE roles
SET organization_id = '00000000-0000-4000-8000-000000000001'
WHERE id = 'b0000000-0000-4000-8000-000000000002';

INSERT INTO user_roles (organization_id, user_id, role_id)
VALUES (
    '00000000-0000-4000-8000-000000000001',
    'b0000000-0000-4000-8000-000000000001',
    'b0000000-0000-4000-8000-000000000002'
)
ON CONFLICT (user_id, role_id) DO UPDATE SET organization_id = EXCLUDED.organization_id;

DELETE FROM user_organizations
WHERE user_id = 'b0000000-0000-4000-8000-000000000001';

INSERT INTO roles (id, organization_id, name, description, system_role, active)
VALUES
    (
        'c0000000-0000-4000-8000-000000000010',
        'a0000000-0000-4000-8000-000000000002',
        'ADMIN',
        'Administrator organizacji — secondary organization',
        FALSE,
        TRUE
    ),
    (
        'c0000000-0000-4000-8000-000000000012',
        'a0000000-0000-4000-8000-000000000002',
        'KOORDYNATOR',
        'Koordynator — secondary organization role',
        FALSE,
        TRUE
    ),
    (
        'c0000000-0000-4000-8000-000000000013',
        'a0000000-0000-4000-8000-000000000002',
        'PRACOWNIK',
        'Pracownik — secondary organization role',
        FALSE,
        TRUE
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'c0000000-0000-4000-8000-000000000010', p.id
FROM permissions p
WHERE p.module IN (
    'DASHBOARD', 'BUILDINGS', 'STAIRCASES', 'SCOPES', 'ACTIVITIES', 'EMPLOYEES',
    'MANAGERS', 'SUPERVISORS', 'CONTACTS', 'USERS', 'ROLES', 'SETTINGS',
    'FLEET', 'EQUIPMENT', 'CHEMICALS', 'FINANCE'
)
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'c0000000-0000-4000-8000-000000000012', p.id
FROM permissions p
WHERE (p.module = 'DASHBOARD' AND p.action = 'VIEW')
   OR (p.module = 'BUILDINGS' AND p.action = 'VIEW')
   OR (p.module = 'EMPLOYEES' AND p.action = 'VIEW')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'c0000000-0000-4000-8000-000000000013', p.id
FROM permissions p
WHERE p.module = 'DASHBOARD' AND p.action = 'VIEW'
ON CONFLICT (role_id, permission_id) DO NOTHING;
