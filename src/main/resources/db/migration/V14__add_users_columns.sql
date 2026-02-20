-- Add missing user columns to match User entity
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS first_name VARCHAR(255);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS last_name VARCHAR(255);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS phone_number VARCHAR(255);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS profile_picture_url TEXT;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS bio TEXT;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS last_login_at BIGINT;

-- email_verified is non-nullable on the entity; add with default, populate and set NOT NULL
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;
UPDATE users SET email_verified = FALSE WHERE email_verified IS NULL;
ALTER TABLE users ALTER COLUMN email_verified SET NOT NULL;

