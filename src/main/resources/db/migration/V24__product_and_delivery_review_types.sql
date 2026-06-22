ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS review_type VARCHAR(30) NOT NULL DEFAULT 'RESTAURANT';

ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS product_id UUID;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'uk_reviews_order_id_reviewer_user_id'
          AND table_name = 'reviews'
    ) THEN
        ALTER TABLE reviews DROP CONSTRAINT uk_reviews_order_id_reviewer_user_id;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_reviews_product_id_products'
          AND table_name = 'reviews'
    ) THEN
        ALTER TABLE reviews
            ADD CONSTRAINT fk_reviews_product_id_products
                FOREIGN KEY (product_id) REFERENCES products (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_reviews_review_type'
          AND table_name = 'reviews'
    ) THEN
        ALTER TABLE reviews
            ADD CONSTRAINT chk_reviews_review_type
                CHECK (review_type IN ('RESTAURANT', 'PRODUCT', 'DELIVERY'));
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_reviews_order_reviewer_type
    ON reviews (order_id, reviewer_user_id, review_type)
    WHERE product_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_reviews_order_reviewer_product
    ON reviews (order_id, reviewer_user_id, review_type, product_id)
    WHERE product_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON reviews (product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_review_type ON reviews (review_type);
