-- Adds backend-completion support without touching existing data.

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(120) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_refresh_tokens_user_id_users FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token)
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX IF NOT EXISTS idx_orders_customer_created_at ON orders (customer_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_restaurant_created_at ON orders (restaurant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX IF NOT EXISTS idx_reviews_restaurant_id ON reviews (restaurant_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_account_created_at ON loyalty_transactions (loyalty_account_id, created_at DESC);
