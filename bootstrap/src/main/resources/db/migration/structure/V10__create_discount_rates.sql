CREATE TABLE discount_rates (
                                id UUID PRIMARY KEY,
                                discount_type VARCHAR(50) NOT NULL UNIQUE,
                                rate NUMERIC(5, 2) NOT NULL,
                                created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                updated_at TIMESTAMP WITH TIME ZONE
);