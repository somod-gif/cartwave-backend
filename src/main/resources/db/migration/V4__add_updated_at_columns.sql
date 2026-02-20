-- Add updated_at columns to tables inheriting BaseEntity
-- This migration adds updated_at and populates it with created_at for existing rows,
-- then makes the column NOT NULL to satisfy Hibernate schema validation.

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;
UPDATE users SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE users ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;
UPDATE stores SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE stores ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE staff
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;
UPDATE staff SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE staff ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE products
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;
UPDATE products SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE products ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;
UPDATE orders SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE orders ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE subscriptions
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;
UPDATE subscriptions SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE subscriptions ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE billing_transactions
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;
UPDATE billing_transactions SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE billing_transactions ALTER COLUMN updated_at SET NOT NULL;

-- Ensure updated_at defaults to now() for future inserts (optional)
ALTER TABLE users ALTER COLUMN updated_at SET DEFAULT NOW();
ALTER TABLE stores ALTER COLUMN updated_at SET DEFAULT NOW();
ALTER TABLE staff ALTER COLUMN updated_at SET DEFAULT NOW();
ALTER TABLE products ALTER COLUMN updated_at SET DEFAULT NOW();
ALTER TABLE orders ALTER COLUMN updated_at SET DEFAULT NOW();
ALTER TABLE subscriptions ALTER COLUMN updated_at SET DEFAULT NOW();
ALTER TABLE billing_transactions ALTER COLUMN updated_at SET DEFAULT NOW();

