-- =============================================================================
-- V4 — Add columns that exist in entity classes but are missing from databases
-- that were migrated with the original V1 + V2 (before the consolidation).
--
-- The consolidated V1 merged columns from the original V3 (password-reset &
-- email-verification on users) and various V2 feature columns.  Databases at
-- schema version 2 have the V2 columns but NOT the V3 columns.
--
-- Every statement uses IF NOT EXISTS so this is safe on any database state.
-- =============================================================================

-- ═══════════════════════════════════════════════════════════════════════════
-- USERS — columns from the original V3 (password-reset + email verification)
-- ═══════════════════════════════════════════════════════════════════════════
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_token      VARCHAR(64);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_expires_at BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_token  VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_users_password_reset_token
    ON users(password_reset_token) WHERE password_reset_token IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_email_verification_token
    ON users(email_verification_token) WHERE email_verification_token IS NOT NULL;

-- ═══════════════════════════════════════════════════════════════════════════
-- STORES — V2 store-builder columns (should exist but guarded for safety)
-- ═══════════════════════════════════════════════════════════════════════════
ALTER TABLE stores ADD COLUMN IF NOT EXISTS template            VARCHAR(20);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS brand_color         VARCHAR(20);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS custom_domain_name  VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS subdomain           VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS store_status        VARCHAR(20) DEFAULT 'ACTIVE';
ALTER TABLE stores ADD COLUMN IF NOT EXISTS meta_title          VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS meta_description    TEXT;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS keywords            TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_stores_subdomain_unique
    ON stores(subdomain) WHERE subdomain IS NOT NULL;

-- ═══════════════════════════════════════════════════════════════════════════
-- PRODUCTS — V2 columns (should exist but guarded for safety)
-- ═══════════════════════════════════════════════════════════════════════════
ALTER TABLE products ADD COLUMN IF NOT EXISTS tags            TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_published    BOOLEAN DEFAULT FALSE;
ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_title       VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_description TEXT;

-- ═══════════════════════════════════════════════════════════════════════════
-- ESCROW_TRANSACTIONS — V2 columns (should exist but guarded for safety)
-- ═══════════════════════════════════════════════════════════════════════════
ALTER TABLE escrow_transactions ADD COLUMN IF NOT EXISTS platform_fee_percent NUMERIC(5,2);
ALTER TABLE escrow_transactions ADD COLUMN IF NOT EXISTS seller_amount        NUMERIC(19,2);
ALTER TABLE escrow_transactions ADD COLUMN IF NOT EXISTS released_at          BIGINT;

-- ═══════════════════════════════════════════════════════════════════════════
-- ESCROW_DISPUTES — V2 columns (should exist but guarded for safety)
-- ═══════════════════════════════════════════════════════════════════════════
ALTER TABLE escrow_disputes ADD COLUMN IF NOT EXISTS evidence               TEXT;
ALTER TABLE escrow_disputes ADD COLUMN IF NOT EXISTS admin_resolution_notes TEXT;

-- ═══════════════════════════════════════════════════════════════════════════
-- EMAIL_QUEUE — V2 column (should exist but guarded for safety)
-- ═══════════════════════════════════════════════════════════════════════════
ALTER TABLE email_queue ADD COLUMN IF NOT EXISTS sent_at TIMESTAMPTZ;
