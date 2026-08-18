-- Dev seed: staircases for existing dev buildings.
INSERT INTO staircases (
    id,
    organization_id,
    building_id,
    code,
    designation,
    intercom_code,
    key_required,
    elevator,
    floors,
    notes
)
SELECT
    seed.id,
    'a0000000-0000-4000-8000-000000000001',
    b.id,
    seed.code,
    seed.designation,
    seed.intercom_code,
    seed.key_required,
    seed.elevator,
    seed.floors,
    seed.notes
FROM (
    VALUES
        ('e0000000-0000-4000-8000-000000000001'::uuid, 'KL0001', 'PUSTA64', '1', '#2258', FALSE, FALSE, 4, NULL::text),
        ('e0000000-0000-4000-8000-000000000002'::uuid, 'KL0002', 'SKLODOWSKA37', '1', 'dzwonek2587dzwonek', FALSE, FALSE, 4, NULL::text),
        ('e0000000-0000-4000-8000-000000000003'::uuid, 'KL0003', 'KASPRZAKA6', '1', '0610#', FALSE, FALSE, 5, NULL::text),
        ('e0000000-0000-4000-8000-000000000004'::uuid, 'KL0004', 'KASPRZAKA6', '2', '2606#', FALSE, FALSE, 5, NULL::text),
        ('e0000000-0000-4000-8000-000000000005'::uuid, 'KL0005', '3KAMIENICE', 'A', 'dzwonek0100dzwonek', FALSE, FALSE, 4, NULL::text),
        ('e0000000-0000-4000-8000-000000000006'::uuid, 'KL0006', '3KAMIENICE', 'B', 'dzwonek0100dzwonek', FALSE, FALSE, 4, NULL::text),
        ('e0000000-0000-4000-8000-000000000007'::uuid, 'KL0007', '3KAMIENICE', 'C', 'dzwonek0100dzwonek', FALSE, FALSE, 4, NULL::text),
        ('e0000000-0000-4000-8000-000000000008'::uuid, 'KL0008', 'CIESZKOWSKIEGO14', '1', NULL::varchar, TRUE, FALSE, 4, NULL::text),
        ('e0000000-0000-4000-8000-000000000009'::uuid, 'KL0009', 'CIESZKOWSKIEGO14', '2', '111kluczyk7272', FALSE, FALSE, 4, NULL::text),
        ('e0000000-0000-4000-8000-000000000010'::uuid, 'KL0010', 'CIESZKOWSKIEGO14', '3', NULL::varchar, TRUE, FALSE, 4, NULL::text),
        ('e0000000-0000-4000-8000-000000000011'::uuid, 'KL0011', 'CIESZKOWSKIEGO14', '4', NULL::varchar, TRUE, FALSE, 4, NULL::text),
        ('e0000000-0000-4000-8000-000000000012'::uuid, 'KL0012', 'NOWYMANHATTAN', '1', NULL::varchar, FALSE, TRUE, 6, NULL::text)
) AS seed(id, code, building_code, designation, intercom_code, key_required, elevator, floors, notes)
JOIN buildings b
    ON b.organization_id = 'a0000000-0000-4000-8000-000000000001'
   AND b.code = seed.building_code
ON CONFLICT (id) DO NOTHING;
