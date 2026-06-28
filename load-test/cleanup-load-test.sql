BEGIN;

CREATE TEMP TABLE load_test_users ON COMMIT DROP AS
SELECT id
FROM users
WHERE email LIKE 'loadtest-%@example.com';

CREATE TEMP TABLE load_test_orders ON COMMIT DROP AS
SELECT id
FROM orders
WHERE customer_user_id IN (SELECT id FROM load_test_users);

CREATE TEMP TABLE load_test_payments ON COMMIT DROP AS
SELECT id
FROM payments
WHERE order_id IN (SELECT id FROM load_test_orders);

CREATE TEMP TABLE load_test_complaints ON COMMIT DROP AS
SELECT id
FROM complaints
WHERE order_id IN (SELECT id FROM load_test_orders);

DELETE FROM refunds
WHERE payment_id IN (SELECT id FROM load_test_payments)
   OR complaint_id IN (SELECT id FROM load_test_complaints);

DELETE FROM invoices
WHERE order_id IN (SELECT id FROM load_test_orders)
   OR payment_id IN (SELECT id FROM load_test_payments);

DELETE FROM delivery_locations
WHERE order_id IN (SELECT id FROM load_test_orders);

DELETE FROM delivery_batch_orders
WHERE order_id IN (SELECT id FROM load_test_orders);

DELETE FROM delivery_assignment_rejections
WHERE order_id IN (SELECT id FROM load_test_orders);

DELETE FROM delivery_assignments
WHERE order_id IN (SELECT id FROM load_test_orders);

DELETE FROM reviews
WHERE order_id IN (SELECT id FROM load_test_orders);

DELETE FROM coupon_redemptions
WHERE order_id IN (SELECT id FROM load_test_orders);

DELETE FROM loyalty_transactions
WHERE order_id IN (SELECT id FROM load_test_orders)
   OR loyalty_account_id IN (
       SELECT id
       FROM loyalty_accounts
       WHERE customer_user_id IN (SELECT id FROM load_test_users)
   );

DELETE FROM complaints
WHERE id IN (SELECT id FROM load_test_complaints);

DELETE FROM payments
WHERE id IN (SELECT id FROM load_test_payments);

DELETE FROM orders
WHERE id IN (SELECT id FROM load_test_orders);

DELETE FROM carts
WHERE customer_user_id IN (SELECT id FROM load_test_users);

DELETE FROM refresh_tokens
WHERE user_id IN (SELECT id FROM load_test_users);

DELETE FROM addresses
WHERE user_id IN (SELECT id FROM load_test_users);

DELETE FROM loyalty_accounts
WHERE customer_user_id IN (SELECT id FROM load_test_users);

DELETE FROM users
WHERE id IN (SELECT id FROM load_test_users);

COMMIT;
