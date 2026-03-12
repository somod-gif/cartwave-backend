-- =============================================================================
-- CartWave V1 — Consolidated authoritative schema
-- This file is the single source of truth for the CartWave database schema.
-- It merges all migrations (V1, V2, V3, enterprise upgrades) into one definitive file.
-- Generated: 2026-03-11
-- IMPORTANT: Requires a clean database. Run on empty schema only.
-- =============================================================================

-- ===========================================================================
-- USERS
-- Merged columns: V1 base + V3 password-reset + email-verification tokens
-- ===========================================================================
CREATE TABLE users (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email                       VARCHAR(255) NOT NULL UNIQUE,
    password_hash               VARCHAR(255) NOT NULL,
    first_name                  VARCHAR(255),
    last_name                   VARCHAR(255),
    phone_number                VARCHAR(255),
    role                        VARCHAR(50)  NOT NULL,
    status                      VARCHAR(50)  NOT NULL,
    email_verified              BOOLEAN      NOT NULL DEFAULT FALSE,
    profile_picture_url         TEXT,
    bio                         TEXT,
    last_login_at               BIGINT,
    password_reset_token        VARCHAR(64),
    password_reset_expires_at   BIGINT,
    email_verification_token    VARCHAR(64),
    created_at                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted                     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_users_email                    ON users(email);
CREATE INDEX idx_users_status                   ON users(status);
CREATE INDEX idx_users_deleted                  ON users(deleted);
CREATE INDEX idx_users_password_reset_token     ON users(password_reset_token)     WHERE password_reset_token IS NOT NULL;
CREATE INDEX idx_users_email_verification_token ON users(email_verification_token) WHERE email_verification_token IS NOT NULL;

-- ===========================================================================
-- STORES
-- Merged columns: V1 base + V2 store-builder fields (template, brand_color,
-- custom_domain_name, subdomain, store_status, meta_title, meta_description,
-- keywords) + UNIQUE constraint on subdomain
-- ===========================================================================
CREATE TABLE stores (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                            VARCHAR(255) NOT NULL,
    slug                            VARCHAR(100) NOT NULL UNIQUE,
    description                     TEXT,
    country                         VARCHAR(255),
    currency                        VARCHAR(10),
    owner_user_id                   UUID NOT NULL REFERENCES users(id),
    subscription_plan               VARCHAR(50),
    active                          BOOLEAN NOT NULL DEFAULT TRUE,
    logo_url                        VARCHAR(500),
    banner_url                      VARCHAR(500),
    website_url                     VARCHAR(500),
    business_address                TEXT,
    business_registration_number    VARCHAR(50),
    business_phone_number           VARCHAR(20),
    business_email                  VARCHAR(255),
    template                        VARCHAR(20),
    brand_color                     VARCHAR(20),
    custom_domain_name              VARCHAR(255),
    subdomain                       VARCHAR(255) UNIQUE,
    store_status                    VARCHAR(20)  DEFAULT 'ACTIVE',
    meta_title                      VARCHAR(255),
    meta_description                TEXT,
    keywords                        TEXT,
    created_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted                         BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_stores_slug      ON stores(slug);
CREATE INDEX idx_stores_owner_id  ON stores(owner_user_id);
CREATE INDEX idx_stores_is_active ON stores(active);
CREATE INDEX idx_stores_deleted   ON stores(deleted);

-- ===========================================================================
-- PRODUCTS
-- Merged columns: V1 base + V2 fields (tags, is_published, seo_title, seo_description)
-- ===========================================================================
CREATE TABLE products (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id            UUID NOT NULL REFERENCES stores(id),
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    price               NUMERIC(19,2) NOT NULL,
    cost_price          NUMERIC(19,2),
    stock               BIGINT NOT NULL DEFAULT 0,
    low_stock_threshold BIGINT,
    sku                 VARCHAR(100),
    status              VARCHAR(50) NOT NULL,
    image_url           VARCHAR(500),
    images              TEXT,
    category            VARCHAR(255),
    attributes          TEXT,
    tags                TEXT,
    is_published        BOOLEAN DEFAULT FALSE,
    seo_title           VARCHAR(255),
    seo_description     TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_products_store_id ON products(store_id);
CREATE INDEX idx_products_status   ON products(status);
CREATE INDEX idx_products_deleted  ON products(deleted);

-- ===========================================================================
-- CUSTOMERS
-- ===========================================================================
CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    store_id        UUID NOT NULL REFERENCES stores(id),
    phone           VARCHAR(64),
    addresses_json  TEXT,
    wishlist_json   TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_customers_user_store ON customers(user_id, store_id);
CREATE INDEX idx_customers_deleted    ON customers(deleted);

-- ===========================================================================
-- STAFF
-- ===========================================================================
CREATE TABLE staff (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id),
    store_id         UUID NOT NULL REFERENCES stores(id),
    permission_level VARCHAR(50),
    role             VARCHAR(50) NOT NULL,
    status           VARCHAR(50) NOT NULL,
    notes            TEXT,
    hired_at         BIGINT,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted          BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_staff_store_id ON staff(store_id);
CREATE INDEX idx_staff_user_id  ON staff(user_id);
CREATE INDEX idx_staff_deleted  ON staff(deleted);

-- ===========================================================================
-- ORDERS
-- ===========================================================================
CREATE TABLE orders (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id              UUID NOT NULL REFERENCES stores(id),
    customer_id           UUID NOT NULL REFERENCES customers(id),
    order_number          VARCHAR(50) NOT NULL UNIQUE,
    total_amount          NUMERIC(19,2) NOT NULL,
    shipping_cost         NUMERIC(19,2),
    tax_amount            NUMERIC(19,2),
    discount_amount       NUMERIC(19,2),
    status                VARCHAR(50) NOT NULL,
    payment_status        VARCHAR(50) NOT NULL,
    delivery_address      TEXT,
    customer_email        VARCHAR(255),
    customer_phone_number VARCHAR(20),
    notes                 TEXT,
    completed_at          BIGINT,
    release_at            BIGINT,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted               BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_orders_store_id    ON orders(store_id);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status      ON orders(status);
CREATE INDEX idx_orders_deleted     ON orders(deleted);

-- ===========================================================================
-- ORDER_ITEMS
-- ===========================================================================
CREATE TABLE order_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID NOT NULL REFERENCES orders(id),
    product_id  UUID NOT NULL REFERENCES products(id),
    quantity    INT NOT NULL DEFAULT 1,
    unit_price  NUMERIC(19,2) NOT NULL,
    line_total  NUMERIC(19,2) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_order_items_order_id   ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_deleted    ON order_items(deleted);

-- ===========================================================================
-- CARTS
-- ===========================================================================
CREATE TABLE carts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID NOT NULL REFERENCES stores(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    status      VARCHAR(32) NOT NULL,
    subtotal    NUMERIC(19,2) NOT NULL DEFAULT 0,
    total       NUMERIC(19,2) NOT NULL DEFAULT 0,
    currency    VARCHAR(10) NOT NULL DEFAULT 'USD',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_carts_store_customer ON carts(store_id, customer_id);
CREATE INDEX idx_carts_status         ON carts(status);
CREATE INDEX idx_carts_deleted        ON carts(deleted);

-- ===========================================================================
-- CART_ITEMS
-- ===========================================================================
CREATE TABLE cart_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id     UUID NOT NULL REFERENCES carts(id),
    product_id  UUID NOT NULL REFERENCES products(id),
    quantity    INT NOT NULL DEFAULT 1,
    unit_price  NUMERIC(19,2) NOT NULL,
    line_total  NUMERIC(19,2) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_cart_items_cart_id    ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);
CREATE INDEX idx_cart_items_deleted    ON cart_items(deleted);

-- ===========================================================================
-- SUBSCRIPTION_PLANS
-- ===========================================================================
CREATE TABLE subscription_plans (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                  VARCHAR(100) NOT NULL UNIQUE,
    description           TEXT,
    product_limit         INT,
    staff_limit           INT,
    payments_enabled      BOOLEAN,
    custom_domain_enabled BOOLEAN,
    monthly_price         NUMERIC(19,2),
    active                BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted               BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_subscription_plans_name ON subscription_plans(name);

-- ===========================================================================
-- SUBSCRIPTIONS
-- ===========================================================================
CREATE TABLE subscriptions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id      UUID NOT NULL REFERENCES stores(id),
    plan_name     VARCHAR(100) NOT NULL,
    status        VARCHAR(50) NOT NULL,
    start_date    BIGINT,
    end_date      BIGINT,
    renewal_date  BIGINT,
    amount        NUMERIC(19,2) NOT NULL,
    billing_cycle VARCHAR(50),
    auto_renewal  BOOLEAN NOT NULL DEFAULT TRUE,
    features      TEXT,
    plan_id       UUID,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted       BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_subscriptions_store_id ON subscriptions(store_id);
CREATE INDEX idx_subscriptions_status   ON subscriptions(status);
CREATE INDEX idx_subscriptions_deleted  ON subscriptions(deleted);

-- ===========================================================================
-- BILLING_TRANSACTIONS
-- ===========================================================================
CREATE TABLE billing_transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id            UUID NOT NULL REFERENCES stores(id),
    order_id            UUID,
    transaction_id      VARCHAR(50) NOT NULL UNIQUE,
    amount              NUMERIC(19,2) NOT NULL,
    currency            VARCHAR(10),
    status              VARCHAR(50) NOT NULL,
    payment_method      VARCHAR(100),
    payment_provider    VARCHAR(50),
    transaction_details TEXT,
    failure_reason      TEXT,
    processed_at        BIGINT,
    release_at          BIGINT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_billing_store_id  ON billing_transactions(store_id);
CREATE INDEX idx_billing_order_id  ON billing_transactions(order_id);
CREATE INDEX idx_billing_status    ON billing_transactions(status);
CREATE INDEX idx_billing_deleted   ON billing_transactions(deleted);

-- ===========================================================================
-- PAYMENTS
-- ===========================================================================
CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id            UUID NOT NULL REFERENCES stores(id),
    order_id            UUID NOT NULL REFERENCES orders(id),
    transaction_id      VARCHAR(64) NOT NULL UNIQUE,
    provider_reference  VARCHAR(100),
    status              VARCHAR(50) NOT NULL,
    amount              NUMERIC(19,2) NOT NULL,
    currency            VARCHAR(10),
    payment_method      VARCHAR(100),
    payment_provider    VARCHAR(50),
    payload             TEXT,
    confirmed_at        BIGINT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_payments_order_id  ON payments(order_id);
CREATE INDEX idx_payments_store_id  ON payments(store_id);
CREATE INDEX idx_payments_status    ON payments(status);
CREATE INDEX idx_payments_deleted   ON payments(deleted);

-- ===========================================================================
-- ESCROW_TRANSACTIONS
-- Merged columns: V1 base + V2 fields (platform_fee_percent, seller_amount, released_at)
-- ===========================================================================
CREATE TABLE escrow_transactions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id             UUID NOT NULL REFERENCES stores(id),
    order_id             UUID NOT NULL REFERENCES orders(id),
    hold_amount          NUMERIC(19,2) NOT NULL,
    status               VARCHAR(32) NOT NULL,
    release_at           BIGINT,
    transaction_ref      VARCHAR(64),
    platform_fee_percent NUMERIC(5,2),
    seller_amount        NUMERIC(19,2),
    released_at          BIGINT,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted              BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_escrow_order_id  ON escrow_transactions(order_id);
CREATE INDEX idx_escrow_status    ON escrow_transactions(status);
CREATE INDEX idx_escrow_store_id  ON escrow_transactions(store_id);
CREATE INDEX idx_escrow_deleted   ON escrow_transactions(deleted);

-- ===========================================================================
-- ESCROW_DISPUTES
-- Merged columns: V1 base + V2 fields (evidence, admin_resolution_notes)
-- ===========================================================================
CREATE TABLE escrow_disputes (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escrow_transaction_id   UUID NOT NULL REFERENCES escrow_transactions(id),
    raised_by_user_id       UUID NOT NULL REFERENCES users(id),
    reason                  TEXT,
    status                  VARCHAR(32) NOT NULL,
    resolution_notes        TEXT,
    resolved_at             BIGINT,
    evidence                TEXT,
    admin_resolution_notes  TEXT,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_escrow_disputes_escrow_id ON escrow_disputes(escrow_transaction_id);
CREATE INDEX idx_escrow_disputes_status    ON escrow_disputes(status);
CREATE INDEX idx_escrow_disputes_deleted   ON escrow_disputes(deleted);

-- ===========================================================================
-- EMAIL_QUEUE
-- Merged columns: V1 base + V2 field (sent_at)
-- ===========================================================================
CREATE TABLE email_queue (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient       VARCHAR(255) NOT NULL,
    subject         VARCHAR(500) NOT NULL,
    template_name   VARCHAR(255) NOT NULL,
    payload_json    TEXT,
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    retry_count     INT NOT NULL DEFAULT 0,
    error_message   TEXT,
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- ===========================================================================
-- FRAUD_FLAGS
-- ===========================================================================
CREATE TABLE fraud_flags (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID NOT NULL REFERENCES stores(id),
    order_id    UUID,
    customer_id UUID,
    severity    VARCHAR(32) NOT NULL,
    reason      TEXT NOT NULL,
    reviewed    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_fraud_flags_store_id ON fraud_flags(store_id);
CREATE INDEX idx_fraud_flags_reviewed ON fraud_flags(reviewed);
CREATE INDEX idx_fraud_flags_deleted  ON fraud_flags(deleted);

-- ===========================================================================
-- KPI_SNAPSHOTS
-- ===========================================================================
CREATE TABLE kpi_snapshots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID,
    scope           VARCHAR(32) NOT NULL,
    revenue         NUMERIC(19,2) NOT NULL DEFAULT 0,
    order_count     BIGINT NOT NULL DEFAULT 0,
    customer_count  BIGINT NOT NULL DEFAULT 0,
    snapshot_date   DATE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_kpi_snapshots_store_date ON kpi_snapshots(store_id, snapshot_date);
CREATE INDEX idx_kpi_snapshots_deleted    ON kpi_snapshots(deleted);

-- ===========================================================================
-- COUPONS
-- Originally created in V2__feature_upgrades.sql
-- ===========================================================================
CREATE TABLE coupons (
    id              UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id        UUID          NOT NULL REFERENCES stores(id),
    code            VARCHAR(64)   NOT NULL,
    discount_type   VARCHAR(20)   NOT NULL,
    discount_value  NUMERIC(19,2) NOT NULL,
    min_order_value NUMERIC(19,2),
    max_uses        INTEGER,
    used_count      INTEGER       NOT NULL DEFAULT 0,
    expires_at      TIMESTAMPTZ,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted         BOOLEAN       NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_coupon_store_id ON coupons(store_id);
CREATE INDEX idx_coupon_code     ON coupons(code);
CREATE INDEX idx_coupon_deleted  ON coupons(deleted);

-- ===========================================================================
-- REFRESH_TOKENS
-- ===========================================================================
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- ===========================================================================
-- ORDER_TRACKING
-- ===========================================================================
CREATE TABLE order_tracking (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID NOT NULL REFERENCES orders(id),
    status      VARCHAR(60) NOT NULL,
    note        TEXT,
    updated_by  UUID REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_order_tracking_order_id ON order_tracking(order_id);
CREATE INDEX idx_order_tracking_status   ON order_tracking(status);

-- ===========================================================================
-- PRODUCT_VARIANTS
-- ===========================================================================
CREATE TABLE product_variants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID NOT NULL REFERENCES products(id),
    variant_name    VARCHAR(255) NOT NULL,
    sku             VARCHAR(100),
    price           NUMERIC(19,2) NOT NULL,
    stock_quantity  BIGINT NOT NULL DEFAULT 0,
    image_url       VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);

-- ===========================================================================
-- REVIEWS
-- ===========================================================================
CREATE TABLE reviews (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id  UUID NOT NULL REFERENCES products(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    store_id    UUID NOT NULL REFERENCES stores(id),
    rating      SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    verified    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (product_id, customer_id)
);
CREATE INDEX idx_reviews_product_id  ON reviews(product_id);
CREATE INDEX idx_reviews_customer_id ON reviews(customer_id);
CREATE INDEX idx_reviews_store_id    ON reviews(store_id);

-- ===========================================================================
-- WISHLISTS
-- ===========================================================================
CREATE TABLE wishlists (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    product_id  UUID NOT NULL REFERENCES products(id),
    store_id    UUID NOT NULL REFERENCES stores(id),
    saved_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (customer_id, product_id)
);
CREATE INDEX idx_wishlists_customer_id ON wishlists(customer_id);
CREATE INDEX idx_wishlists_product_id  ON wishlists(product_id);
