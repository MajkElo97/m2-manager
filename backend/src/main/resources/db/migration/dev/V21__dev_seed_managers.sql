-- Dev seed: managers for local development and tests.
INSERT INTO managers (
    id,
    organization_id,
    code,
    name,
    phone,
    email,
    address,
    notes,
    active
)
VALUES
    (
        'f0000000-0000-4000-8000-000000000001',
        'a0000000-0000-4000-8000-000000000001',
        'ZA0001',
        'Kozera Nieruchomości',
        '795702202',
        'kozeranieruchomosci@gmail.com',
        'ul. Cupiała 7b, 41-300 Dąbrowa Górnicza',
        'numer do Magdy',
        TRUE
    ),
    (
        'f0000000-0000-4000-8000-000000000002',
        'a0000000-0000-4000-8000-000000000001',
        'ZA0002',
        'SPÓŁDZIELNIA MIESZKANIOWA "PODLESIE"',
        '795702202',
        'angelika.podlesie@gmail.com',
        'ul. Kasprzaka 38, 41-303 Dąbrowa Górnicza',
        'numer do Angeliki',
        TRUE
    )
ON CONFLICT (id) DO NOTHING;
