CREATE TABLE PRODUCTS (
                          id UUID PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          unit_price NUMERIC(15,2) NOT NULL,
                          status VARCHAR(50) NOT NULL
);

CREATE TABLE PRODUCT_COLORS (
                                product_id UUID NOT NULL,
                                color VARCHAR(100) NOT NULL,
                                PRIMARY KEY(product_id, color),
                                CONSTRAINT fk_product_colors_product
                                    FOREIGN KEY(product_id)
                                        REFERENCES PRODUCTS(id)
);