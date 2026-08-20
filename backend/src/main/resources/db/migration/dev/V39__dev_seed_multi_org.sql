-- Dev seed: secondary organization and multi-organization test user.
-- Runs only when the dev/test Flyway location is enabled.

INSERT INTO organizations (id, name, slug, nip, email, phone, active, timezone)
VALUES (
    'a0000000-0000-4000-8000-000000000002',
    'M2 Manager Dev Secondary',
    'm2-manager-dev-secondary',
    '9876543210',
    'secondary@m2manager.local',
    '+48987654321',
    TRUE,
    'Europe/Warsaw'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (
    id,
    organization_id,
    email,
    password_hash,
    first_name,
    last_name,
    active
)
VALUES (
    'c0000000-0000-4000-8000-000000000001',
    'a0000000-0000-4000-8000-000000000001',
    'multiadmin@m2manager.local',
    '$2a$12$Jt8iSmQesv59..E7KFXyI.pn/M9HZkEQD0j1uBZMvM.bV9ni4jeju',
    'Michał',
    'Ociepka',
    TRUE
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_organizations (user_id, organization_id)
VALUES
    ('c0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001'),
    ('c0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000002')
ON CONFLICT DO NOTHING;

INSERT INTO roles (id, organization_id, name, description, system_role, active)
VALUES (
    'c0000000-0000-4000-8000-000000000002',
    'a0000000-0000-4000-8000-000000000002',
    'BIURO',
    'Biuro — secondary organization role',
    FALSE,
    TRUE
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'c0000000-0000-4000-8000-000000000002', p.id
FROM permissions p
WHERE (p.module = 'DASHBOARD' AND p.action IN ('VIEW', 'CREATE', 'EDIT', 'DELETE'))
   OR (p.module = 'BUILDINGS' AND p.action IN ('VIEW', 'CREATE', 'EDIT', 'DELETE'))
   OR (p.module = 'EMPLOYEES' AND p.action IN ('VIEW', 'CREATE', 'EDIT', 'DELETE'))
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO user_roles (organization_id, user_id, role_id)
VALUES
    (
        'a0000000-0000-4000-8000-000000000001',
        'c0000000-0000-4000-8000-000000000001',
        'b0000000-0000-4000-8000-000000000010'
    ),
    (
        'a0000000-0000-4000-8000-000000000002',
        'c0000000-0000-4000-8000-000000000001',
        'c0000000-0000-4000-8000-000000000002'
    )
ON CONFLICT (user_id, role_id) DO NOTHING;
