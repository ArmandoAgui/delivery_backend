ALTER TABLE order_status_history
    DROP CONSTRAINT IF EXISTS chk_order_status_history_previous_status,
    DROP CONSTRAINT IF EXISTS chk_order_status_history_new_status,
    ADD CONSTRAINT chk_order_status_history_previous_status CHECK (
        previous_status IS NULL OR previous_status IN (
            'CREATED',
            'CONFIRMED',
            'WAITING_FOR_DRIVER',
            'NO_DRIVER_AVAILABLE',
            'REJECTED',
            'PREPARING',
            'READY_FOR_PICKUP',
            'ON_THE_WAY',
            'DELIVERED',
            'CANCELLED'
        )
    ),
    ADD CONSTRAINT chk_order_status_history_new_status CHECK (
        new_status IN (
            'CREATED',
            'CONFIRMED',
            'WAITING_FOR_DRIVER',
            'NO_DRIVER_AVAILABLE',
            'REJECTED',
            'PREPARING',
            'READY_FOR_PICKUP',
            'ON_THE_WAY',
            'DELIVERED',
            'CANCELLED'
        )
    );
