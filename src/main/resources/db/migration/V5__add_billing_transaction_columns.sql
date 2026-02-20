-- Add missing columns to billing_transactions to match entity
ALTER TABLE billing_transactions
  ADD COLUMN IF NOT EXISTS currency VARCHAR(10);

ALTER TABLE billing_transactions
  ADD COLUMN IF NOT EXISTS payment_method VARCHAR(100);

ALTER TABLE billing_transactions
  ADD COLUMN IF NOT EXISTS payment_provider VARCHAR(50);

ALTER TABLE billing_transactions
  ADD COLUMN IF NOT EXISTS transaction_details TEXT;

ALTER TABLE billing_transactions
  ADD COLUMN IF NOT EXISTS failure_reason TEXT;

ALTER TABLE billing_transactions
  ADD COLUMN IF NOT EXISTS processed_at BIGINT;

-- Optionally set defaults for new columns (do not set NOT NULL to avoid migration failures)
ALTER TABLE billing_transactions ALTER COLUMN currency SET DEFAULT 'USD';

