-- V3: Password reset and email verification token columns

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_reset_token       VARCHAR(64),
    ADD COLUMN IF NOT EXISTS password_reset_expires_at  BIGINT,
    ADD COLUMN IF NOT EXISTS email_verification_token   VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_users_password_reset_token
    ON users (password_reset_token)
    WHERE password_reset_token IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_email_verification_token
    ON users (email_verification_token)
    WHERE email_verification_token IS NOT NULL;
