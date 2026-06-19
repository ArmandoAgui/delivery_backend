-- V3__convert_transactional_ids_to_uuid_v7.sql
-- Converts user-facing and high-transaction tables from sequential BIGSERIAL ids
-- to UUID primary keys while keeping catalog/configuration table ids numeric.
-- PostgreSQL only stores UUID values. The backend must generate UUID v7 ids
-- before insert; this migration intentionally does not create any UUID generator
-- function in the database.

-- ============================================================================
-- Preconditions
-- ============================================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM users
        UNION ALL SELECT 1 FROM addresses
        UNION ALL SELECT 1 FROM restaurants
        UNION ALL SELECT 1 FROM categories
        UNION ALL SELECT 1 FROM products
        UNION ALL SELECT 1 FROM orders
        UNION ALL SELECT 1 FROM order_items
        UNION ALL SELECT 1 FROM delivery_assignments
        UNION ALL SELECT 1 FROM payments
        UNION ALL SELECT 1 FROM reviews
        UNION ALL SELECT 1 FROM complaints
        UNION ALL SELECT 1 FROM restaurant_schedules
        UNION ALL SELECT 1 FROM carts
        UNION ALL SELECT 1 FROM cart_items
        UNION ALL SELECT 1 FROM order_status_history
        UNION ALL SELECT 1 FROM delivery_batches
        UNION ALL SELECT 1 FROM delivery_batch_orders
        UNION ALL SELECT 1 FROM delivery_locations
        UNION ALL SELECT 1 FROM refunds
        UNION ALL SELECT 1 FROM invoices
        UNION ALL SELECT 1 FROM coupon_redemptions
        UNION ALL SELECT 1 FROM loyalty_accounts
        UNION ALL SELECT 1 FROM loyalty_transactions
        UNION ALL SELECT 1 FROM restaurant_commissions
        LIMIT 1
    ) THEN
        RAISE EXCEPTION 'V3 requires empty application tables because UUID v7 ids are generated only by the backend.';
    END IF;
END;
$$;

-- ============================================================================
-- New UUID Primary Keys
-- ============================================================================

ALTER TABLE users ADD COLUMN id_uuid UUID;
ALTER TABLE addresses ADD COLUMN id_uuid UUID;
ALTER TABLE restaurants ADD COLUMN id_uuid UUID;
ALTER TABLE products ADD COLUMN id_uuid UUID;
ALTER TABLE orders ADD COLUMN id_uuid UUID;
ALTER TABLE order_items ADD COLUMN id_uuid UUID;
ALTER TABLE delivery_assignments ADD COLUMN id_uuid UUID;
ALTER TABLE payments ADD COLUMN id_uuid UUID;
ALTER TABLE reviews ADD COLUMN id_uuid UUID;
ALTER TABLE complaints ADD COLUMN id_uuid UUID;
ALTER TABLE carts ADD COLUMN id_uuid UUID;
ALTER TABLE cart_items ADD COLUMN id_uuid UUID;
ALTER TABLE order_status_history ADD COLUMN id_uuid UUID;
ALTER TABLE delivery_batches ADD COLUMN id_uuid UUID;
ALTER TABLE delivery_batch_orders ADD COLUMN id_uuid UUID;
ALTER TABLE delivery_locations ADD COLUMN id_uuid UUID;
ALTER TABLE refunds ADD COLUMN id_uuid UUID;
ALTER TABLE invoices ADD COLUMN id_uuid UUID;
ALTER TABLE coupon_redemptions ADD COLUMN id_uuid UUID;
ALTER TABLE loyalty_accounts ADD COLUMN id_uuid UUID;
ALTER TABLE loyalty_transactions ADD COLUMN id_uuid UUID;

ALTER TABLE users ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE addresses ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE restaurants ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE products ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE orders ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE order_items ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE delivery_assignments ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE payments ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE reviews ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE complaints ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE carts ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE cart_items ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE order_status_history ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE delivery_batches ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE delivery_batch_orders ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE delivery_locations ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE refunds ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE invoices ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE coupon_redemptions ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE loyalty_accounts ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE loyalty_transactions ALTER COLUMN id_uuid SET NOT NULL;

-- ============================================================================
-- New UUID Foreign Keys
-- ============================================================================

ALTER TABLE addresses ADD COLUMN user_id_uuid UUID;
ALTER TABLE restaurants ADD COLUMN owner_user_id_uuid UUID;
ALTER TABLE categories ADD COLUMN restaurant_id_uuid UUID;
ALTER TABLE products ADD COLUMN restaurant_id_uuid UUID;
ALTER TABLE orders ADD COLUMN customer_user_id_uuid UUID;
ALTER TABLE orders ADD COLUMN restaurant_id_uuid UUID;
ALTER TABLE orders ADD COLUMN delivery_address_id_uuid UUID;
ALTER TABLE order_items ADD COLUMN order_id_uuid UUID;
ALTER TABLE order_items ADD COLUMN product_id_uuid UUID;
ALTER TABLE delivery_assignments ADD COLUMN order_id_uuid UUID;
ALTER TABLE delivery_assignments ADD COLUMN delivery_user_id_uuid UUID;
ALTER TABLE payments ADD COLUMN order_id_uuid UUID;
ALTER TABLE reviews ADD COLUMN order_id_uuid UUID;
ALTER TABLE reviews ADD COLUMN reviewer_user_id_uuid UUID;
ALTER TABLE reviews ADD COLUMN restaurant_id_uuid UUID;
ALTER TABLE reviews ADD COLUMN delivery_user_id_uuid UUID;
ALTER TABLE complaints ADD COLUMN order_id_uuid UUID;
ALTER TABLE complaints ADD COLUMN customer_user_id_uuid UUID;
ALTER TABLE complaints ADD COLUMN assigned_admin_user_id_uuid UUID;
ALTER TABLE restaurant_schedules ADD COLUMN restaurant_id_uuid UUID;
ALTER TABLE carts ADD COLUMN customer_user_id_uuid UUID;
ALTER TABLE carts ADD COLUMN restaurant_id_uuid UUID;
ALTER TABLE cart_items ADD COLUMN cart_id_uuid UUID;
ALTER TABLE cart_items ADD COLUMN product_id_uuid UUID;
ALTER TABLE order_status_history ADD COLUMN order_id_uuid UUID;
ALTER TABLE order_status_history ADD COLUMN changed_by_user_id_uuid UUID;
ALTER TABLE delivery_batches ADD COLUMN delivery_user_id_uuid UUID;
ALTER TABLE delivery_batch_orders ADD COLUMN delivery_batch_id_uuid UUID;
ALTER TABLE delivery_batch_orders ADD COLUMN order_id_uuid UUID;
ALTER TABLE delivery_locations ADD COLUMN delivery_user_id_uuid UUID;
ALTER TABLE delivery_locations ADD COLUMN order_id_uuid UUID;
ALTER TABLE delivery_locations ADD COLUMN delivery_batch_id_uuid UUID;
ALTER TABLE refunds ADD COLUMN payment_id_uuid UUID;
ALTER TABLE refunds ADD COLUMN complaint_id_uuid UUID;
ALTER TABLE invoices ADD COLUMN order_id_uuid UUID;
ALTER TABLE invoices ADD COLUMN payment_id_uuid UUID;
ALTER TABLE coupon_redemptions ADD COLUMN order_id_uuid UUID;
ALTER TABLE coupon_redemptions ADD COLUMN customer_user_id_uuid UUID;
ALTER TABLE loyalty_accounts ADD COLUMN customer_user_id_uuid UUID;
ALTER TABLE loyalty_transactions ADD COLUMN loyalty_account_id_uuid UUID;
ALTER TABLE loyalty_transactions ADD COLUMN order_id_uuid UUID;
ALTER TABLE restaurant_commissions ADD COLUMN restaurant_id_uuid UUID;

UPDATE addresses a SET user_id_uuid = u.id_uuid FROM users u WHERE a.user_id = u.id;
UPDATE restaurants r SET owner_user_id_uuid = u.id_uuid FROM users u WHERE r.owner_user_id = u.id;
UPDATE categories c SET restaurant_id_uuid = r.id_uuid FROM restaurants r WHERE c.restaurant_id = r.id;
UPDATE products p SET restaurant_id_uuid = r.id_uuid FROM restaurants r WHERE p.restaurant_id = r.id;
UPDATE orders o SET customer_user_id_uuid = u.id_uuid FROM users u WHERE o.customer_user_id = u.id;
UPDATE orders o SET restaurant_id_uuid = r.id_uuid FROM restaurants r WHERE o.restaurant_id = r.id;
UPDATE orders o SET delivery_address_id_uuid = a.id_uuid FROM addresses a WHERE o.delivery_address_id = a.id;
UPDATE order_items oi SET order_id_uuid = o.id_uuid FROM orders o WHERE oi.order_id = o.id;
UPDATE order_items oi SET product_id_uuid = p.id_uuid FROM products p WHERE oi.product_id = p.id;
UPDATE delivery_assignments da SET order_id_uuid = o.id_uuid FROM orders o WHERE da.order_id = o.id;
UPDATE delivery_assignments da SET delivery_user_id_uuid = u.id_uuid FROM users u WHERE da.delivery_user_id = u.id;
UPDATE payments p SET order_id_uuid = o.id_uuid FROM orders o WHERE p.order_id = o.id;
UPDATE reviews r SET order_id_uuid = o.id_uuid FROM orders o WHERE r.order_id = o.id;
UPDATE reviews r SET reviewer_user_id_uuid = u.id_uuid FROM users u WHERE r.reviewer_user_id = u.id;
UPDATE reviews rv SET restaurant_id_uuid = r.id_uuid FROM restaurants r WHERE rv.restaurant_id = r.id;
UPDATE reviews r SET delivery_user_id_uuid = u.id_uuid FROM users u WHERE r.delivery_user_id = u.id;
UPDATE complaints c SET order_id_uuid = o.id_uuid FROM orders o WHERE c.order_id = o.id;
UPDATE complaints c SET customer_user_id_uuid = u.id_uuid FROM users u WHERE c.customer_user_id = u.id;
UPDATE complaints c SET assigned_admin_user_id_uuid = u.id_uuid FROM users u WHERE c.assigned_admin_user_id = u.id;
UPDATE restaurant_schedules rs SET restaurant_id_uuid = r.id_uuid FROM restaurants r WHERE rs.restaurant_id = r.id;
UPDATE carts c SET customer_user_id_uuid = u.id_uuid FROM users u WHERE c.customer_user_id = u.id;
UPDATE carts c SET restaurant_id_uuid = r.id_uuid FROM restaurants r WHERE c.restaurant_id = r.id;
UPDATE cart_items ci SET cart_id_uuid = c.id_uuid FROM carts c WHERE ci.cart_id = c.id;
UPDATE cart_items ci SET product_id_uuid = p.id_uuid FROM products p WHERE ci.product_id = p.id;
UPDATE order_status_history osh SET order_id_uuid = o.id_uuid FROM orders o WHERE osh.order_id = o.id;
UPDATE order_status_history osh SET changed_by_user_id_uuid = u.id_uuid FROM users u WHERE osh.changed_by_user_id = u.id;
UPDATE delivery_batches db SET delivery_user_id_uuid = u.id_uuid FROM users u WHERE db.delivery_user_id = u.id;
UPDATE delivery_batch_orders dbo SET delivery_batch_id_uuid = db.id_uuid FROM delivery_batches db WHERE dbo.delivery_batch_id = db.id;
UPDATE delivery_batch_orders dbo SET order_id_uuid = o.id_uuid FROM orders o WHERE dbo.order_id = o.id;
UPDATE delivery_locations dl SET delivery_user_id_uuid = u.id_uuid FROM users u WHERE dl.delivery_user_id = u.id;
UPDATE delivery_locations dl SET order_id_uuid = o.id_uuid FROM orders o WHERE dl.order_id = o.id;
UPDATE delivery_locations dl SET delivery_batch_id_uuid = db.id_uuid FROM delivery_batches db WHERE dl.delivery_batch_id = db.id;
UPDATE refunds r SET payment_id_uuid = p.id_uuid FROM payments p WHERE r.payment_id = p.id;
UPDATE refunds r SET complaint_id_uuid = c.id_uuid FROM complaints c WHERE r.complaint_id = c.id;
UPDATE invoices i SET order_id_uuid = o.id_uuid FROM orders o WHERE i.order_id = o.id;
UPDATE invoices i SET payment_id_uuid = p.id_uuid FROM payments p WHERE i.payment_id = p.id;
UPDATE coupon_redemptions cr SET order_id_uuid = o.id_uuid FROM orders o WHERE cr.order_id = o.id;
UPDATE coupon_redemptions cr SET customer_user_id_uuid = u.id_uuid FROM users u WHERE cr.customer_user_id = u.id;
UPDATE loyalty_accounts la SET customer_user_id_uuid = u.id_uuid FROM users u WHERE la.customer_user_id = u.id;
UPDATE loyalty_transactions lt SET loyalty_account_id_uuid = la.id_uuid FROM loyalty_accounts la WHERE lt.loyalty_account_id = la.id;
UPDATE loyalty_transactions lt SET order_id_uuid = o.id_uuid FROM orders o WHERE lt.order_id = o.id;
UPDATE restaurant_commissions rc SET restaurant_id_uuid = r.id_uuid FROM restaurants r WHERE rc.restaurant_id = r.id;

ALTER TABLE addresses ALTER COLUMN user_id_uuid SET NOT NULL;
ALTER TABLE restaurants ALTER COLUMN owner_user_id_uuid SET NOT NULL;
ALTER TABLE categories ALTER COLUMN restaurant_id_uuid SET NOT NULL;
ALTER TABLE products ALTER COLUMN restaurant_id_uuid SET NOT NULL;
ALTER TABLE orders ALTER COLUMN customer_user_id_uuid SET NOT NULL;
ALTER TABLE orders ALTER COLUMN restaurant_id_uuid SET NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_address_id_uuid SET NOT NULL;
ALTER TABLE order_items ALTER COLUMN order_id_uuid SET NOT NULL;
ALTER TABLE order_items ALTER COLUMN product_id_uuid SET NOT NULL;
ALTER TABLE delivery_assignments ALTER COLUMN order_id_uuid SET NOT NULL;
ALTER TABLE delivery_assignments ALTER COLUMN delivery_user_id_uuid SET NOT NULL;
ALTER TABLE payments ALTER COLUMN order_id_uuid SET NOT NULL;
ALTER TABLE reviews ALTER COLUMN order_id_uuid SET NOT NULL;
ALTER TABLE reviews ALTER COLUMN reviewer_user_id_uuid SET NOT NULL;
ALTER TABLE reviews ALTER COLUMN restaurant_id_uuid SET NOT NULL;
ALTER TABLE complaints ALTER COLUMN order_id_uuid SET NOT NULL;
ALTER TABLE complaints ALTER COLUMN customer_user_id_uuid SET NOT NULL;
ALTER TABLE restaurant_schedules ALTER COLUMN restaurant_id_uuid SET NOT NULL;
ALTER TABLE carts ALTER COLUMN customer_user_id_uuid SET NOT NULL;
ALTER TABLE carts ALTER COLUMN restaurant_id_uuid SET NOT NULL;
ALTER TABLE cart_items ALTER COLUMN cart_id_uuid SET NOT NULL;
ALTER TABLE cart_items ALTER COLUMN product_id_uuid SET NOT NULL;
ALTER TABLE order_status_history ALTER COLUMN order_id_uuid SET NOT NULL;
ALTER TABLE delivery_batches ALTER COLUMN delivery_user_id_uuid SET NOT NULL;
ALTER TABLE delivery_batch_orders ALTER COLUMN delivery_batch_id_uuid SET NOT NULL;
ALTER TABLE delivery_batch_orders ALTER COLUMN order_id_uuid SET NOT NULL;
ALTER TABLE delivery_locations ALTER COLUMN delivery_user_id_uuid SET NOT NULL;
ALTER TABLE refunds ALTER COLUMN payment_id_uuid SET NOT NULL;
ALTER TABLE invoices ALTER COLUMN order_id_uuid SET NOT NULL;
ALTER TABLE coupon_redemptions ALTER COLUMN order_id_uuid SET NOT NULL;
ALTER TABLE coupon_redemptions ALTER COLUMN customer_user_id_uuid SET NOT NULL;
ALTER TABLE loyalty_accounts ALTER COLUMN customer_user_id_uuid SET NOT NULL;
ALTER TABLE loyalty_transactions ALTER COLUMN loyalty_account_id_uuid SET NOT NULL;
ALTER TABLE restaurant_commissions ALTER COLUMN restaurant_id_uuid SET NOT NULL;

-- ============================================================================
-- Drop Constraints And Indexes Using Old BIGINT Keys
-- ============================================================================

ALTER TABLE restaurant_commissions DROP CONSTRAINT fk_restaurant_commissions_restaurant_id_restaurants;
ALTER TABLE loyalty_transactions DROP CONSTRAINT fk_loyalty_transactions_order_id_orders;
ALTER TABLE loyalty_transactions DROP CONSTRAINT fk_loyalty_transactions_loyalty_account_id_loyalty_accounts;
ALTER TABLE loyalty_accounts DROP CONSTRAINT fk_loyalty_accounts_customer_user_id_users;
ALTER TABLE coupon_redemptions DROP CONSTRAINT fk_coupon_redemptions_customer_user_id_users;
ALTER TABLE coupon_redemptions DROP CONSTRAINT fk_coupon_redemptions_order_id_orders;
ALTER TABLE invoices DROP CONSTRAINT fk_invoices_payment_id_payments;
ALTER TABLE invoices DROP CONSTRAINT fk_invoices_order_id_orders;
ALTER TABLE refunds DROP CONSTRAINT fk_refunds_complaint_id_complaints;
ALTER TABLE refunds DROP CONSTRAINT fk_refunds_payment_id_payments;
ALTER TABLE delivery_locations DROP CONSTRAINT fk_delivery_locations_delivery_batch_id_delivery_batches;
ALTER TABLE delivery_locations DROP CONSTRAINT fk_delivery_locations_order_id_orders;
ALTER TABLE delivery_locations DROP CONSTRAINT fk_delivery_locations_delivery_user_id_users;
ALTER TABLE delivery_batch_orders DROP CONSTRAINT fk_delivery_batch_orders_order_id_orders;
ALTER TABLE delivery_batch_orders DROP CONSTRAINT fk_delivery_batch_orders_delivery_batch_id_delivery_batches;
ALTER TABLE delivery_batches DROP CONSTRAINT fk_delivery_batches_delivery_user_id_users;
ALTER TABLE order_status_history DROP CONSTRAINT fk_order_status_history_changed_by_user_id_users;
ALTER TABLE order_status_history DROP CONSTRAINT fk_order_status_history_order_id_orders;
ALTER TABLE cart_items DROP CONSTRAINT fk_cart_items_product_id_products;
ALTER TABLE cart_items DROP CONSTRAINT fk_cart_items_cart_id_carts;
ALTER TABLE carts DROP CONSTRAINT fk_carts_restaurant_id_restaurants;
ALTER TABLE carts DROP CONSTRAINT fk_carts_customer_user_id_users;
ALTER TABLE restaurant_schedules DROP CONSTRAINT fk_restaurant_schedules_restaurant_id_restaurants;
ALTER TABLE complaints DROP CONSTRAINT fk_complaints_assigned_admin_user_id_users;
ALTER TABLE complaints DROP CONSTRAINT fk_complaints_customer_user_id_users;
ALTER TABLE complaints DROP CONSTRAINT fk_complaints_order_id_orders;
ALTER TABLE reviews DROP CONSTRAINT fk_reviews_delivery_user_id_users;
ALTER TABLE reviews DROP CONSTRAINT fk_reviews_restaurant_id_restaurants;
ALTER TABLE reviews DROP CONSTRAINT fk_reviews_reviewer_user_id_users;
ALTER TABLE reviews DROP CONSTRAINT fk_reviews_order_id_orders;
ALTER TABLE payments DROP CONSTRAINT fk_payments_order_id_orders;
ALTER TABLE delivery_assignments DROP CONSTRAINT fk_delivery_assignments_delivery_user_id_users;
ALTER TABLE delivery_assignments DROP CONSTRAINT fk_delivery_assignments_order_id_orders;
ALTER TABLE order_items DROP CONSTRAINT fk_order_items_product_id_products;
ALTER TABLE order_items DROP CONSTRAINT fk_order_items_order_id_orders;
ALTER TABLE orders DROP CONSTRAINT fk_orders_delivery_address_id_addresses;
ALTER TABLE orders DROP CONSTRAINT fk_orders_restaurant_id_restaurants;
ALTER TABLE orders DROP CONSTRAINT fk_orders_customer_user_id_users;
ALTER TABLE products DROP CONSTRAINT fk_products_category_id_restaurant_id_categories;
ALTER TABLE products DROP CONSTRAINT fk_products_restaurant_id_restaurants;
ALTER TABLE categories DROP CONSTRAINT fk_categories_restaurant_id_restaurants;
ALTER TABLE restaurants DROP CONSTRAINT fk_restaurants_owner_user_id_users;
ALTER TABLE addresses DROP CONSTRAINT fk_addresses_user_id_users;

ALTER TABLE restaurant_commissions DROP CONSTRAINT pk_restaurant_commissions;
ALTER TABLE loyalty_transactions DROP CONSTRAINT pk_loyalty_transactions;
ALTER TABLE loyalty_accounts DROP CONSTRAINT pk_loyalty_accounts;
ALTER TABLE coupon_redemptions DROP CONSTRAINT pk_coupon_redemptions;
ALTER TABLE invoices DROP CONSTRAINT pk_invoices;
ALTER TABLE refunds DROP CONSTRAINT pk_refunds;
ALTER TABLE delivery_locations DROP CONSTRAINT pk_delivery_locations;
ALTER TABLE delivery_batch_orders DROP CONSTRAINT pk_delivery_batch_orders;
ALTER TABLE delivery_batches DROP CONSTRAINT pk_delivery_batches;
ALTER TABLE order_status_history DROP CONSTRAINT pk_order_status_history;
ALTER TABLE cart_items DROP CONSTRAINT pk_cart_items;
ALTER TABLE carts DROP CONSTRAINT pk_carts;
ALTER TABLE restaurant_schedules DROP CONSTRAINT pk_restaurant_schedules;
ALTER TABLE complaints DROP CONSTRAINT pk_complaints;
ALTER TABLE reviews DROP CONSTRAINT pk_reviews;
ALTER TABLE payments DROP CONSTRAINT pk_payments;
ALTER TABLE delivery_assignments DROP CONSTRAINT pk_delivery_assignments;
ALTER TABLE order_items DROP CONSTRAINT pk_order_items;
ALTER TABLE orders DROP CONSTRAINT pk_orders;
ALTER TABLE products DROP CONSTRAINT pk_products;
ALTER TABLE restaurants DROP CONSTRAINT pk_restaurants;
ALTER TABLE addresses DROP CONSTRAINT pk_addresses;
ALTER TABLE users DROP CONSTRAINT pk_users;

ALTER TABLE restaurants DROP CONSTRAINT uk_restaurants_owner_user_id;
ALTER TABLE categories DROP CONSTRAINT uk_categories_restaurant_id_name;
ALTER TABLE categories DROP CONSTRAINT uk_categories_id_restaurant_id;
ALTER TABLE products DROP CONSTRAINT uk_products_restaurant_id_name;
ALTER TABLE order_items DROP CONSTRAINT uk_order_items_order_id_product_id;
ALTER TABLE delivery_assignments DROP CONSTRAINT uk_delivery_assignments_order_id;
ALTER TABLE reviews DROP CONSTRAINT uk_reviews_order_id_reviewer_user_id;
ALTER TABLE restaurant_schedules DROP CONSTRAINT uk_restaurant_schedules_restaurant_id_day_of_week;
ALTER TABLE cart_items DROP CONSTRAINT uk_cart_items_cart_id_product_id;
ALTER TABLE delivery_batch_orders DROP CONSTRAINT uk_delivery_batch_orders_order_id;
ALTER TABLE delivery_batch_orders DROP CONSTRAINT uk_delivery_batch_orders_batch_id_sequence;
ALTER TABLE invoices DROP CONSTRAINT uk_invoices_order_id;
ALTER TABLE coupon_redemptions DROP CONSTRAINT uk_coupon_redemptions_coupon_id_order_id;
ALTER TABLE loyalty_accounts DROP CONSTRAINT uk_loyalty_accounts_customer_user_id;

DROP INDEX IF EXISTS idx_addresses_user_id;
DROP INDEX IF EXISTS idx_addresses_user_id_is_default;
DROP INDEX IF EXISTS idx_restaurants_owner_user_id;
DROP INDEX IF EXISTS idx_categories_restaurant_id;
DROP INDEX IF EXISTS idx_products_restaurant_id;
DROP INDEX IF EXISTS idx_products_category_id;
DROP INDEX IF EXISTS idx_products_restaurant_id_is_available;
DROP INDEX IF EXISTS idx_orders_customer_user_id;
DROP INDEX IF EXISTS idx_orders_restaurant_id;
DROP INDEX IF EXISTS idx_orders_delivery_address_id;
DROP INDEX IF EXISTS idx_order_items_order_id;
DROP INDEX IF EXISTS idx_order_items_product_id;
DROP INDEX IF EXISTS idx_delivery_assignments_delivery_user_id;
DROP INDEX IF EXISTS idx_payments_order_id;
DROP INDEX IF EXISTS idx_reviews_restaurant_id;
DROP INDEX IF EXISTS idx_reviews_delivery_user_id;
DROP INDEX IF EXISTS idx_complaints_order_id;
DROP INDEX IF EXISTS idx_complaints_customer_user_id;
DROP INDEX IF EXISTS idx_restaurant_schedules_restaurant_id_day;
DROP INDEX IF EXISTS idx_carts_customer_user_id_status;
DROP INDEX IF EXISTS uk_carts_active_customer_restaurant;
DROP INDEX IF EXISTS idx_carts_restaurant_id;
DROP INDEX IF EXISTS idx_cart_items_cart_id;
DROP INDEX IF EXISTS idx_cart_items_product_id;
DROP INDEX IF EXISTS idx_order_status_history_order_id_created_at;
DROP INDEX IF EXISTS idx_delivery_batches_delivery_user_id_status;
DROP INDEX IF EXISTS idx_delivery_batch_orders_batch_id_sequence;
DROP INDEX IF EXISTS idx_delivery_locations_delivery_user_id_recorded_at;
DROP INDEX IF EXISTS idx_delivery_locations_order_id_recorded_at;
DROP INDEX IF EXISTS idx_delivery_locations_batch_id_recorded_at;
DROP INDEX IF EXISTS idx_refunds_payment_id;
DROP INDEX IF EXISTS idx_refunds_complaint_id;
DROP INDEX IF EXISTS idx_coupon_redemptions_customer_user_id;
DROP INDEX IF EXISTS idx_coupon_redemptions_order_id;
DROP INDEX IF EXISTS idx_loyalty_transactions_account_id_created_at;
DROP INDEX IF EXISTS idx_loyalty_transactions_order_id;
DROP INDEX IF EXISTS idx_restaurant_commissions_restaurant_id_dates;

-- ============================================================================
-- Replace Columns
-- ============================================================================

ALTER TABLE addresses DROP COLUMN user_id;
ALTER TABLE addresses RENAME COLUMN user_id_uuid TO user_id;
ALTER TABLE restaurants DROP COLUMN owner_user_id;
ALTER TABLE restaurants RENAME COLUMN owner_user_id_uuid TO owner_user_id;
ALTER TABLE categories DROP COLUMN restaurant_id;
ALTER TABLE categories RENAME COLUMN restaurant_id_uuid TO restaurant_id;
ALTER TABLE products DROP COLUMN restaurant_id;
ALTER TABLE products RENAME COLUMN restaurant_id_uuid TO restaurant_id;
ALTER TABLE orders DROP COLUMN customer_user_id;
ALTER TABLE orders RENAME COLUMN customer_user_id_uuid TO customer_user_id;
ALTER TABLE orders DROP COLUMN restaurant_id;
ALTER TABLE orders RENAME COLUMN restaurant_id_uuid TO restaurant_id;
ALTER TABLE orders DROP COLUMN delivery_address_id;
ALTER TABLE orders RENAME COLUMN delivery_address_id_uuid TO delivery_address_id;
ALTER TABLE order_items DROP COLUMN order_id;
ALTER TABLE order_items RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE order_items DROP COLUMN product_id;
ALTER TABLE order_items RENAME COLUMN product_id_uuid TO product_id;
ALTER TABLE delivery_assignments DROP COLUMN order_id;
ALTER TABLE delivery_assignments RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE delivery_assignments DROP COLUMN delivery_user_id;
ALTER TABLE delivery_assignments RENAME COLUMN delivery_user_id_uuid TO delivery_user_id;
ALTER TABLE payments DROP COLUMN order_id;
ALTER TABLE payments RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE reviews DROP COLUMN order_id;
ALTER TABLE reviews RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE reviews DROP COLUMN reviewer_user_id;
ALTER TABLE reviews RENAME COLUMN reviewer_user_id_uuid TO reviewer_user_id;
ALTER TABLE reviews DROP COLUMN restaurant_id;
ALTER TABLE reviews RENAME COLUMN restaurant_id_uuid TO restaurant_id;
ALTER TABLE reviews DROP COLUMN delivery_user_id;
ALTER TABLE reviews RENAME COLUMN delivery_user_id_uuid TO delivery_user_id;
ALTER TABLE complaints DROP COLUMN order_id;
ALTER TABLE complaints RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE complaints DROP COLUMN customer_user_id;
ALTER TABLE complaints RENAME COLUMN customer_user_id_uuid TO customer_user_id;
ALTER TABLE complaints DROP COLUMN assigned_admin_user_id;
ALTER TABLE complaints RENAME COLUMN assigned_admin_user_id_uuid TO assigned_admin_user_id;
ALTER TABLE restaurant_schedules DROP COLUMN restaurant_id;
ALTER TABLE restaurant_schedules RENAME COLUMN restaurant_id_uuid TO restaurant_id;
ALTER TABLE carts DROP COLUMN customer_user_id;
ALTER TABLE carts RENAME COLUMN customer_user_id_uuid TO customer_user_id;
ALTER TABLE carts DROP COLUMN restaurant_id;
ALTER TABLE carts RENAME COLUMN restaurant_id_uuid TO restaurant_id;
ALTER TABLE cart_items DROP COLUMN cart_id;
ALTER TABLE cart_items RENAME COLUMN cart_id_uuid TO cart_id;
ALTER TABLE cart_items DROP COLUMN product_id;
ALTER TABLE cart_items RENAME COLUMN product_id_uuid TO product_id;
ALTER TABLE order_status_history DROP COLUMN order_id;
ALTER TABLE order_status_history RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE order_status_history DROP COLUMN changed_by_user_id;
ALTER TABLE order_status_history RENAME COLUMN changed_by_user_id_uuid TO changed_by_user_id;
ALTER TABLE delivery_batches DROP COLUMN delivery_user_id;
ALTER TABLE delivery_batches RENAME COLUMN delivery_user_id_uuid TO delivery_user_id;
ALTER TABLE delivery_batch_orders DROP COLUMN delivery_batch_id;
ALTER TABLE delivery_batch_orders RENAME COLUMN delivery_batch_id_uuid TO delivery_batch_id;
ALTER TABLE delivery_batch_orders DROP COLUMN order_id;
ALTER TABLE delivery_batch_orders RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE delivery_locations DROP COLUMN delivery_user_id;
ALTER TABLE delivery_locations RENAME COLUMN delivery_user_id_uuid TO delivery_user_id;
ALTER TABLE delivery_locations DROP COLUMN order_id;
ALTER TABLE delivery_locations RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE delivery_locations DROP COLUMN delivery_batch_id;
ALTER TABLE delivery_locations RENAME COLUMN delivery_batch_id_uuid TO delivery_batch_id;
ALTER TABLE refunds DROP COLUMN payment_id;
ALTER TABLE refunds RENAME COLUMN payment_id_uuid TO payment_id;
ALTER TABLE refunds DROP COLUMN complaint_id;
ALTER TABLE refunds RENAME COLUMN complaint_id_uuid TO complaint_id;
ALTER TABLE invoices DROP COLUMN order_id;
ALTER TABLE invoices RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE invoices DROP COLUMN payment_id;
ALTER TABLE invoices RENAME COLUMN payment_id_uuid TO payment_id;
ALTER TABLE coupon_redemptions DROP COLUMN order_id;
ALTER TABLE coupon_redemptions RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE coupon_redemptions DROP COLUMN customer_user_id;
ALTER TABLE coupon_redemptions RENAME COLUMN customer_user_id_uuid TO customer_user_id;
ALTER TABLE loyalty_accounts DROP COLUMN customer_user_id;
ALTER TABLE loyalty_accounts RENAME COLUMN customer_user_id_uuid TO customer_user_id;
ALTER TABLE loyalty_transactions DROP COLUMN loyalty_account_id;
ALTER TABLE loyalty_transactions RENAME COLUMN loyalty_account_id_uuid TO loyalty_account_id;
ALTER TABLE loyalty_transactions DROP COLUMN order_id;
ALTER TABLE loyalty_transactions RENAME COLUMN order_id_uuid TO order_id;
ALTER TABLE restaurant_commissions DROP COLUMN restaurant_id;
ALTER TABLE restaurant_commissions RENAME COLUMN restaurant_id_uuid TO restaurant_id;

ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN id_uuid TO id;
ALTER TABLE addresses DROP COLUMN id;
ALTER TABLE addresses RENAME COLUMN id_uuid TO id;
ALTER TABLE restaurants DROP COLUMN id;
ALTER TABLE restaurants RENAME COLUMN id_uuid TO id;
ALTER TABLE products DROP COLUMN id;
ALTER TABLE products RENAME COLUMN id_uuid TO id;
ALTER TABLE orders DROP COLUMN id;
ALTER TABLE orders RENAME COLUMN id_uuid TO id;
ALTER TABLE order_items DROP COLUMN id;
ALTER TABLE order_items RENAME COLUMN id_uuid TO id;
ALTER TABLE delivery_assignments DROP COLUMN id;
ALTER TABLE delivery_assignments RENAME COLUMN id_uuid TO id;
ALTER TABLE payments DROP COLUMN id;
ALTER TABLE payments RENAME COLUMN id_uuid TO id;
ALTER TABLE reviews DROP COLUMN id;
ALTER TABLE reviews RENAME COLUMN id_uuid TO id;
ALTER TABLE complaints DROP COLUMN id;
ALTER TABLE complaints RENAME COLUMN id_uuid TO id;
ALTER TABLE carts DROP COLUMN id;
ALTER TABLE carts RENAME COLUMN id_uuid TO id;
ALTER TABLE cart_items DROP COLUMN id;
ALTER TABLE cart_items RENAME COLUMN id_uuid TO id;
ALTER TABLE order_status_history DROP COLUMN id;
ALTER TABLE order_status_history RENAME COLUMN id_uuid TO id;
ALTER TABLE delivery_batches DROP COLUMN id;
ALTER TABLE delivery_batches RENAME COLUMN id_uuid TO id;
ALTER TABLE delivery_batch_orders DROP COLUMN id;
ALTER TABLE delivery_batch_orders RENAME COLUMN id_uuid TO id;
ALTER TABLE delivery_locations DROP COLUMN id;
ALTER TABLE delivery_locations RENAME COLUMN id_uuid TO id;
ALTER TABLE refunds DROP COLUMN id;
ALTER TABLE refunds RENAME COLUMN id_uuid TO id;
ALTER TABLE invoices DROP COLUMN id;
ALTER TABLE invoices RENAME COLUMN id_uuid TO id;
ALTER TABLE coupon_redemptions DROP COLUMN id;
ALTER TABLE coupon_redemptions RENAME COLUMN id_uuid TO id;
ALTER TABLE loyalty_accounts DROP COLUMN id;
ALTER TABLE loyalty_accounts RENAME COLUMN id_uuid TO id;
ALTER TABLE loyalty_transactions DROP COLUMN id;
ALTER TABLE loyalty_transactions RENAME COLUMN id_uuid TO id;

-- ============================================================================
-- Recreate Primary Keys, Foreign Keys, Uniques, And Indexes
-- ============================================================================

ALTER TABLE users ADD CONSTRAINT pk_users PRIMARY KEY (id);
ALTER TABLE addresses ADD CONSTRAINT pk_addresses PRIMARY KEY (id);
ALTER TABLE restaurants ADD CONSTRAINT pk_restaurants PRIMARY KEY (id);
ALTER TABLE products ADD CONSTRAINT pk_products PRIMARY KEY (id);
ALTER TABLE orders ADD CONSTRAINT pk_orders PRIMARY KEY (id);
ALTER TABLE order_items ADD CONSTRAINT pk_order_items PRIMARY KEY (id);
ALTER TABLE delivery_assignments ADD CONSTRAINT pk_delivery_assignments PRIMARY KEY (id);
ALTER TABLE payments ADD CONSTRAINT pk_payments PRIMARY KEY (id);
ALTER TABLE reviews ADD CONSTRAINT pk_reviews PRIMARY KEY (id);
ALTER TABLE complaints ADD CONSTRAINT pk_complaints PRIMARY KEY (id);
ALTER TABLE restaurant_schedules ADD CONSTRAINT pk_restaurant_schedules PRIMARY KEY (id);
ALTER TABLE carts ADD CONSTRAINT pk_carts PRIMARY KEY (id);
ALTER TABLE cart_items ADD CONSTRAINT pk_cart_items PRIMARY KEY (id);
ALTER TABLE order_status_history ADD CONSTRAINT pk_order_status_history PRIMARY KEY (id);
ALTER TABLE delivery_batches ADD CONSTRAINT pk_delivery_batches PRIMARY KEY (id);
ALTER TABLE delivery_batch_orders ADD CONSTRAINT pk_delivery_batch_orders PRIMARY KEY (id);
ALTER TABLE delivery_locations ADD CONSTRAINT pk_delivery_locations PRIMARY KEY (id);
ALTER TABLE refunds ADD CONSTRAINT pk_refunds PRIMARY KEY (id);
ALTER TABLE invoices ADD CONSTRAINT pk_invoices PRIMARY KEY (id);
ALTER TABLE coupon_redemptions ADD CONSTRAINT pk_coupon_redemptions PRIMARY KEY (id);
ALTER TABLE loyalty_accounts ADD CONSTRAINT pk_loyalty_accounts PRIMARY KEY (id);
ALTER TABLE loyalty_transactions ADD CONSTRAINT pk_loyalty_transactions PRIMARY KEY (id);
ALTER TABLE restaurant_commissions ADD CONSTRAINT pk_restaurant_commissions PRIMARY KEY (id);

ALTER TABLE addresses ADD CONSTRAINT fk_addresses_user_id_users FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE restaurants ADD CONSTRAINT fk_restaurants_owner_user_id_users FOREIGN KEY (owner_user_id) REFERENCES users (id);
ALTER TABLE categories ADD CONSTRAINT fk_categories_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id);
ALTER TABLE products ADD CONSTRAINT fk_products_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id);
ALTER TABLE orders ADD CONSTRAINT fk_orders_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id);
ALTER TABLE orders ADD CONSTRAINT fk_orders_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id);
ALTER TABLE orders ADD CONSTRAINT fk_orders_delivery_address_id_addresses FOREIGN KEY (delivery_address_id) REFERENCES addresses (id);
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product_id_products FOREIGN KEY (product_id) REFERENCES products (id);
ALTER TABLE delivery_assignments ADD CONSTRAINT fk_delivery_assignments_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE delivery_assignments ADD CONSTRAINT fk_delivery_assignments_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id);
ALTER TABLE payments ADD CONSTRAINT fk_payments_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_reviewer_user_id_users FOREIGN KEY (reviewer_user_id) REFERENCES users (id);
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id);
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id);
ALTER TABLE complaints ADD CONSTRAINT fk_complaints_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE complaints ADD CONSTRAINT fk_complaints_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id);
ALTER TABLE complaints ADD CONSTRAINT fk_complaints_assigned_admin_user_id_users FOREIGN KEY (assigned_admin_user_id) REFERENCES users (id);
ALTER TABLE restaurant_schedules ADD CONSTRAINT fk_restaurant_schedules_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE;
ALTER TABLE carts ADD CONSTRAINT fk_carts_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id);
ALTER TABLE carts ADD CONSTRAINT fk_carts_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id);
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_cart_id_carts FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE;
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_product_id_products FOREIGN KEY (product_id) REFERENCES products (id);
ALTER TABLE order_status_history ADD CONSTRAINT fk_order_status_history_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;
ALTER TABLE order_status_history ADD CONSTRAINT fk_order_status_history_changed_by_user_id_users FOREIGN KEY (changed_by_user_id) REFERENCES users (id);
ALTER TABLE delivery_batches ADD CONSTRAINT fk_delivery_batches_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id);
ALTER TABLE delivery_batch_orders ADD CONSTRAINT fk_delivery_batch_orders_delivery_batch_id_delivery_batches FOREIGN KEY (delivery_batch_id) REFERENCES delivery_batches (id) ON DELETE CASCADE;
ALTER TABLE delivery_batch_orders ADD CONSTRAINT fk_delivery_batch_orders_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE delivery_locations ADD CONSTRAINT fk_delivery_locations_delivery_user_id_users FOREIGN KEY (delivery_user_id) REFERENCES users (id);
ALTER TABLE delivery_locations ADD CONSTRAINT fk_delivery_locations_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE delivery_locations ADD CONSTRAINT fk_delivery_locations_delivery_batch_id_delivery_batches FOREIGN KEY (delivery_batch_id) REFERENCES delivery_batches (id);
ALTER TABLE refunds ADD CONSTRAINT fk_refunds_payment_id_payments FOREIGN KEY (payment_id) REFERENCES payments (id);
ALTER TABLE refunds ADD CONSTRAINT fk_refunds_complaint_id_complaints FOREIGN KEY (complaint_id) REFERENCES complaints (id);
ALTER TABLE invoices ADD CONSTRAINT fk_invoices_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE invoices ADD CONSTRAINT fk_invoices_payment_id_payments FOREIGN KEY (payment_id) REFERENCES payments (id);
ALTER TABLE coupon_redemptions ADD CONSTRAINT fk_coupon_redemptions_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE coupon_redemptions ADD CONSTRAINT fk_coupon_redemptions_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id);
ALTER TABLE loyalty_accounts ADD CONSTRAINT fk_loyalty_accounts_customer_user_id_users FOREIGN KEY (customer_user_id) REFERENCES users (id);
ALTER TABLE loyalty_transactions ADD CONSTRAINT fk_loyalty_transactions_loyalty_account_id_loyalty_accounts FOREIGN KEY (loyalty_account_id) REFERENCES loyalty_accounts (id);
ALTER TABLE loyalty_transactions ADD CONSTRAINT fk_loyalty_transactions_order_id_orders FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE restaurant_commissions ADD CONSTRAINT fk_restaurant_commissions_restaurant_id_restaurants FOREIGN KEY (restaurant_id) REFERENCES restaurants (id);

ALTER TABLE categories ADD CONSTRAINT uk_categories_restaurant_id_name UNIQUE (restaurant_id, name);
ALTER TABLE categories ADD CONSTRAINT uk_categories_id_restaurant_id UNIQUE (id, restaurant_id);
ALTER TABLE products ADD CONSTRAINT fk_products_category_id_restaurant_id_categories FOREIGN KEY (category_id, restaurant_id) REFERENCES categories (id, restaurant_id);
ALTER TABLE restaurants ADD CONSTRAINT uk_restaurants_owner_user_id UNIQUE (owner_user_id);
ALTER TABLE products ADD CONSTRAINT uk_products_restaurant_id_name UNIQUE (restaurant_id, name);
ALTER TABLE order_items ADD CONSTRAINT uk_order_items_order_id_product_id UNIQUE (order_id, product_id);
ALTER TABLE delivery_assignments ADD CONSTRAINT uk_delivery_assignments_order_id UNIQUE (order_id);
ALTER TABLE reviews ADD CONSTRAINT uk_reviews_order_id_reviewer_user_id UNIQUE (order_id, reviewer_user_id);
ALTER TABLE restaurant_schedules ADD CONSTRAINT uk_restaurant_schedules_restaurant_id_day_of_week UNIQUE (restaurant_id, day_of_week);
ALTER TABLE cart_items ADD CONSTRAINT uk_cart_items_cart_id_product_id UNIQUE (cart_id, product_id);
ALTER TABLE delivery_batch_orders ADD CONSTRAINT uk_delivery_batch_orders_order_id UNIQUE (order_id);
ALTER TABLE delivery_batch_orders ADD CONSTRAINT uk_delivery_batch_orders_batch_id_sequence UNIQUE (delivery_batch_id, sequence_number);
ALTER TABLE invoices ADD CONSTRAINT uk_invoices_order_id UNIQUE (order_id);
ALTER TABLE coupon_redemptions ADD CONSTRAINT uk_coupon_redemptions_coupon_id_order_id UNIQUE (coupon_id, order_id);
ALTER TABLE loyalty_accounts ADD CONSTRAINT uk_loyalty_accounts_customer_user_id UNIQUE (customer_user_id);

CREATE INDEX idx_addresses_user_id ON addresses (user_id);
CREATE INDEX idx_addresses_user_id_is_default ON addresses (user_id, is_default);
CREATE INDEX idx_restaurants_owner_user_id ON restaurants (owner_user_id);
CREATE INDEX idx_categories_restaurant_id ON categories (restaurant_id);
CREATE INDEX idx_products_restaurant_id ON products (restaurant_id);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_restaurant_id_is_available ON products (restaurant_id, is_available);
CREATE INDEX idx_orders_customer_user_id ON orders (customer_user_id);
CREATE INDEX idx_orders_restaurant_id ON orders (restaurant_id);
CREATE INDEX idx_orders_delivery_address_id ON orders (delivery_address_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
CREATE INDEX idx_delivery_assignments_delivery_user_id ON delivery_assignments (delivery_user_id);
CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_reviews_restaurant_id ON reviews (restaurant_id);
CREATE INDEX idx_reviews_delivery_user_id ON reviews (delivery_user_id);
CREATE INDEX idx_complaints_order_id ON complaints (order_id);
CREATE INDEX idx_complaints_customer_user_id ON complaints (customer_user_id);
CREATE INDEX idx_restaurant_schedules_restaurant_id_day ON restaurant_schedules (restaurant_id, day_of_week);
CREATE INDEX idx_carts_customer_user_id_status ON carts (customer_user_id, status);
CREATE UNIQUE INDEX uk_carts_active_customer_restaurant ON carts (customer_user_id, restaurant_id) WHERE status = 'ACTIVE';
CREATE INDEX idx_carts_restaurant_id ON carts (restaurant_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items (product_id);
CREATE INDEX idx_order_status_history_order_id_created_at ON order_status_history (order_id, created_at);
CREATE INDEX idx_delivery_batches_delivery_user_id_status ON delivery_batches (delivery_user_id, status);
CREATE INDEX idx_delivery_batch_orders_batch_id_sequence ON delivery_batch_orders (delivery_batch_id, sequence_number);
CREATE INDEX idx_delivery_locations_delivery_user_id_recorded_at ON delivery_locations (delivery_user_id, recorded_at);
CREATE INDEX idx_delivery_locations_order_id_recorded_at ON delivery_locations (order_id, recorded_at);
CREATE INDEX idx_delivery_locations_batch_id_recorded_at ON delivery_locations (delivery_batch_id, recorded_at);
CREATE INDEX idx_refunds_payment_id ON refunds (payment_id);
CREATE INDEX idx_refunds_complaint_id ON refunds (complaint_id);
CREATE INDEX idx_coupon_redemptions_customer_user_id ON coupon_redemptions (customer_user_id);
CREATE INDEX idx_coupon_redemptions_order_id ON coupon_redemptions (order_id);
CREATE INDEX idx_loyalty_transactions_account_id_created_at ON loyalty_transactions (loyalty_account_id, created_at);
CREATE INDEX idx_loyalty_transactions_order_id ON loyalty_transactions (order_id);
CREATE INDEX idx_restaurant_commissions_restaurant_id_dates ON restaurant_commissions (restaurant_id, starts_at, ends_at);
