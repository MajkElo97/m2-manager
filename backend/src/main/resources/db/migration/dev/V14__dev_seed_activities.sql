-- Dev seed: global activity catalog for local development and tests.

INSERT INTO activities (
    id,
    code,
    name,
    category,
    planning_type,
    default_period,
    duration_minutes,
    priority,
    active
)
VALUES
    ('f0000000-0000-4000-8000-000000000001', 'CZ0001', 'Tereny zewnętrzne', 'Sprzątanie', 'CYCLIC', NULL, 30, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000002', 'CZ0002', 'Tereny wewnętrzne', 'Sprzątanie', 'CYCLIC', NULL, 30, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000003', 'CZ0003', 'Mycie klatek schodowych', 'Sprzątanie', 'CYCLIC', NULL, 40, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000004', 'CZ0004', 'Mycie drzwi wejściowych', 'Sprzątanie', 'CYCLIC', NULL, 10, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000005', 'CZ0005', 'Czyszczenie wind', 'Sprzątanie', 'CYCLIC', NULL, 15, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000006', 'CZ0006', 'Opróżnianie koszy', 'Sprzątanie', 'CYCLIC', NULL, 10, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000007', 'CZ0007', 'Wymiana worków w koszach', 'Sprzątanie', 'CYCLIC', NULL, 10, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000008', 'CZ0008', 'Usuwanie pajęczyn', 'Sprzątanie', 'CYCLIC', NULL, 20, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000009', 'CZ0009', 'Mycie okien', 'Sprzątanie', 'PERIODIC', 'IV_KWARTAŁ', 180, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000010', 'CZ0010', 'Mycie przeszkleń', 'Sprzątanie', 'PERIODIC', 'IV_KWARTAŁ', 120, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000011', 'CZ0011', 'Sprzątanie piwnic', 'Sprzątanie', 'PERIODIC', 'II_KWARTAŁ', 90, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000012', 'CZ0012', 'Sprzątanie garaży', 'Sprzątanie', 'PERIODIC', 'II_KWARTAŁ', 120, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000013', 'CZ0013', 'Mycie altany śmietnikowej', 'Sprzątanie', 'PERIODIC', 'II_KWARTAŁ', 30, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000014', 'CZ0014', 'Czyszczenie wycieraczek', 'Sprzątanie', 'PERIODIC', 'I_KWARTAŁ', 15, 'LOW', TRUE),
    ('f0000000-0000-4000-8000-000000000015', 'CZ0015', 'Koszenie trawy', 'Tereny zielone', 'ON_DEMAND', 'SEZON', 120, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000016', 'CZ0016', 'Grabienie liści', 'Tereny zielone', 'ON_DEMAND', 'JESIEŃ', 120, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000017', 'CZ0017', 'Pielenie', 'Tereny zielone', 'ON_DEMAND', 'SEZON', 90, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000018', 'CZ0018', 'Odśnieżanie', 'Zimowe', 'ON_DEMAND', 'ZIMA', 120, 'HIGH', TRUE),
    ('f0000000-0000-4000-8000-000000000019', 'CZ0019', 'Posypywanie chodników', 'Zimowe', 'ON_DEMAND', 'ZIMA', 45, 'HIGH', TRUE),
    ('f0000000-0000-4000-8000-000000000020', 'CZ0020', 'Zgłoszenie usterki', 'Techniczne', 'ON_DEMAND', '15', NULL, 'HIGH', TRUE),
    ('f0000000-0000-4000-8000-000000000021', 'CZ0021', 'Sprzątanie wiaty śmietnikowej', 'Sprzątanie', 'PERIODIC', 'II_KWARTAŁ', 30, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000022', 'CZ0022', 'Zamiatanie piwnic', 'Sprzątanie', 'PERIODIC', 'III_KWARTAŁ', 45, 'NORMAL', TRUE),
    ('f0000000-0000-4000-8000-000000000023', 'CZ0023', 'Mycie lamperii', 'Sprzątanie', 'PERIODIC', 'III_KWARTAŁ', 180, 'NORMAL', TRUE)
ON CONFLICT (id) DO NOTHING;
