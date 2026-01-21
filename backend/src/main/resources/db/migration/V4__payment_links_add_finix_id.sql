ALTER TABLE payment_links
ADD COLUMN finix_payment_link_id TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_links_finix_payment_link_id
ON payment_links(finix_payment_link_id);