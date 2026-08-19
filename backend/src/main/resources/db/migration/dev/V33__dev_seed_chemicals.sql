-- Dev seed: synthetic demo chemical for integration tests (not real business data).
INSERT INTO chemicals (
    id,
    organization_id,
    code,
    name,
    category,
    quantity,
    unit,
    minimum_stock,
    location,
    active,
    notes
)
VALUES (
    'f6000000-0000-4000-8000-000000000001',
    'a0000000-0000-4000-8000-000000000001',
    'DEMO-CH-001',
    'Demo glass cleaner',
    'Windows',
    12.000,
    'LITER',
    5.000,
    'Magazyn',
    TRUE,
    'Synthetic dev seed — not real chemical data'
)
ON CONFLICT (id) DO NOTHING;
