-- Global platform commission configuration.
-- Applies one percentage to every restaurant from starts_at until ends_at.

CREATE TABLE IF NOT EXISTS platform_commissions (
    id BIGSERIAL,
    commission_percentage NUMERIC(5, 2) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_platform_commissions PRIMARY KEY (id),
    CONSTRAINT chk_platform_commissions_percentage_range CHECK (commission_percentage >= 0 AND commission_percentage <= 100),
    CONSTRAINT chk_platform_commissions_date_range CHECK (ends_at IS NULL OR ends_at > starts_at)
);

CREATE INDEX IF NOT EXISTS idx_platform_commissions_dates ON platform_commissions (starts_at, ends_at);

INSERT INTO platform_commissions (commission_percentage, starts_at, ends_at)
SELECT 12.00, NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM platform_commissions);
