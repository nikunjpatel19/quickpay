CREATE TABLE IF NOT EXISTS payment_links (
  id              VARCHAR(64) PRIMARY KEY,
  amount_cents    BIGINT NOT NULL CHECK (amount_cents >= 0),
  currency        VARCHAR(3) NOT NULL,
  description     TEXT,
  checkout_url    TEXT,
  status          VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING | PAID | CANCELED | EXPIRED
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS orders (
  id              VARCHAR(64) PRIMARY KEY,
  link_id         VARCHAR(64) NOT NULL REFERENCES payment_links(id) ON DELETE CASCADE,
  status          VARCHAR(32) NOT NULL DEFAULT 'CREATED', -- CREATED | AUTHORIZED | CAPTURED | FAILED
  amount_cents    BIGINT NOT NULL CHECK (amount_cents >= 0),
  currency        VARCHAR(3) NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS webhook_events (
  id              BIGSERIAL PRIMARY KEY,
  event_type      VARCHAR(128) NOT NULL,
  payload         JSONB NOT NULL,
  received_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  processed       BOOLEAN NOT NULL DEFAULT FALSE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_payment_links_status ON payment_links(status);
CREATE INDEX IF NOT EXISTS idx_orders_link_id ON orders(link_id);
CREATE INDEX IF NOT EXISTS idx_webhook_events_processed ON webhook_events(processed);

-- Updated at triggers
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_payment_links_updated_at ON payment_links;
CREATE TRIGGER trg_payment_links_updated_at
BEFORE UPDATE ON payment_links
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_orders_updated_at ON orders;
CREATE TRIGGER trg_orders_updated_at
BEFORE UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
