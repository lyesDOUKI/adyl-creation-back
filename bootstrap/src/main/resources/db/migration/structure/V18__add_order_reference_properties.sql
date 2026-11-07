ALTER TABLE orders
    ALTER COLUMN order_reference SET NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT uq_orders_order_reference UNIQUE (order_reference);

CREATE INDEX idx_orders_order_reference ON orders (order_reference);