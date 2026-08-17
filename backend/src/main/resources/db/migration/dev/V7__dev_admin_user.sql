-- Dev seed: super administrator for local development and tests.
-- Runs only when the dev/test Flyway location is enabled.

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
    'b0000000-0000-4000-8000-000000000001',
    'a0000000-0000-4000-8000-000000000001',
    'admin@m2manager.local',
    '$2a$12$Jt8iSmQesv59..E7KFXyI.pn/M9HZkEQD0j1uBZMvM.bV9ni4jeju',
    'Admin',
    'M2',
    TRUE
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO roles (
    id,
    organization_id,
    name,
    description,
    system_role,
    active
)
VALUES (
    'b0000000-0000-4000-8000-000000000002',
    'a0000000-0000-4000-8000-000000000001',
    'SUPER_ADMIN',
    'Development super administrator',
    TRUE,
    TRUE
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (organization_id, user_id, role_id)
VALUES (
    'a0000000-0000-4000-8000-000000000001',
    'b0000000-0000-4000-8000-000000000001',
    'b0000000-0000-4000-8000-000000000002'
)
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000002', p.id
FROM permissions p
ON CONFLICT (role_id, permission_id) DO NOTHING;
