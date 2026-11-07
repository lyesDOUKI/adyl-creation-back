ALTER TABLE discount_claims
    ADD COLUMN customer_identity_subject UUID;

UPDATE discount_claims
SET customer_identity_subject = (
    SELECT identity_subject
    FROM customers
    WHERE customers.email = discount_claims.email
);

ALTER TABLE discount_claims
    ALTER COLUMN customer_identity_subject SET NOT NULL;

ALTER TABLE discount_claims
DROP CONSTRAINT pk_discount_claims;

DROP INDEX idx_discount_claims_email;

ALTER TABLE discount_claims
DROP COLUMN email;

ALTER TABLE discount_claims
    ADD CONSTRAINT pk_discount_claims
        PRIMARY KEY (type, customer_identity_subject);

ALTER TABLE discount_claims
    ADD CONSTRAINT fk_discount_claims_customer_identity_subject
        FOREIGN KEY (customer_identity_subject)
            REFERENCES customers (identity_subject);

CREATE INDEX idx_discount_claims_customer_identity_subject
    ON discount_claims (customer_identity_subject);