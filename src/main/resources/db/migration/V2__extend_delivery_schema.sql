-- V2__extend_delivery_schema.sql
-- Extends the initial schema to support carts, schedules, tracking, batching,
-- refunds, invoicing, commissions, coupon redemptions, and loyalty.

-- ============================================================================
-- Order Monetary Breakdown And Lifecycle Metadata
-- ============================================================================

ALTER TABLE orders
    ADD COLUMN tax_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    ADD COLUMN tip_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    ADD COLUMN estimated_delivery_minutes INTEGER,
    ADD COLUMN demand_multiplier NUMERIC(5, 2) NOT NULL DEFAULT 1,
    ADD COLUMN confirmed_at TIMESTAMPTZ,
    ADD COLUMN cancelled_at TIMESTAMPTZ,
    ADD CONSTRAINT chk_orders_tax_amount_non_negative CHECK (tax_amount >= 0),
    ADD CONSTRAINT chk_orders_tip_amount_non_negative CHECK (tip_amount >= 0),
    ADD CONSTRAINT chk_orders_estimated_delivery_minutes_positive CHECK (estimated_delivery_minutes IS NULL OR estimated_delivery_minutes > 0),
    ADD CONSTRAINT chk_orders_demand_multiplier_positive CHECK (demand_multiplier >= 1);

-- ============================================================================
-- Restaurant Schedules
-- ============================================================================

-- Stores weekly opening hours for each restaurant.
-- Day of week follows ISO convention: 1 = Monday, 7 = Sunday.
CREATE TABLE restaurant_schedules (
    id BIGSERIAL,
    restaurant_id BIGINT NOT NULL,
    day_of_week SMALLINT NOT NULL,
    opens_at TIME,
    closes_at TIME,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_restaurant_schedules PRIMARY KEY (id),
    CONSTRAINT fk_restaurant_schedules_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE,
    CONSTRAINT uk_restaurant_schedules_restaurant_id_day_of_week UNIQUE (restaurant_id, day_of_week),
    CONSTRAINT chk_restaurant_schedules_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_restaurant_schedules_open_days_have_hours CHECK (
        (is_closed = TRUE AND opens_at IS NULL AND closes_at IS NULL)
        OR (is_closed = FALSE AND opens_at IS NOT NULL AND closes_at IS NOT NULL AND closes_at > opens_at)
    )
);

-- ============================================================================
-- Shopping Cart
-- ============================================================================

-- Stores the active shopping cart for a customer and restaurant.
CREATE TABLE carts (
    id BIGSERIAL,
    customer_user_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_carts PRIMARY KEY (id),
    CONSTRAINT fk_carts_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id),
    CONSTRAINT fk_carts_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id),
    CONSTRAINT chk_carts_status CHECK (status IN ('ACTIVE', 'CHECKED_OUT', 'ABANDONED'))
);

-- Stores products selected by the customer before creating an order.
CREATE TABLE cart_items (
    id BIGSERIAL,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_cart_items PRIMARY KEY (id),
    CONSTRAINT fk_cart_items_cart_id_carts FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product_id_products FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_cart_items_cart_id_product_id UNIQUE (cart_id, product_id),
    CONSTRAINT chk_cart_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_cart_items_unit_price_positive CHECK (unit_price > 0)
);

-- ============================================================================
-- Order Tracking
-- ============================================================================

-- Stores every order status transition for auditability and real-time tracking history.
CREATE TABLE order_status_history (
    id BIGSERIAL,
    order_id BIGINT NOT NULL,
    previous_status VARCHAR(40),
    new_status VARCHAR(40) NOT NULL,
    changed_by_user_id BIGINT,
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_order_status_history PRIMARY KEY (id),
    CONSTRAINT fk_order_status_history_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_status_history_changed_by_user_id_users FOREIGN KEY (changed_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_order_status_history_previous_status CHECK (previous_status IS NULL OR previous_status IN ('CREATED', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'ON_THE_WAY', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT chk_order_status_history_new_status CHECK (new_status IN ('CREATED', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'ON_THE_WAY', 'DELIVERED', 'CANCELLED'))
);

-- ============================================================================
-- Delivery Batching And Location Tracking
-- ============================================================================

-- Groups several orders assigned to the same delivery driver during peak demand.
CREATE TABLE delivery_batches (
    id BIGSERIAL,
    delivery_user_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'OPEN',
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_delivery_batches PRIMARY KEY (id),
    CONSTRAINT fk_delivery_batches_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id),
    CONSTRAINT chk_delivery_batches_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_delivery_batches_time_order CHECK (completed_at IS NULL OR started_at IS NOT NULL),
    CONSTRAINT chk_delivery_batches_completed_after_started CHECK (completed_at IS NULL OR completed_at >= started_at)
);

-- Links orders to a delivery batch and preserves the intended delivery sequence.
CREATE TABLE delivery_batch_orders (
    id BIGSERIAL,
    delivery_batch_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    sequence_number INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_delivery_batch_orders PRIMARY KEY (id),
    CONSTRAINT fk_delivery_batch_orders_delivery_batch_id_delivery_batches FOREIGN KEY (delivery_batch_id) REFERENCES delivery_batches (id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_batch_orders_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT uk_delivery_batch_orders_order_id UNIQUE (order_id),
    CONSTRAINT uk_delivery_batch_orders_batch_id_sequence UNIQUE (delivery_batch_id, sequence_number),
    CONSTRAINT chk_delivery_batch_orders_sequence_number_positive CHECK (sequence_number > 0)
);

-- Stores delivery driver location samples for real-time tracking.
-- GEOGRAPHY(Point, 4326) keeps distance calculations consistent with restaurants and addresses.
CREATE TABLE delivery_locations (
    id BIGSERIAL,
    delivery_user_id BIGINT NOT NULL,
    order_id BIGINT,
    delivery_batch_id BIGINT,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_delivery_locations PRIMARY KEY (id),
    CONSTRAINT fk_delivery_locations_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id),
    CONSTRAINT fk_delivery_locations_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_delivery_locations_delivery_batch_id_delivery_batches FOREIGN KEY (delivery_batch_id) REFERENCES delivery_batches (id),
    CONSTRAINT chk_delivery_locations_tracking_target CHECK (order_id IS NOT NULL OR delivery_batch_id IS NOT NULL)
);

-- ============================================================================
-- Refunds And Invoicing
-- ============================================================================

-- Stores refunds approved from complaints or payment reversals.
CREATE TABLE refunds (
    id BIGSERIAL,
    payment_id BIGINT NOT NULL,
    complaint_id BIGINT,
    status VARCHAR(40) NOT NULL DEFAULT 'REQUESTED',
    amount NUMERIC(10, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_refunds PRIMARY KEY (id),
    CONSTRAINT fk_refunds_payment_id_payments FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_refunds_complaint_id_complaints FOREIGN KEY (complaint_id) REFERENCES complaints (id),
    CONSTRAINT chk_refunds_status CHECK (status IN ('REQUESTED', 'APPROVED', 'REJECTED', 'PROCESSED')),
    CONSTRAINT chk_refunds_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_refunds_reason_not_blank CHECK (LENGTH(TRIM(reason)) > 0)
);

-- Stores billing records generated from paid orders.
CREATE TABLE invoices (
    id BIGSERIAL,
    order_id BIGINT NOT NULL,
    payment_id BIGINT,
    invoice_number VARCHAR(80) NOT NULL,
    subtotal_amount NUMERIC(10, 2) NOT NULL,
    tax_amount NUMERIC(10, 2) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_invoices PRIMARY KEY (id),
    CONSTRAINT fk_invoices_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_invoices_payment_id_payments FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT uk_invoices_order_id UNIQUE (order_id),
    CONSTRAINT uk_invoices_invoice_number UNIQUE (invoice_number),
    CONSTRAINT chk_invoices_subtotal_amount_non_negative CHECK (subtotal_amount >= 0),
    CONSTRAINT chk_invoices_tax_amount_non_negative CHECK (tax_amount >= 0),
    CONSTRAINT chk_invoices_total_amount_non_negative CHECK (total_amount >= 0)
);

-- ============================================================================
-- Coupons And Loyalty
-- ============================================================================

-- Stores coupon usage by customer and order to prevent duplicated redemption.
CREATE TABLE coupon_redemptions (
    id BIGSERIAL,
    coupon_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    customer_user_id BIGINT NOT NULL,
    discount_amount NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_coupon_redemptions PRIMARY KEY (id),
    CONSTRAINT fk_coupon_redemptions_coupon_id_coupons FOREIGN KEY (coupon_id) REFERENCES coupons (id),
    CONSTRAINT fk_coupon_redemptions_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_coupon_redemptions_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id),
    CONSTRAINT uk_coupon_redemptions_coupon_id_order_id UNIQUE (coupon_id, order_id),
    CONSTRAINT chk_coupon_redemptions_discount_amount_non_negative CHECK (discount_amount >= 0)
);

-- Stores loyalty point balance per customer.
CREATE TABLE loyalty_accounts (
    id BIGSERIAL,
    customer_user_id BIGINT NOT NULL,
    points_balance INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_loyalty_accounts PRIMARY KEY (id),
    CONSTRAINT fk_loyalty_accounts_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id),
    CONSTRAINT uk_loyalty_accounts_customer_user_id UNIQUE (customer_user_id),
    CONSTRAINT chk_loyalty_accounts_points_balance_non_negative CHECK (points_balance >= 0)
);

-- Stores loyalty point movements for traceability.
CREATE TABLE loyalty_transactions (
    id BIGSERIAL,
    loyalty_account_id BIGINT NOT NULL,
    order_id BIGINT,
    transaction_type VARCHAR(30) NOT NULL,
    points INTEGER NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_loyalty_transactions PRIMARY KEY (id),
    CONSTRAINT fk_loyalty_transactions_loyalty_account_id_loyalty_accounts FOREIGN KEY (loyalty_account_id) REFERENCES loyalty_accounts (id),
    CONSTRAINT fk_loyalty_transactions_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT chk_loyalty_transactions_transaction_type CHECK (transaction_type IN ('EARNED', 'REDEEMED', 'ADJUSTED')),
    CONSTRAINT chk_loyalty_transactions_points_not_zero CHECK (points <> 0)
);

-- ============================================================================
-- Platform Commissions
-- ============================================================================

-- Stores platform commission configuration per restaurant.
CREATE TABLE restaurant_commissions (
    id BIGSERIAL,
    restaurant_id BIGINT NOT NULL,
    commission_percentage NUMERIC(5, 2) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_restaurant_commissions PRIMARY KEY (id),
    CONSTRAINT fk_restaurant_commissions_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id),
    CONSTRAINT chk_restaurant_commissions_percentage_range CHECK (commission_percentage >= 0 AND commission_percentage <= 100),
    CONSTRAINT chk_restaurant_commissions_date_range CHECK (ends_at IS NULL OR ends_at > starts_at)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_delivery_locations_location_gist ON delivery_locations USING GIST (location);

CREATE INDEX idx_restaurant_schedules_restaurant_id_day ON restaurant_schedules (restaurant_id, day_of_week);

CREATE INDEX idx_carts_customer_user_id_status ON carts (customer_user_id, status);
CREATE UNIQUE INDEX uk_carts_active_customer_restaurant ON carts (customer_user_id, restaurant_id) WHERE status = 'ACTIVE';
CREATE INDEX idx_carts_restaurant_id ON carts (restaurant_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items (product_id);

CREATE INDEX idx_order_status_history_order_id_created_at ON order_status_history (order_id, created_at);
CREATE INDEX idx_order_status_history_new_status ON order_status_history (new_status);

CREATE INDEX idx_delivery_batches_delivery_user_id_status ON delivery_batches (delivery_user_id, status);
CREATE INDEX idx_delivery_batch_orders_batch_id_sequence ON delivery_batch_orders (delivery_batch_id, sequence_number);
CREATE INDEX idx_delivery_locations_delivery_user_id_recorded_at ON delivery_locations (delivery_user_id, recorded_at);
CREATE INDEX idx_delivery_locations_order_id_recorded_at ON delivery_locations (order_id, recorded_at);
CREATE INDEX idx_delivery_locations_batch_id_recorded_at ON delivery_locations (delivery_batch_id, recorded_at);

CREATE INDEX idx_refunds_payment_id ON refunds (payment_id);
CREATE INDEX idx_refunds_complaint_id ON refunds (complaint_id);
CREATE INDEX idx_refunds_status ON refunds (status);
CREATE INDEX idx_invoices_issued_at ON invoices (issued_at);

CREATE INDEX idx_coupon_redemptions_customer_user_id ON coupon_redemptions (customer_user_id);
CREATE INDEX idx_coupon_redemptions_order_id ON coupon_redemptions (order_id);
CREATE INDEX idx_loyalty_transactions_account_id_created_at ON loyalty_transactions (loyalty_account_id, created_at);
CREATE INDEX idx_loyalty_transactions_order_id ON loyalty_transactions (order_id);
CREATE INDEX idx_restaurant_commissions_restaurant_id_dates ON restaurant_commissions (restaurant_id, starts_at, ends_at);
