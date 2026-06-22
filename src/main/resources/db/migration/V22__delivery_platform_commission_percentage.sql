ALTER TABLE platform_commissions
    ADD COLUMN IF NOT EXISTS delivery_commission_percentage NUMERIC(5, 2) NOT NULL DEFAULT 10.00;

ALTER TABLE platform_commissions
    DROP CONSTRAINT IF EXISTS chk_platform_commissions_delivery_percentage_range,
    ADD CONSTRAINT chk_platform_commissions_delivery_percentage_range
        CHECK (delivery_commission_percentage >= 0 AND delivery_commission_percentage <= 100);
