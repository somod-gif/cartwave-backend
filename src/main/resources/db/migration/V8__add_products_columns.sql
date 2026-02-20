-- Add missing product columns to match Product entity
ALTER TABLE products
  ADD COLUMN IF NOT EXISTS stock BIGINT DEFAULT 0;

ALTER TABLE products
  ADD COLUMN IF NOT EXISTS low_stock_threshold BIGINT;

ALTER TABLE products
  ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

ALTER TABLE products
  ADD COLUMN IF NOT EXISTS images TEXT;

ALTER TABLE products
  ADD COLUMN IF NOT EXISTS category VARCHAR(255);

ALTER TABLE products
  ADD COLUMN IF NOT EXISTS attributes TEXT;

