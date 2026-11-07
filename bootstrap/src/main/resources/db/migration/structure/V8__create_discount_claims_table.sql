CREATE TABLE discount_claims (
                                 type VARCHAR(50) NOT NULL,
                                 email VARCHAR(255) NOT NULL,
                                 claimed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT pk_discount_claims PRIMARY KEY (type, email)
);

CREATE INDEX idx_discount_claims_email ON discount_claims (email);