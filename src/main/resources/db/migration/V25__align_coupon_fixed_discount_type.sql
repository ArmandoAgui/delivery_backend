-- Align coupon fixed discount values with the Java enum DiscountType.FIXED.
-- Older schema constraints accepted FIXED_AMOUNT, while the application sends FIXED.

UPDATE coupons
SET discount_type = 'FIXED'
WHERE discount_type = 'FIXED_AMOUNT';

ALTER TABLE coupons
    DROP CONSTRAINT IF EXISTS chk_coupons_discount_type;

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_discount_type
    CHECK (discount_type IN ('PERCENTAGE', 'FIXED'));
