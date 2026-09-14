CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE appointment
    ADD COLUMN slot_range tstzrange
        GENERATED ALWAYS AS (tstzrange(start_at, end_at, '[)')) STORED;

ALTER TABLE appointment
    ADD CONSTRAINT no_overlapping_appointments
    EXCLUDE USING gist (slot_range WITH &&)
    WHERE (status <> 'CANCELLED');