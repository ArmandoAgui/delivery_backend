ALTER TABLE delivery_assignments
    ADD COLUMN IF NOT EXISTS delivery_gross_earnings NUMERIC(10, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_platform_commission_percentage NUMERIC(5, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_platform_commission_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_net_earnings NUMERIC(10, 2) NOT NULL DEFAULT 0;

ALTER TABLE delivery_assignments
    DROP CONSTRAINT IF EXISTS chk_delivery_assignments_earnings_non_negative,
    DROP CONSTRAINT IF EXISTS chk_delivery_assignments_commission_percentage_range,
    ADD CONSTRAINT chk_delivery_assignments_earnings_non_negative CHECK (
        delivery_gross_earnings >= 0
        AND delivery_platform_commission_amount >= 0
        AND delivery_net_earnings >= 0
    ),
    ADD CONSTRAINT chk_delivery_assignments_commission_percentage_range CHECK (
        delivery_platform_commission_percentage >= 0
        AND delivery_platform_commission_percentage <= 100
    );

UPDATE delivery_assignments da
SET delivery_gross_earnings = round((o.delivery_fee + o.tip_amount)::numeric, 2),
    delivery_platform_commission_percentage = coalesce(applied_commission.delivery_commission_percentage, 0),
    delivery_platform_commission_amount = round((o.delivery_fee * coalesce(applied_commission.delivery_commission_percentage, 0) / 100)::numeric, 2),
    delivery_net_earnings = round((o.delivery_fee + o.tip_amount - (o.delivery_fee * coalesce(applied_commission.delivery_commission_percentage, 0) / 100))::numeric, 2)
FROM orders o
LEFT JOIN LATERAL (
    SELECT pc.delivery_commission_percentage
    FROM platform_commissions pc
    WHERE pc.starts_at <= coalesce(da.delivered_at, now())
      AND (pc.ends_at IS NULL OR pc.ends_at > coalesce(da.delivered_at, now()))
    ORDER BY pc.starts_at DESC
    LIMIT 1
) applied_commission ON TRUE
WHERE o.id = da.order_id
  AND da.status = 'DELIVERED'
  AND da.delivery_gross_earnings = 0
  AND da.delivery_platform_commission_amount = 0
  AND da.delivery_net_earnings = 0;
