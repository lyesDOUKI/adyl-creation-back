-- V__alter_orders_customer_and_delivery.sql

-- ============================================================
-- 1. Customer :
--    remplacer customer_id par customer_identity_subject
-- ============================================================

ALTER TABLE orders
    ADD COLUMN customer_identity_subject UUID;

UPDATE orders o
SET customer_identity_subject = (
    SELECT c.identity_subject
    FROM CUSTOMERS c
    WHERE c.id = o.customer_id
)
WHERE EXISTS (
    SELECT 1
    FROM CUSTOMERS c
    WHERE c.id = o.customer_id
);

ALTER TABLE orders
DROP CONSTRAINT IF EXISTS orders_customer_id_fkey;

ALTER TABLE orders
DROP COLUMN customer_id;

ALTER TABLE orders
    ALTER COLUMN customer_identity_subject SET NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer_identity_subject
        FOREIGN KEY (customer_identity_subject)
            REFERENCES CUSTOMERS (identity_subject);


-- ============================================================
-- 2. Delivery address :
--    Order référence directement son adresse
-- ============================================================

ALTER TABLE orders
    ADD COLUMN delivery_address_id UUID;

ALTER TABLE orders
    ALTER COLUMN delivery_address_id SET NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_delivery_address
        FOREIGN KEY (delivery_address_id)
            REFERENCES ORDER_DELIVERY_ADDRESSES (id);