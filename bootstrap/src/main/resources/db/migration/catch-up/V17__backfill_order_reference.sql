WITH ranked AS (
    SELECT
        id,
        created_at,
        ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM orders
    WHERE order_reference IS NULL
)
UPDATE orders o
SET order_reference = 'ORD-'
    || TO_CHAR(COALESCE(r.created_at, CURRENT_TIMESTAMP), 'YYYYMMDD')
    || '-'
    || LPAD(TO_HEX(r.rn), 6, '0')
    FROM ranked r
WHERE o.id = r.id;