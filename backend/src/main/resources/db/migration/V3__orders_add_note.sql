ALTER TABLE orders
ADD COLUMN note TEXT;

CREATE INDEX IF NOT EXISTS idx_orders_note ON orders(note);