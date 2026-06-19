-- Stores delivery estimation metadata calculated from PostGIS distances or fallback rules.

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS estimated_delivery_minutes INTEGER,
    ADD COLUMN IF NOT EXISTS demand_multiplier NUMERIC(5, 2) NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS distance_km NUMERIC(10, 2);

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS chk_orders_estimated_delivery_minutes_positive,
    ADD CONSTRAINT chk_orders_estimated_delivery_minutes_positive
        CHECK (estimated_delivery_minutes IS NULL OR estimated_delivery_minutes > 0),
    DROP CONSTRAINT IF EXISTS chk_orders_demand_multiplier_positive,
    ADD CONSTRAINT chk_orders_demand_multiplier_positive
        CHECK (demand_multiplier >= 1),
    DROP CONSTRAINT IF EXISTS chk_orders_distance_km_non_negative,
    ADD CONSTRAINT chk_orders_distance_km_non_negative
        CHECK (distance_km IS NULL OR distance_km >= 0);
