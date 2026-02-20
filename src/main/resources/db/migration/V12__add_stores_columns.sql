-- Add missing columns to stores to match Store entity
ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS description TEXT;

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS country VARCHAR(255);

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS currency VARCHAR(10);

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS subscription_plan VARCHAR(50);

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500);

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS banner_url VARCHAR(500);

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS website_url VARCHAR(500);

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS business_address TEXT;

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS business_registration_number VARCHAR(50);

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS business_phone_number VARCHAR(20);

ALTER TABLE stores
  ADD COLUMN IF NOT EXISTS business_email VARCHAR(255);

