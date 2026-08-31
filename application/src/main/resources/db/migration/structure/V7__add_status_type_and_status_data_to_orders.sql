ALTER TABLE orders
    ADD COLUMN status_type VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN status_data JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE INDEX idx_orders_status_type ON orders(status_type);
