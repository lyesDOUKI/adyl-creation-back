CREATE TABLE CUSTOMERS (
                           id UUID PRIMARY KEY,
                           customer_name VARCHAR(250) NOT NULL,
                           customer_email VARCHAR(255) NOT NULL,
                           customer_phone VARCHAR(50) NOT NULL,
                           customer_address VARCHAR(255),
                           customer_city VARCHAR(50)
);

CREATE INDEX idx_customer_email ON CUSTOMERS (customer_email);