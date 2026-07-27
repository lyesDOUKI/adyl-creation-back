DROP INDEX idx_order_customer_email_unique;

ALTER TABLE ORDERS
DROP COLUMN customer_name,
    DROP COLUMN customer_email,
    DROP COLUMN customer_phone,
    DROP COLUMN customer_address,
    DROP COLUMN customer_city;

ALTER TABLE ORDERS
    ADD COLUMN customer_id UUID;

ALTER TABLE ORDERS
    ADD CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id) REFERENCES CUSTOMERS (id);