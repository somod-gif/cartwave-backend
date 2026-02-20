-- Add completed_at to orders to match entity
ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS completed_at BIGINT;

-- No NOT NULL constraint to avoid issues with existing data

