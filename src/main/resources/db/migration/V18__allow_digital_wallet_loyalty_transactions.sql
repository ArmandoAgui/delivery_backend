ALTER TABLE loyalty_transactions
    DROP CONSTRAINT IF EXISTS chk_loyalty_transactions_points_not_zero;

ALTER TABLE loyalty_transactions
    DROP CONSTRAINT IF EXISTS chk_loyalty_transactions_points_or_credit_not_zero,
    ADD CONSTRAINT chk_loyalty_transactions_points_or_credit_not_zero
        CHECK (points <> 0 OR credit_amount <> 0);
