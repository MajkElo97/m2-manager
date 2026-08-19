-- Dev seed: activity scopes for existing dev buildings and activities.

INSERT INTO activity_scopes (
    id,
    organization_id,
    code,
    building_id,
    activity_id,
    planning_type,
    frequency,
    weekdays,
    notes,
    status
)
SELECT
    seed.id,
    'a0000000-0000-4000-8000-000000000001',
    seed.code,
    b.id,
    a.id,
    seed.planning_type,
    seed.frequency,
    seed.weekdays,
    seed.notes,
    seed.status
FROM (
    VALUES
        ('01000000-0000-4000-8000-000000000001'::uuid, 'ZP0001', 'PUSTA64', 'CZ0001', 'WEEKLY', 1, 'Wtorek', NULL::text, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000002'::uuid, 'ZP0002', 'PUSTA64', 'CZ0002', 'WEEKLY', 1, 'Wtorek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000003'::uuid, 'ZP0003', 'PUSTA64', 'CZ0003', 'WEEKLY', 1, 'Wtorek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000004'::uuid, 'ZP0004', 'PUSTA64', 'CZ0004', 'WEEKLY', 1, 'Wtorek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000005'::uuid, 'ZP0005', 'PUSTA64', 'CZ0009', 'YEARLY', 2, NULL, 'Wiosna i jesień', 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000006'::uuid, 'ZP0006', 'PUSTA64', 'CZ0018', 'EVENT', NULL, NULL, 'Wg potrzeb', 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000007'::uuid, 'ZP0007', 'PUSTA64', 'CZ0015', 'EVENT', NULL, NULL, 'Wg potrzeb', 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000008'::uuid, 'ZP0008', 'SKLODOWSKA37', 'CZ0001', 'WEEKLY', 1, 'Czwartek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000009'::uuid, 'ZP0009', 'SKLODOWSKA37', 'CZ0002', 'WEEKLY', 1, 'Czwartek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000000a'::uuid, 'ZP0010', 'SKLODOWSKA37', 'CZ0003', 'WEEKLY', 1, 'Czwartek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000000b'::uuid, 'ZP0011', 'SKLODOWSKA37', 'CZ0004', 'WEEKLY', 1, 'Czwartek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000000c'::uuid, 'ZP0012', 'SKLODOWSKA37', 'CZ0009', 'YEARLY', 2, NULL, 'Wiosna i jesień', 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000000d'::uuid, 'ZP0013', 'SKLODOWSKA37', 'CZ0018', 'EVENT', NULL, NULL, 'Wg potrzeb', 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000000e'::uuid, 'ZP0014', 'SKLODOWSKA37', 'CZ0015', 'EVENT', NULL, NULL, 'Wg potrzeb', 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000000f'::uuid, 'ZP0015', 'KASPRZAKA6', 'CZ0001', 'WEEKLY', 2, 'Poniedziałek, Piątek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000010'::uuid, 'ZP0016', 'KASPRZAKA6', 'CZ0002', 'WEEKLY', 2, 'Poniedziałek, Piątek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000011'::uuid, 'ZP0017', 'KASPRZAKA6', 'CZ0003', 'WEEKLY', 1, 'Poniedziałek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000012'::uuid, 'ZP0018', 'KASPRZAKA6', 'CZ0004', 'MONTHLY', 1, NULL, NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000013'::uuid, 'ZP0019', 'KASPRZAKA6', 'CZ0009', 'YEARLY', 4, NULL, NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000014'::uuid, 'ZP0020', 'KASPRZAKA6', 'CZ0018', 'EVENT', NULL, NULL, 'Wg potrzeb', 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000015'::uuid, 'ZP0021', 'KASPRZAKA6', 'CZ0021', 'MONTHLY', 1, 'Poniedziałek, Piątek', '2x w tygodniu co 3 miesiące', 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000016'::uuid, 'ZP0022', 'KASPRZAKA6', 'CZ0022', 'MONTHLY', 1, NULL, 'raz w miesiącu', 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000017'::uuid, 'ZP0023', 'KASPRZAKA6', 'CZ0023', 'YEARLY', 1, NULL, NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000018'::uuid, 'ZP0024', 'APARTAMENTYPRZYJEZIORZE', 'CZ0001', 'MONTHLY', 2, 'Poniedziałek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000019'::uuid, 'ZP0025', 'APARTAMENTYPRZYJEZIORZE', 'CZ0015', 'EVENT', 1, NULL, 'Co ok. 45 dni (średnio co 1,5 miesiąca)', 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000001a'::uuid, 'ZP0026', 'APARTAMENTYPRZYJEZIORZE', 'CZ0018', 'EVENT', NULL, NULL, 'Wg potrzeb', 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000001b'::uuid, 'ZP0027', 'CIESZKOWSKIEGO14', 'CZ0001', 'WEEKLY', 2, 'Poniedziałek, Piątek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000001c'::uuid, 'ZP0028', 'CIESZKOWSKIEGO14', 'CZ0002', 'WEEKLY', 2, 'Poniedziałek, Piątek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000001d'::uuid, 'ZP0029', 'CIESZKOWSKIEGO14', 'CZ0003', 'WEEKLY', 2, 'Poniedziałek, Piątek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000001e'::uuid, 'ZP0030', 'CIESZKOWSKIEGO14', 'CZ0004', 'MONTHLY', 1, 'Poniedziałek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-00000000001f'::uuid, 'ZP0031', 'CIESZKOWSKIEGO14', 'CZ0009', 'YEARLY', 4, NULL, NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000020'::uuid, 'ZP0032', 'CIESZKOWSKIEGO14', 'CZ0021', 'MONTHLY', 1, 'Poniedziałek, Piątek', '2x w tygodniu co 2 miesiące', 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000021'::uuid, 'ZP0033', 'CIESZKOWSKIEGO14', 'CZ0022', 'MONTHLY', 1, 'Poniedziałek', NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000022'::uuid, 'ZP0034', 'CIESZKOWSKIEGO14', 'CZ0023', 'YEARLY', 1, NULL, NULL, 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000023'::uuid, 'ZP0035', 'CIESZKOWSKIEGO14', 'CZ0018', 'EVENT', NULL, NULL, 'Wg potrzeb', 'ACTIVE'),
        ('01000000-0000-4000-8000-000000000024'::uuid, 'ZP0036', 'CIESZKOWSKIEGO14', 'CZ0015', 'EVENT', NULL, NULL, 'Wg potrzeb', 'ACTIVE')
) AS seed(id, code, building_code, activity_code, planning_type, frequency, weekdays, notes, status)
JOIN buildings b ON b.code = seed.building_code
JOIN activities a ON a.code = seed.activity_code
ON CONFLICT (id) DO NOTHING;
