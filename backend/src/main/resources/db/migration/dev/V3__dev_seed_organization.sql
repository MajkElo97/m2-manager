-- Dev seed: single organization for local development and tests.
-- This migration runs only when the dev/test Flyway location is enabled.
INSERT INTO organizations (id, name, nip, email, phone, active, timezone)
VALUES (
    'a0000000-0000-4000-8000-000000000001',
    'M2 Manager Dev',
    '1234567890',
    'dev@m2manager.local',
    '+48123456789',
    TRUE,
    'Europe/Warsaw'
)
ON CONFLICT (id) DO NOTHING;
