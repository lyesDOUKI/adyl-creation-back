ALTER TABLE CUSTOMERS
DROP COLUMN customer_name,
    DROP COLUMN customer_address,
    DROP COLUMN customer_city;

ALTER TABLE CUSTOMERS
    RENAME COLUMN customer_email TO email;

ALTER TABLE CUSTOMERS
    RENAME COLUMN customer_phone TO phone;

ALTER TABLE CUSTOMERS
    ADD COLUMN identity_subject UUID,
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE CUSTOMERS
    ALTER COLUMN identity_subject SET NOT NULL;

ALTER TABLE CUSTOMERS
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE CUSTOMERS
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE CUSTOMERS
    ADD CONSTRAINT uk_customers_identity_subject
        UNIQUE (identity_subject);

DROP INDEX idx_customer_email;

CREATE INDEX idx_customers_email
    ON CUSTOMERS (email);