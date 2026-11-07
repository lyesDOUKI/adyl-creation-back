ALTER TABLE order_details
    ADD COLUMN discount_rate NUMERIC(5, 4) NOT NULL DEFAULT 0;