ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS chk_orders_status,
    ADD CONSTRAINT chk_orders_status CHECK (
        status IN (
            'CREATED',
            'CONFIRMED',
            'WAITING_FOR_DRIVER',
            'NO_DRIVER_AVAILABLE',
            'PREPARING',
            'READY_FOR_PICKUP',
            'ON_THE_WAY',
            'DELIVERED',
            'CANCELLED'
        )
    );
