-- Dev seed: business roles with permission mappings (idempotent).
-- SUPER_ADMIN remains unchanged from V7.

INSERT INTO roles (id, organization_id, name, description, system_role, active)
VALUES
    (
        'b0000000-0000-4000-8000-000000000010',
        'a0000000-0000-4000-8000-000000000001',
        'ADMIN',
        'Administrator organizacji — pełny dostęp do modułów biznesowych',
        FALSE,
        TRUE
    ),
    (
        'b0000000-0000-4000-8000-000000000011',
        'a0000000-0000-4000-8000-000000000001',
        'BIURO',
        'Biuro — dashboard, budynki, pracownicy (EDIT)',
        FALSE,
        TRUE
    ),
    (
        'b0000000-0000-4000-8000-000000000012',
        'a0000000-0000-4000-8000-000000000001',
        'KOORDYNATOR',
        'Koordynator — dashboard, budynki, pracownicy (READ)',
        FALSE,
        TRUE
    ),
    (
        'b0000000-0000-4000-8000-000000000013',
        'a0000000-0000-4000-8000-000000000001',
        'PRACOWNIK',
        'Pracownik — dashboard (READ)',
        FALSE,
        TRUE
    )
ON CONFLICT (id) DO NOTHING;

-- ADMIN: full EDIT on all currently implemented business modules + users/roles admin.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000010', p.id
FROM permissions p
WHERE p.module IN (
    'DASHBOARD',
    'BUILDINGS',
    'STAIRCASES',
    'SCOPES',
    'ACTIVITIES',
    'EMPLOYEES',
    'MANAGERS',
    'SUPERVISORS',
    'CONTACTS',
    'USERS',
    'ROLES'
)
  AND p.action IN ('VIEW', 'CREATE', 'EDIT', 'DELETE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- BIURO: dashboard EDIT, buildings EDIT, employees EDIT (existing modules only).
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000011', p.id
FROM permissions p
WHERE (p.module = 'DASHBOARD' AND p.action IN ('VIEW', 'CREATE', 'EDIT', 'DELETE'))
   OR (p.module = 'BUILDINGS' AND p.action IN ('VIEW', 'CREATE', 'EDIT', 'DELETE'))
   OR (p.module = 'EMPLOYEES' AND p.action IN ('VIEW', 'CREATE', 'EDIT', 'DELETE'))
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- KOORDYNATOR: dashboard READ, buildings READ, employees READ.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000012', p.id
FROM permissions p
WHERE (p.module = 'DASHBOARD' AND p.action = 'VIEW')
   OR (p.module = 'BUILDINGS' AND p.action = 'VIEW')
   OR (p.module = 'EMPLOYEES' AND p.action = 'VIEW')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- PRACOWNIK: dashboard READ only.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000013', p.id
FROM permissions p
WHERE p.module = 'DASHBOARD' AND p.action = 'VIEW'
ON CONFLICT (role_id, permission_id) DO NOTHING;
