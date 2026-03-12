-- ============================================================
-- V2__feature_upgrades.sql
-- CartWave Platform – Feature Upgrades Migration
-- All statements use IF NOT EXISTS / IF EXISTS for idempotency
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- 1. STORES – Store Builder V2 fields
-- ────────────────────────────────────────────────────────────
ALTER TABLE stores
    ADD COLUMN IF NOT EXISTS template         VARCHAR(20),
    ADD COLUMN IF NOT EXISTS brand_color      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS custom_domain_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS subdomain        VARCHAR(255),
    ADD COLUMN IF NOT EXISTS store_status     VARCHAR(20)  DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS meta_title       VARCHAR(255),
    ADD COLUMN IF NOT EXISTS meta_description TEXT,
    ADD COLUMN IF NOT EXISTS keywords         TEXT;

-- Unique constraint on subdomain (safe – won't fail if already present)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_stores_subdomain'
    ) THEN
        ALTER TABLE stores ADD CONSTRAINT uq_stores_subdomain UNIQUE (subdomain);
    END IF;
END $$;

-- ────────────────────────────────────────────────────────────
-- 2. PRODUCTS – Product Management V2 fields
-- ────────────────────────────────────────────────────────────
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS tags            TEXT,
    ADD COLUMN IF NOT EXISTS is_published    BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS seo_title       VARCHAR(255),
    ADD COLUMN IF NOT EXISTS seo_description TEXT;

-- ────────────────────────────────────────────────────────────
-- 3. ESCROW_TRANSACTIONS – Escrow V2 fields
-- ────────────────────────────────────────────────────────────
ALTER TABLE escrow_transactions
    ADD COLUMN IF NOT EXISTS platform_fee_percent NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS seller_amount        NUMERIC(19,2),
    ADD COLUMN IF NOT EXISTS released_at          BIGINT;

-- ────────────────────────────────────────────────────────────
-- 4. ESCROW_DISPUTES – Dispute V2 fields
-- ────────────────────────────────────────────────────────────
ALTER TABLE escrow_disputes
    ADD COLUMN IF NOT EXISTS evidence               TEXT,
    ADD COLUMN IF NOT EXISTS admin_resolution_notes TEXT;

-- ────────────────────────────────────────────────────────────
-- 5. EMAIL_QUEUE – Track dispatch time
-- ────────────────────────────────────────────────────────────
ALTER TABLE email_queue
    ADD COLUMN IF NOT EXISTS sent_at TIMESTAMPTZ;

-- ────────────────────────────────────────────────────────────
-- 6. COUPONS – Marketing coupon table
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS coupons (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted             BOOLEAN      NOT NULL DEFAULT FALSE,

    store_id            UUID         NOT NULL,
    code                VARCHAR(64)  NOT NULL,
    discount_type       VARCHAR(20)  NOT NULL,      -- PERCENT | FIXED
    discount_value      NUMERIC(19,2) NOT NULL,
    min_order_value     NUMERIC(19,2),
    max_uses            INTEGER,
    used_count          INTEGER      NOT NULL DEFAULT 0,
    expires_at          TIMESTAMPTZ,
    active              BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Indexes for coupons
CREATE INDEX IF NOT EXISTS idx_coupon_store_id ON coupons (store_id);
CREATE INDEX IF NOT EXISTS idx_coupon_code     ON coupons (code);
CREATE INDEX IF NOT EXISTS idx_coupon_deleted  ON coupons (deleted);
