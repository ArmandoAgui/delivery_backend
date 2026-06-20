-- Reverts PayPal checkout support while keeping Flyway history consistent for databases
-- where V15 was already applied. The project returns to simulated card payments only.

UPDATE orders
SET status = 'CREATED'
WHERE status IN ('PENDING_PAYMENT', 'PAID');

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS chk_orders_status,
    ADD CONSTRAINT chk_orders_status CHECK (
        status IN (
            'CREATED',
            'CONFIRMED',
            'PREPARING',
            'READY_FOR_PICKUP',
            'ON_THE_WAY',
            'DELIVERED',
            'CANCELLED'
        )
    );

DELETE FROM payments
WHERE provider = 'PAYPAL';

DROP INDEX IF EXISTS uk_payments_paypal_capture_id;
DROP INDEX IF EXISTS uk_payments_paypal_order_id;

ALTER TABLE payments
    DROP CONSTRAINT IF EXISTS chk_payments_provider,
    ADD CONSTRAINT chk_payments_provider CHECK (provider IN ('SIMULATED', 'STRIPE'));

ALTER TABLE payments
    DROP COLUMN IF EXISTS paypal_capture_id,
    DROP COLUMN IF EXISTS paypal_order_id;
