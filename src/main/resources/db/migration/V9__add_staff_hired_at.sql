-- Add missing hired_at to staff to match Staff entity
ALTER TABLE staff
  ADD COLUMN IF NOT EXISTS hired_at BIGINT;

