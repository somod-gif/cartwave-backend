-- Add banner_url to stores to match Store entity
ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS banner_url VARCHAR(500);

