-- CartWave V1 init — single authoritative migration
-- Reset is handled by FlywayConfig.java (drops tables via SQL before migrate)

-- ===========================================================================
-- USERS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    phone_number    VARCHAR(255),
    role            VARCHAR(50) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    profile_picture_url TEXT,
    bio             TEXT,
    last_login_at   BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_users_email   ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status  ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_deleted ON users(deleted);

-- ===========================================================================
-- STORES
-- ===========================================================================
CREATE TABLE IF NOT EXISTS stores (
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
    created_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted                         BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_stores_slug       ON stores(slug);
CREATE INDEX IF NOT EXISTS idx_stores_owner_id   ON stores(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_stores_is_active  ON stores(active);
CREATE INDEX IF NOT EXISTS idx_stores_deleted    ON stores(deleted);

-- ===========================================================================
-- PRODUCTS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS products (
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
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_products_store_id ON products(store_id);
CREATE INDEX IF NOT EXISTS idx_products_status   ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_deleted  ON products(deleted);

-- ===========================================================================
-- CUSTOMERS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS customers (
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
CREATE INDEX IF NOT EXISTS idx_customers_user_store ON customers(user_id, store_id);
CREATE INDEX IF NOT EXISTS idx_customers_deleted    ON customers(deleted);

-- ===========================================================================
-- STAFF
-- ===========================================================================
CREATE TABLE IF NOT EXISTS staff (
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
CREATE INDEX IF NOT EXISTS idx_staff_store_id ON staff(store_id);
CREATE INDEX IF NOT EXISTS idx_staff_user_id  ON staff(user_id);
CREATE INDEX IF NOT EXISTS idx_staff_deleted  ON staff(deleted);

-- ===========================================================================
-- ORDERS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS orders (
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
CREATE INDEX IF NOT EXISTS idx_orders_store_id    ON orders(store_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status      ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_deleted     ON orders(deleted);

-- ===========================================================================
-- ORDER_ITEMS  (referenced in spec, not currently an entity — placeholder)
-- ===========================================================================
CREATE TABLE IF NOT EXISTS order_items (
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
CREATE INDEX IF NOT EXISTS idx_order_items_order_id   ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_order_items_deleted    ON order_items(deleted);

-- ===========================================================================
-- CARTS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS carts (
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
CREATE INDEX IF NOT EXISTS idx_carts_store_customer ON carts(store_id, customer_id);
CREATE INDEX IF NOT EXISTS idx_carts_status         ON carts(status);
CREATE INDEX IF NOT EXISTS idx_carts_deleted        ON carts(deleted);

-- ===========================================================================
-- CART_ITEMS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS cart_items (
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
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id    ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON cart_items(product_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_deleted    ON cart_items(deleted);

-- ===========================================================================
-- SUBSCRIPTION_PLANS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS subscription_plans (
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
CREATE INDEX IF NOT EXISTS idx_subscription_plans_name ON subscription_plans(name);

-- ===========================================================================
-- SUBSCRIPTIONS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS subscriptions (
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
CREATE INDEX IF NOT EXISTS idx_subscriptions_store_id ON subscriptions(store_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status   ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_deleted  ON subscriptions(deleted);

-- ===========================================================================
-- BILLING_TRANSACTIONS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS billing_transactions (
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
CREATE INDEX IF NOT EXISTS idx_billing_store_id  ON billing_transactions(store_id);
CREATE INDEX IF NOT EXISTS idx_billing_order_id  ON billing_transactions(order_id);
CREATE INDEX IF NOT EXISTS idx_billing_status    ON billing_transactions(status);
CREATE INDEX IF NOT EXISTS idx_billing_deleted   ON billing_transactions(deleted);

-- ===========================================================================
-- PAYMENTS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS payments (
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
CREATE INDEX IF NOT EXISTS idx_payments_order_id  ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_store_id  ON payments(store_id);
CREATE INDEX IF NOT EXISTS idx_payments_status    ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_deleted   ON payments(deleted);

-- ===========================================================================
-- ESCROW_TRANSACTIONS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS escrow_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID NOT NULL REFERENCES stores(id),
    order_id        UUID NOT NULL REFERENCES orders(id),
    hold_amount     NUMERIC(19,2) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    release_at      BIGINT,
    transaction_ref VARCHAR(64),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_escrow_order_id  ON escrow_transactions(order_id);
CREATE INDEX IF NOT EXISTS idx_escrow_status    ON escrow_transactions(status);
CREATE INDEX IF NOT EXISTS idx_escrow_store_id  ON escrow_transactions(store_id);
CREATE INDEX IF NOT EXISTS idx_escrow_deleted   ON escrow_transactions(deleted);

-- ===========================================================================
-- ESCROW_DISPUTES
-- ===========================================================================
CREATE TABLE IF NOT EXISTS escrow_disputes (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escrow_transaction_id   UUID NOT NULL REFERENCES escrow_transactions(id),
    raised_by_user_id       UUID NOT NULL REFERENCES users(id),
    reason                  TEXT,
    status                  VARCHAR(32) NOT NULL,
    resolution_notes        TEXT,
    resolved_at             BIGINT,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_escrow_disputes_escrow_id ON escrow_disputes(escrow_transaction_id);
CREATE INDEX IF NOT EXISTS idx_escrow_disputes_status    ON escrow_disputes(status);
CREATE INDEX IF NOT EXISTS idx_escrow_disputes_deleted   ON escrow_disputes(deleted);

-- ===========================================================================
-- EMAIL_QUEUE
-- ===========================================================================
CREATE TABLE IF NOT EXISTS email_queue (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient       VARCHAR(255) NOT NULL,
    subject         VARCHAR(500) NOT NULL,
    template_name   VARCHAR(255) NOT NULL,
    payload_json    TEXT,
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    retry_count     INT NOT NULL DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- ===========================================================================
-- FRAUD_FLAGS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS fraud_flags (
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
CREATE INDEX IF NOT EXISTS idx_fraud_flags_store_id ON fraud_flags(store_id);
CREATE INDEX IF NOT EXISTS idx_fraud_flags_reviewed ON fraud_flags(reviewed);
CREATE INDEX IF NOT EXISTS idx_fraud_flags_deleted  ON fraud_flags(deleted);

-- ===========================================================================
-- KPI_SNAPSHOTS
-- ===========================================================================
CREATE TABLE IF NOT EXISTS kpi_snapshots (
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
CREATE INDEX IF NOT EXISTS idx_kpi_snapshots_store_date ON kpi_snapshots(store_id, snapshot_date);
CREATE INDEX IF NOT EXISTS idx_kpi_snapshots_deleted    ON kpi_snapshots(deleted);
