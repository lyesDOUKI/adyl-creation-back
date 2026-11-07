CREATE TABLE PRODUCT_PHOTOS (
                                id           UUID PRIMARY KEY,
                                product_id   UUID NOT NULL,
                                storage_key  VARCHAR(300) NOT NULL,
                                position     INT NOT NULL,
                                created_at   TIMESTAMP NOT NULL DEFAULT now(),

                                CONSTRAINT fk_product_photos_product
                                    FOREIGN KEY (product_id)
                                        REFERENCES PRODUCTS(id),

                                CONSTRAINT uq_product_photos_storage_key
                                    UNIQUE (storage_key)
);

CREATE INDEX idx_product_photos_product_id ON PRODUCT_PHOTOS(product_id);