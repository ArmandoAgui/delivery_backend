-- Test data for the delivery module.
-- Run manually in Supabase SQL editor or with psql. This is intentionally not a
-- Flyway migration because it contains local/dev data only.

BEGIN;

INSERT INTO roles (name, description)
VALUES
    ('ADMIN', 'Platform administrator'),
    ('CUSTOMER', 'Customer who places food orders'),
    ('RESTAURANT', 'Restaurant owner or manager'),
    ('DELIVERY', 'Delivery driver')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (id, role_id, first_name, last_name, email, phone, password_hash, is_active)
VALUES
    ('018f0000-0000-7000-8000-000000000001', (SELECT id FROM roles WHERE name = 'CUSTOMER'), 'Cliente', 'Demo', 'cliente.demo@example.com', '+50370000001', '{noop}password', true),
    ('018f0000-0000-7000-8000-000000000002', (SELECT id FROM roles WHERE name = 'RESTAURANT'), 'Restaurante', 'Demo', 'restaurante.demo@example.com', '+50370000002', '{noop}password', true),
    ('018f0000-0000-7000-8000-000000000003', (SELECT id FROM roles WHERE name = 'DELIVERY'), 'Repartidor', 'Cercano', 'delivery.cercano@example.com', '+50370000003', '{noop}password', true),
    ('018f0000-0000-7000-8000-000000000004', (SELECT id FROM roles WHERE name = 'DELIVERY'), 'Repartidor', 'Lejano', 'delivery.lejano@example.com', '+50370000004', '{noop}password', true),
    ('018f0000-0000-7000-8000-000000000005', (SELECT id FROM roles WHERE name = 'DELIVERY'), 'Repartidor', 'Ocupado', 'delivery.ocupado@example.com', '+50370000005', '{noop}password', true),
    ('018f0000-0000-7000-8000-000000000006', (SELECT id FROM roles WHERE name = 'DELIVERY'), 'Repartidor', 'Inactivo', 'delivery.inactivo@example.com', '+50370000006', '{noop}password', false)
ON CONFLICT (email) DO UPDATE SET
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    role_id = EXCLUDED.role_id,
    phone = EXCLUDED.phone,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

INSERT INTO addresses (
    id,
    user_id,
    label,
    street_address,
    city,
    state,
    country,
    postal_code,
    location,
    is_default
)
VALUES (
    '018f0000-0000-7000-8000-000000000101',
    '018f0000-0000-7000-8000-000000000001',
    'Casa',
    'Calle La Mascota 123',
    'San Salvador',
    'San Salvador',
    'El Salvador',
    '1101',
    ST_SetSRID(ST_MakePoint(-89.2350, 13.6960), 4326)::geography,
    true
)
ON CONFLICT (id) DO UPDATE SET
    street_address = EXCLUDED.street_address,
    location = EXCLUDED.location,
    is_default = EXCLUDED.is_default;

INSERT INTO restaurants (
    id,
    owner_user_id,
    name,
    description,
    phone,
    email,
    street_address,
    city,
    state,
    country,
    location,
    is_open,
    is_active
)
VALUES (
    '018f0000-0000-7000-8000-000000000201',
    '018f0000-0000-7000-8000-000000000002',
    'Pupuseria Demo',
    'Restaurante de prueba para delivery',
    '+50370001000',
    'pupuseria.demo@example.com',
    'Boulevard de Los Proceres 500',
    'San Salvador',
    'San Salvador',
    'El Salvador',
    ST_SetSRID(ST_MakePoint(-89.2320, 13.6929), 4326)::geography,
    true,
    true
)
ON CONFLICT (owner_user_id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    street_address = EXCLUDED.street_address,
    location = EXCLUDED.location,
    is_open = EXCLUDED.is_open,
    is_active = EXCLUDED.is_active;

INSERT INTO categories (restaurant_id, name, description, is_active)
VALUES (
    '018f0000-0000-7000-8000-000000000201',
    'Pupusas',
    'Categoria de prueba',
    true
)
ON CONFLICT (restaurant_id, name) DO UPDATE SET
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active;

INSERT INTO products (
    id,
    restaurant_id,
    category_id,
    name,
    description,
    price,
    image_url,
    is_available
)
VALUES (
    '018f0000-0000-7000-8000-000000000301',
    '018f0000-0000-7000-8000-000000000201',
    (SELECT id FROM categories WHERE restaurant_id = '018f0000-0000-7000-8000-000000000201' AND name = 'Pupusas'),
    'Pupusa revuelta',
    'Producto de prueba',
    1.50,
    null,
    true
)
ON CONFLICT (restaurant_id, name) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    is_available = EXCLUDED.is_available;

INSERT INTO orders (
    id,
    customer_user_id,
    restaurant_id,
    delivery_address_id,
    status,
    subtotal_amount,
    delivery_fee,
    discount_amount,
    total_amount,
    notes,
    tax_amount,
    tip_amount
)
VALUES
    (
        '018f0000-0000-7000-8000-000000000401',
        '018f0000-0000-7000-8000-000000000001',
        '018f0000-0000-7000-8000-000000000201',
        '018f0000-0000-7000-8000-000000000101',
        'READY_FOR_PICKUP',
        3.00,
        1.50,
        0.00,
        4.50,
        'Pedido listo para probar POST /api/deliveries/assign',
        0.00,
        0.00
    ),
    (
        '018f0000-0000-7000-8000-000000000402',
        '018f0000-0000-7000-8000-000000000001',
        '018f0000-0000-7000-8000-000000000201',
        '018f0000-0000-7000-8000-000000000101',
        'CANCELLED',
        3.00,
        1.50,
        0.00,
        4.50,
        'Pedido cancelado para probar validacion',
        0.00,
        0.00
    ),
    (
        '018f0000-0000-7000-8000-000000000403',
        '018f0000-0000-7000-8000-000000000001',
        '018f0000-0000-7000-8000-000000000201',
        '018f0000-0000-7000-8000-000000000101',
        'READY_FOR_PICKUP',
        3.00,
        1.50,
        0.00,
        4.50,
        'Pedido asignado al repartidor ocupado',
        0.00,
        0.00
    )
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    notes = EXCLUDED.notes,
    tax_amount = EXCLUDED.tax_amount,
    tip_amount = EXCLUDED.tip_amount;

INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, line_total)
VALUES
    ('018f0000-0000-7000-8000-000000000501', '018f0000-0000-7000-8000-000000000401', '018f0000-0000-7000-8000-000000000301', 'Pupusa revuelta', 2, 1.50, 3.00),
    ('018f0000-0000-7000-8000-000000000502', '018f0000-0000-7000-8000-000000000402', '018f0000-0000-7000-8000-000000000301', 'Pupusa revuelta', 2, 1.50, 3.00),
    ('018f0000-0000-7000-8000-000000000503', '018f0000-0000-7000-8000-000000000403', '018f0000-0000-7000-8000-000000000301', 'Pupusa revuelta', 2, 1.50, 3.00)
ON CONFLICT (order_id, product_id) DO UPDATE SET
    quantity = EXCLUDED.quantity,
    unit_price = EXCLUDED.unit_price,
    line_total = EXCLUDED.line_total;

INSERT INTO delivery_assignments (
    id,
    order_id,
    delivery_user_id,
    status,
    assigned_at,
    picked_up_at,
    delivered_at
)
VALUES (
    '018f0000-0000-7000-8000-000000000601',
    '018f0000-0000-7000-8000-000000000403',
    '018f0000-0000-7000-8000-000000000005',
    'ASSIGNED',
    NOW(),
    null,
    null
)
ON CONFLICT (order_id) DO UPDATE SET
    delivery_user_id = EXCLUDED.delivery_user_id,
    status = EXCLUDED.status,
    picked_up_at = EXCLUDED.picked_up_at,
    delivered_at = EXCLUDED.delivered_at;

INSERT INTO delivery_locations (
    id,
    delivery_user_id,
    order_id,
    delivery_batch_id,
    location,
    recorded_at
)
VALUES
    (
        '018f0000-0000-7000-8000-000000000701',
        '018f0000-0000-7000-8000-000000000003',
        '018f0000-0000-7000-8000-000000000401',
        null,
        ST_SetSRID(ST_MakePoint(-89.2326, 13.6932), 4326)::geography,
        NOW()
    ),
    (
        '018f0000-0000-7000-8000-000000000702',
        '018f0000-0000-7000-8000-000000000004',
        '018f0000-0000-7000-8000-000000000401',
        null,
        ST_SetSRID(ST_MakePoint(-89.2500, 13.7060), 4326)::geography,
        NOW()
    ),
    (
        '018f0000-0000-7000-8000-000000000703',
        '018f0000-0000-7000-8000-000000000005',
        '018f0000-0000-7000-8000-000000000403',
        null,
        ST_SetSRID(ST_MakePoint(-89.2319, 13.6928), 4326)::geography,
        NOW()
    )
ON CONFLICT (id) DO UPDATE SET
    location = EXCLUDED.location,
    recorded_at = EXCLUDED.recorded_at;

COMMIT;

-- Useful IDs:
-- order ready for assignment:       018f0000-0000-7000-8000-000000000401
-- cancelled order:                  018f0000-0000-7000-8000-000000000402
-- delivery user nearest available:  018f0000-0000-7000-8000-000000000003
-- delivery user occupied:           018f0000-0000-7000-8000-000000000005
-- occupied assignment:              018f0000-0000-7000-8000-000000000601
