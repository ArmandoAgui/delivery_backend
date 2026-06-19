-- Align complaint workflow with the backend complaint module.

UPDATE complaints
SET status = 'IN_PROGRESS'
WHERE status = 'IN_REVIEW';

UPDATE complaints
SET status = 'RESOLVED'
WHERE status = 'CLOSED';

ALTER TABLE complaints
    DROP CONSTRAINT chk_complaints_status,
    ADD CONSTRAINT chk_complaints_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'));

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_complaints_order_id'
          AND conrelid = 'complaints'::regclass
    ) THEN
        ALTER TABLE complaints
            ADD CONSTRAINT uk_complaints_order_id UNIQUE (order_id);
    END IF;
END;
$$;

CREATE INDEX IF NOT EXISTS idx_refunds_status ON refunds (status);
