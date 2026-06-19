-- Adds delivery request lifecycle support without removing existing delivery data.

ALTER TABLE delivery_assignments
    DROP CONSTRAINT IF EXISTS chk_delivery_assignments_status;

ALTER TABLE delivery_assignments
    ADD CONSTRAINT chk_delivery_assignments_status
    CHECK (status IN ('OFFERED', 'ASSIGNED', 'REJECTED', 'PICKED_UP', 'ON_THE_WAY', 'DELIVERED', 'CANCELLED'));

CREATE TABLE IF NOT EXISTS delivery_profiles (
    delivery_user_id UUID PRIMARY KEY,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_delivery_profiles_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS delivery_assignment_rejections (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    delivery_user_id UUID NOT NULL,
    rejected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reason VARCHAR(255),
    CONSTRAINT fk_delivery_assignment_rejections_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_delivery_assignment_rejections_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id),
    CONSTRAINT uk_delivery_assignment_rejections_order_delivery UNIQUE (order_id, delivery_user_id)
);

ALTER TABLE delivery_locations
    DROP CONSTRAINT IF EXISTS chk_delivery_locations_tracking_target;

CREATE INDEX IF NOT EXISTS idx_delivery_assignment_rejections_order_user
    ON delivery_assignment_rejections (order_id, delivery_user_id);

CREATE INDEX IF NOT EXISTS idx_delivery_profiles_available
    ON delivery_profiles (is_available);

