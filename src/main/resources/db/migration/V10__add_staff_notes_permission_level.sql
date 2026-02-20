-- Add missing columns to staff to match Staff entity
ALTER TABLE staff
  ADD COLUMN IF NOT EXISTS permission_level VARCHAR(50);

ALTER TABLE staff
  ADD COLUMN IF NOT EXISTS notes TEXT;

