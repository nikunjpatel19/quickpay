-- 1) Fix payload type: jsonb -> text
ALTER TABLE webhook_events
ALTER COLUMN payload TYPE TEXT
USING payload::text;

-- 2) Add columns for idempotency + querying
ALTER TABLE webhook_events
ADD COLUMN finix_event_id TEXT,
ADD COLUMN entity TEXT,
ADD COLUMN resource_id TEXT;

-- 3) Prevent double processing
CREATE UNIQUE INDEX IF NOT EXISTS uq_webhook_finix_event_id
ON webhook_events (finix_event_id);