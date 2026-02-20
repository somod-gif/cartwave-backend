-- Add missing subscription columns to match Subscription entity
ALTER TABLE subscriptions
  ADD COLUMN IF NOT EXISTS plan_name VARCHAR(100);

ALTER TABLE subscriptions
  ADD COLUMN IF NOT EXISTS start_date BIGINT;

ALTER TABLE subscriptions
  ADD COLUMN IF NOT EXISTS end_date BIGINT;

ALTER TABLE subscriptions
  ADD COLUMN IF NOT EXISTS renewal_date BIGINT;

ALTER TABLE subscriptions
  ADD COLUMN IF NOT EXISTS billing_cycle VARCHAR(50);

ALTER TABLE subscriptions
  ADD COLUMN IF NOT EXISTS features TEXT;

