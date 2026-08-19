-- Dev seed: building contacts for local development and tests.
INSERT INTO contacts (
    id,
    organization_id,
    building_id,
    first_name,
    last_name,
    function_title,
    phone,
    email,
    notes,
    active
)
VALUES
    (
        'f3000000-0000-4000-8000-000000000001',
        'a0000000-0000-4000-8000-000000000001',
        'd0000000-0000-4000-8000-000000000001',
        'Michał',
        'Ociepka',
        'Członek zarządu',
        '516154328',
        'm.ocpieka97@gmail.com',
        NULL,
        TRUE
    ),
    (
        'f3000000-0000-4000-8000-000000000002',
        'a0000000-0000-4000-8000-000000000001',
        'd0000000-0000-4000-8000-000000000002',
        'Michał',
        'Ociepka',
        'Członek zarządu',
        '516154328',
        'm.ocpieka97@gmail.com',
        NULL,
        TRUE
    ),
    (
        'f3000000-0000-4000-8000-000000000003',
        'a0000000-0000-4000-8000-000000000001',
        'd0000000-0000-4000-8000-000000000005',
        'Marek',
        'Zychla',
        'Członek zarządu',
        '502459144',
        'marexantykorozja@interia.pl',
        NULL,
        TRUE
    ),
    (
        'f3000000-0000-4000-8000-000000000004',
        'a0000000-0000-4000-8000-000000000001',
        'd0000000-0000-4000-8000-000000000009',
        NULL,
        NULL,
        'Członek zarządu',
        '608638120',
        'szulc3@op.pl',
        NULL,
        TRUE
    )
ON CONFLICT (id) DO NOTHING;
