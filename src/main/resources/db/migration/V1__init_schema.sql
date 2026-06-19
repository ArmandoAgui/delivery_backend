-- V1__init_schema.sql
-- Initial schema for a food delivery backend.
-- Target database: PostgreSQL 15+.

-- ============================================================================
-- Base Tables
-- ============================================================================

-- Stores application roles used by Spring Security authorization.
CREATE TABLE roles (
    id BIGSERIAL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name),
    CONSTRAINT chk_roles_name CHECK (name IN ('ADMIN', 'CUSTOMER', 'RESTAURANT', 'DELIVERY'))
);

-- Stores all platform users. Specific behavior is derived from the assigned role.
CREATE TABLE users (
    id BIGSERIAL,
    role_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT fk_users_role_id_roles FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone),
    CONSTRAINT chk_users_email_format CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$')
);

-- ============================================================================
-- Related Tables
-- ============================================================================

-- Stores customer addresses and geographic delivery points.
CREATE TABLE addresses (
    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    label VARCHAR(80),
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(120) NOT NULL,
    state VARCHAR(120),
    country VARCHAR(120) NOT NULL,
    postal_code VARCHAR(30),
    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_addresses PRIMARY KEY (id),
    CONSTRAINT fk_addresses_user_id_users FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_addresses_label_not_blank CHECK (label IS NULL OR LENGTH(TRIM(label)) > 0),
    CONSTRAINT chk_addresses_latitude_range CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_addresses_longitude_range CHECK (longitude BETWEEN -180 AND 180)
);

-- Stores restaurants managed by users with the RESTAURANT role.
-- The role itself is validated at the application layer to keep the database simple.
CREATE TABLE restaurants (
    id BIGSERIAL,
    owner_user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    phone VARCHAR(30),
    email VARCHAR(150),
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(120) NOT NULL,
    state VARCHAR(120),
    country VARCHAR(120) NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,
    is_open BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_restaurants PRIMARY KEY (id),
    CONSTRAINT fk_restaurants_owner_user_id_users FOREIGN KEY (owner_user_id) REFERENCES users (id),
    CONSTRAINT uk_restaurants_owner_user_id UNIQUE (owner_user_id),
    CONSTRAINT uk_restaurants_name_city UNIQUE (name, city),
    CONSTRAINT chk_restaurants_email_format CHECK (email IS NULL OR email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    CONSTRAINT chk_restaurants_latitude_range CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_restaurants_longitude_range CHECK (longitude BETWEEN -180 AND 180)
);

-- Stores restaurant product categories such as burgers, drinks, and desserts.
CREATE TABLE categories (
    id BIGSERIAL,
    restaurant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT fk_categories_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id),
    CONSTRAINT uk_categories_restaurant_id_name UNIQUE (restaurant_id, name),
    CONSTRAINT uk_categories_id_restaurant_id UNIQUE (id, restaurant_id)
);

-- Stores menu products offered by restaurants.
CREATE TABLE products (
    id BIGSERIAL,
    restaurant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    image_url VARCHAR(500),
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id),
    CONSTRAINT fk_products_category_id_restaurant_id_categories FOREIGN KEY (category_id, restaurant_id) REFERENCES categories (id, restaurant_id),
    CONSTRAINT uk_products_restaurant_id_name UNIQUE (restaurant_id, name),
    CONSTRAINT chk_products_price_positive CHECK (price > 0)
);

-- Stores customer orders.
-- Status fields use VARCHAR plus CHECK constraints instead of PostgreSQL ENUMs to simplify future migrations.
CREATE TABLE orders (
    id BIGSERIAL,
    customer_user_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    delivery_address_id BIGINT NOT NULL,
    coupon_id BIGINT,
    status VARCHAR(40) NOT NULL DEFAULT 'CREATED',
    subtotal_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    delivery_fee NUMERIC(10, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id),
    CONSTRAINT fk_orders_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id),
    CONSTRAINT fk_orders_delivery_address_id_addresses FOREIGN KEY (delivery_address_id) REFERENCES addresses (id),
    CONSTRAINT chk_orders_status CHECK (status IN ('CREATED', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'ON_THE_WAY', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT chk_orders_subtotal_amount_non_negative CHECK (subtotal_amount >= 0),
    CONSTRAINT chk_orders_delivery_fee_non_negative CHECK (delivery_fee >= 0),
    CONSTRAINT chk_orders_discount_amount_non_negative CHECK (discount_amount >= 0),
    CONSTRAINT chk_orders_total_amount_non_negative CHECK (total_amount >= 0)
);

-- Stores products purchased inside an order.
-- Product name and unit price are copied as a historical snapshot.
CREATE TABLE order_items (
    id BIGSERIAL,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    line_total NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product_id_products FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_order_items_order_id_product_id UNIQUE (order_id, product_id),
    CONSTRAINT chk_order_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price_positive CHECK (unit_price > 0),
    CONSTRAINT chk_order_items_line_total_non_negative CHECK (line_total >= 0)
);

-- Stores delivery driver assignment and lifecycle for an order.
CREATE TABLE delivery_assignments (
    id BIGSERIAL,
    order_id BIGINT NOT NULL,
    delivery_user_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'ASSIGNED',
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    picked_up_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_delivery_assignments PRIMARY KEY (id),
    CONSTRAINT fk_delivery_assignments_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_delivery_assignments_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id),
    CONSTRAINT uk_delivery_assignments_order_id UNIQUE (order_id),
    CONSTRAINT chk_delivery_assignments_status CHECK (status IN ('ASSIGNED', 'ACCEPTED', 'PICKED_UP', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT chk_delivery_assignments_time_order CHECK (
        (picked_up_at IS NULL OR picked_up_at >= assigned_at)
        AND (delivered_at IS NULL OR picked_up_at IS NOT NULL)
        AND (delivered_at IS NULL OR delivered_at >= picked_up_at)
    )
);

-- Stores payment attempts and payment results.
CREATE TABLE payments (
    id BIGSERIAL,
    order_id BIGINT NOT NULL,
    provider VARCHAR(40) NOT NULL DEFAULT 'SIMULATED',
    provider_transaction_id VARCHAR(150),
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    amount NUMERIC(10, 2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT fk_payments_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT uk_payments_provider_transaction_id UNIQUE (provider_transaction_id),
    CONSTRAINT chk_payments_provider CHECK (provider IN ('SIMULATED', 'STRIPE')),
    CONSTRAINT chk_payments_status CHECK (status IN ('PENDING', 'AUTHORIZED', 'PAID', 'FAILED', 'REFUNDED', 'CANCELLED')),
    CONSTRAINT chk_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_payments_currency_uppercase CHECK (currency = UPPER(currency))
);

-- Stores customer reviews for completed orders, restaurants, and optionally delivery drivers.
CREATE TABLE reviews (
    id BIGSERIAL,
    order_id BIGINT NOT NULL,
    reviewer_user_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    delivery_user_id BIGINT,
    rating INTEGER NOT NULL,
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT fk_reviews_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_reviews_reviewer_user_id_users FOREIGN KEY (reviewer_user_id) REFERENCES users (id),
    CONSTRAINT fk_reviews_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id),
    CONSTRAINT fk_reviews_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id),
    CONSTRAINT uk_reviews_order_id_reviewer_user_id UNIQUE (order_id, reviewer_user_id),
    CONSTRAINT chk_reviews_rating_range CHECK (rating BETWEEN 1 AND 5)
);

-- Stores customer complaints related to orders.
CREATE TABLE complaints (
    id BIGSERIAL,
    order_id BIGINT NOT NULL,
    customer_user_id BIGINT NOT NULL,
    assigned_admin_user_id BIGINT,
    status VARCHAR(40) NOT NULL DEFAULT 'OPEN',
    subject VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    resolution TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_complaints PRIMARY KEY (id),
    CONSTRAINT fk_complaints_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_complaints_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id),
    CONSTRAINT fk_complaints_assigned_admin_user_id_users FOREIGN KEY (assigned_admin_user_id) REFERENCES users (id),
    CONSTRAINT chk_complaints_status CHECK (status IN ('OPEN', 'IN_REVIEW', 'RESOLVED', 'REJECTED', 'CLOSED')),
    CONSTRAINT chk_complaints_subject_not_blank CHECK (LENGTH(TRIM(subject)) > 0),
    CONSTRAINT chk_complaints_description_not_blank CHECK (LENGTH(TRIM(description)) > 0)
);

-- Stores discount coupons applicable to orders.
CREATE TABLE coupons (
    id BIGSERIAL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL,
    discount_value NUMERIC(10, 2) NOT NULL,
    minimum_order_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    max_discount_amount NUMERIC(10, 2),
    usage_limit INTEGER,
    used_count INTEGER NOT NULL DEFAULT 0,
    starts_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_coupons PRIMARY KEY (id),
    CONSTRAINT uk_coupons_code UNIQUE (code),
    CONSTRAINT chk_coupons_code_uppercase CHECK (code = UPPER(code)),
    CONSTRAINT chk_coupons_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT chk_coupons_discount_value_positive CHECK (discount_value > 0),
    CONSTRAINT chk_coupons_percentage_range CHECK (discount_type <> 'PERCENTAGE' OR discount_value <= 100),
    CONSTRAINT chk_coupons_minimum_order_amount_non_negative CHECK (minimum_order_amount >= 0),
    CONSTRAINT chk_coupons_max_discount_amount_positive CHECK (max_discount_amount IS NULL OR max_discount_amount > 0),
    CONSTRAINT chk_coupons_usage_limit_positive CHECK (usage_limit IS NULL OR usage_limit > 0),
    CONSTRAINT chk_coupons_used_count_non_negative CHECK (used_count >= 0),
    CONSTRAINT chk_coupons_usage_count_within_limit CHECK (usage_limit IS NULL OR used_count <= usage_limit),
    CONSTRAINT chk_coupons_valid_date_range CHECK (expires_at > starts_at)
);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_coupon_id_coupons FOREIGN KEY (coupon_id) REFERENCES coupons (id);

-- ============================================================================
-- Initial Data
-- ============================================================================

INSERT INTO roles (name, description)
VALUES
    ('ADMIN', 'Platform administrator'),
    ('CUSTOMER', 'Customer who places food orders'),
    ('RESTAURANT', 'Restaurant owner or manager'),
    ('DELIVERY', 'Delivery driver')
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- Indexes
-- ============================================================================

-- Geospatial indexes for nearby restaurants and delivery address distance queries.
CREATE INDEX idx_addresses_latitude_longitude ON addresses (latitude, longitude);
CREATE INDEX idx_restaurants_latitude_longitude ON restaurants (latitude, longitude);

-- User lookup indexes.
CREATE INDEX idx_users_role_id ON users (role_id);
CREATE INDEX idx_users_is_active ON users (is_active);

-- Address lookup indexes.
CREATE INDEX idx_addresses_user_id ON addresses (user_id);
CREATE INDEX idx_addresses_user_id_is_default ON addresses (user_id, is_default);

-- Restaurant and menu lookup indexes.
CREATE INDEX idx_restaurants_owner_user_id ON restaurants (owner_user_id);
CREATE INDEX idx_restaurants_is_active_is_open ON restaurants (is_active, is_open);
CREATE INDEX idx_categories_restaurant_id ON categories (restaurant_id);
CREATE INDEX idx_products_restaurant_id ON products (restaurant_id);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_restaurant_id_is_available ON products (restaurant_id, is_available);

-- Order lifecycle lookup indexes.
CREATE INDEX idx_orders_customer_user_id ON orders (customer_user_id);
CREATE INDEX idx_orders_restaurant_id ON orders (restaurant_id);
CREATE INDEX idx_orders_delivery_address_id ON orders (delivery_address_id);
CREATE INDEX idx_orders_coupon_id ON orders (coupon_id);
CREATE INDEX idx_orders_status_created_at ON orders (status, created_at);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);

-- Delivery and payment lookup indexes.
CREATE INDEX idx_delivery_assignments_delivery_user_id ON delivery_assignments (delivery_user_id);
CREATE INDEX idx_delivery_assignments_status ON delivery_assignments (status);
CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_status ON payments (status);

-- Customer service and reporting indexes.
CREATE INDEX idx_reviews_restaurant_id ON reviews (restaurant_id);
CREATE INDEX idx_reviews_delivery_user_id ON reviews (delivery_user_id);
CREATE INDEX idx_complaints_order_id ON complaints (order_id);
CREATE INDEX idx_complaints_customer_user_id ON complaints (customer_user_id);
CREATE INDEX idx_complaints_status ON complaints (status);
CREATE INDEX idx_coupons_is_active ON coupons (is_active);
CREATE INDEX idx_coupons_validity ON coupons (starts_at, expires_at);
