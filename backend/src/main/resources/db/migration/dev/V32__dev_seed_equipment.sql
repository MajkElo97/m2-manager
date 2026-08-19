-- Dev seed: synthetic demo equipment for integration tests (not real business data).
INSERT INTO equipment (
    id,
    organization_id,
    code,
    name,
    category,
    manufacturer,
    model,
    serial_number,
    quantity,
    condition_status,
    location,
    employee_id,
    purchase_date,
    purchase_value,
    active,
    notes
)
VALUES (
    'f5000000-0000-4000-8000-000000000001',
    'a0000000-0000-4000-8000-000000000001',
    'DEMO-EQ-001',
    'Demo pressure washer',
    'Cleaning',
    'Demo',
    'PW-100',
    'DEMO-SN-001',
    2,
    'GOOD',
    'Magazyn',
    NULL,
    '2024-03-01',
    1500.00,
    TRUE,
    'Synthetic dev seed — not real equipment data'
)
ON CONFLICT (id) DO NOTHING;
