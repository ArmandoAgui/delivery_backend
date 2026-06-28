BEGIN;

WITH source_password AS (
    SELECT password_hash
    FROM users
    WHERE email = 'cliente.dev@example.com'
),
generated_users AS (
    SELECT
        format('10000000-0000-7000-8000-%s', lpad(number::text, 12, '0'))::uuid AS id,
        format('loadtest-%s@example.com', lpad(number::text, 4, '0')) AS email,
        number
    FROM generate_series(1, 1000) AS number
)
INSERT INTO users (
    id,
    role_id,
    first_name,
    last_name,
    email,
    phone,
    password_hash,
    is_active,
    created_at
)
SELECT
    generated_users.id,
    roles.id,
    'Load',
    format('Test %s', generated_users.number),
    generated_users.email,
    NULL,
    source_password.password_hash,
    TRUE,
    CURRENT_TIMESTAMP
FROM generated_users
CROSS JOIN source_password
JOIN roles ON roles.name = 'CUSTOMER'
ON CONFLICT (email) DO UPDATE
SET is_active = TRUE,
    password_hash = EXCLUDED.password_hash;

WITH generated_addresses AS (
    SELECT
        format('20000000-0000-7000-8000-%s', lpad(number::text, 12, '0'))::uuid AS id,
        format('loadtest-%s@example.com', lpad(number::text, 4, '0')) AS email,
        number
    FROM generate_series(1, 1000) AS number
)
INSERT INTO addresses (
    id,
    user_id,
    label,
    street_address,
    city,
    state,
    location,
    is_default,
    created_at
)
SELECT
    generated_addresses.id,
    users.id,
    'Carga',
    format('Direccion de prueba %s', generated_addresses.number),
    'San Salvador',
    'San Salvador',
    ST_SetSRID(ST_MakePoint(-89.218000, 13.692800), 4326)::geography,
    TRUE,
    CURRENT_TIMESTAMP
FROM generated_addresses
JOIN users ON users.email = generated_addresses.email
ON CONFLICT (id) DO UPDATE
SET location = EXCLUDED.location,
    is_default = TRUE;

COMMIT;
