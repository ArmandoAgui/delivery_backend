CREATE TABLE IF NOT EXISTS promotions (
    id BIGSERIAL,
    restaurant_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    discount_percentage INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_promotions PRIMARY KEY (id),
    CONSTRAINT fk_promotions_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id),
    CONSTRAINT chk_promotions_discount_percentage_range CHECK (discount_percentage BETWEEN 1 AND 100),
    CONSTRAINT chk_promotions_date_range CHECK (end_date >= start_date)
);

CREATE INDEX IF NOT EXISTS idx_promotions_restaurant_id ON promotions (restaurant_id);
CREATE INDEX IF NOT EXISTS idx_promotions_active_dates ON promotions (active, start_date, end_date);
