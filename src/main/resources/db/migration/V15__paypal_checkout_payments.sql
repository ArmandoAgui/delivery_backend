-- Adds PayPal Sandbox checkout support without removing existing simulated payments.

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS chk_orders_status,
    ADD CONSTRAINT chk_orders_status CHECK (
        status IN (
            'PENDING_PAYMENT',
            'PAID',
            'CREATED',
            'CONFIRMED',
            'PREPARING',
            'READY_FOR_PICKUP',
            'ON_THE_WAY',
            'DELIVERED',
            'CANCELLED'
        )
    );

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS paypal_order_id VARCHAR(150),
    ADD COLUMN IF NOT EXISTS paypal_capture_id VARCHAR(150);

UPDATE payments
SET paypal_order_id = provider_transaction_id
WHERE provider = 'PAYPAL'
  AND paypal_order_id IS NULL
  AND provider_transaction_id IS NOT NULL;

ALTER TABLE payments
    DROP CONSTRAINT IF EXISTS chk_payments_provider,
    ADD CONSTRAINT chk_payments_provider CHECK (provider IN ('SIMULATED', 'STRIPE', 'PAYPAL'));

CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_paypal_order_id
    ON payments (paypal_order_id)
    WHERE paypal_order_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_paypal_capture_id
    ON payments (paypal_capture_id)
    WHERE paypal_capture_id IS NOT NULL;
