-- Add missing columns to orders to match Order entity
ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS delivery_address TEXT;

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS customer_email VARCHAR(255);

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS customer_phone_number VARCHAR(20);

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS notes TEXT;

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS shipping_cost NUMERIC(19,2);

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(19,2);

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(19,2);

-- completed_at already added in V6, this ensures idempotency
ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS completed_at BIGINT;

