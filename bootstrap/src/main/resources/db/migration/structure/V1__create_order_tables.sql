CREATE TABLE ORDERS (
                        id UUID PRIMARY KEY,
                        customer_name VARCHAR(250) NOT NULL,
                        customer_email VARCHAR(255) NOT NULL UNIQUE,
                        customer_phone VARCHAR(50) NOT NULL UNIQUE,
                        customer_address VARCHAR(255),
                        customer_city VARCHAR(50),
                        customer_message VARCHAR(255),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_order_customer_email_unique ON ORDERS (customer_email);

CREATE TABLE ORDER_DETAILS (
                               id UUID PRIMARY KEY,
                               order_id UUID NOT NULL,
                               product_id UUID NOT NULL,
                               quantity NUMERIC(15,2) NOT NULL,
                               unit_price NUMERIC(15,2) NOT NULL,
                               total_amount NUMERIC(15,2) NOT NULL,
                               chosen_color VARCHAR(50),
                               CONSTRAINT fk_order
                                   FOREIGN KEY (order_id)
                                       REFERENCES ORDERS (id)
                                       ON DELETE CASCADE
);