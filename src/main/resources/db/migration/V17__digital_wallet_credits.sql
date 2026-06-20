ALTER TABLE loyalty_accounts
    ADD COLUMN IF NOT EXISTS credit_balance NUMERIC(12, 2) NOT NULL DEFAULT 0;

ALTER TABLE loyalty_transactions
    ADD COLUMN IF NOT EXISTS credit_amount NUMERIC(12, 2) NOT NULL DEFAULT 0;

ALTER TABLE loyalty_accounts
    DROP CONSTRAINT IF EXISTS chk_loyalty_accounts_credit_balance_non_negative,
    ADD CONSTRAINT chk_loyalty_accounts_credit_balance_non_negative CHECK (credit_balance >= 0);
