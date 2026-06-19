-- Aligns delivery assignment lifecycle statuses with the backend delivery module.

ALTER TABLE delivery_assignments
    DROP CONSTRAINT chk_delivery_assignments_status;

ALTER TABLE delivery_assignments
    ADD CONSTRAINT chk_delivery_assignments_status
    CHECK (status IN ('ASSIGNED', 'PICKED_UP', 'ON_THE_WAY', 'DELIVERED', 'CANCELLED'));
