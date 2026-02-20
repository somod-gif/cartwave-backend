-- Consolidated initial schema for CartWave (moved to cartwave/ location to avoid duplicate migration conflicts)
-- Creates core tables and seeds default subscription plans

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone_number VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE NOT NULL,
    profile_picture_url TEXT,
    bio TEXT,
    last_login_at BIGINT,
    created_at BIGINT,
    updated_at BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);

-- stores
CREATE TABLE IF NOT EXISTS stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    handle VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    banner_url VARCHAR(500),
    domain VARCHAR(255),
    custom_domain BOOLEAN DEFAULT FALSE,
    currency VARCHAR(10),
    created_at BIGINT,
    updated_at BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);

-- staff
CREATE TABLE IF NOT EXISTS staff (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    hired_at BIGINT,
    notes TEXT,
    permission_level INTEGER DEFAULT 0,
    created_at BIGINT,
    updated_at BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_staff_store_id ON staff(store_id);
CREATE INDEX IF NOT EXISTS idx_staff_user_id ON staff(user_id);

-- products
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19,2) NOT NULL,
    cost_price NUMERIC(19,2),
    stock BIGINT DEFAULT 0,
    low_stock_threshold BIGINT,
    sku VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    images TEXT,
    category VARCHAR(255),
    attributes TEXT,
    created_at BIGINT,
    updated_at BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_products_store_id ON products(store_id);

-- orders
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    user_id UUID,
    total_amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(10),
    status VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50),
    completed_at BIGINT,
    created_at BIGINT,
    updated_at BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_orders_store_id ON orders(store_id);

-- subscription_plans
CREATE TABLE IF NOT EXISTS subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    product_limit INTEGER DEFAULT 0,
    staff_limit INTEGER DEFAULT 0,
    payments_enabled BOOLEAN DEFAULT FALSE,
    custom_domain_enabled BOOLEAN DEFAULT FALSE,
    price NUMERIC(19,2) DEFAULT 0,
    created_at BIGINT,
    updated_at BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);

-- subscriptions
CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date BIGINT,
    end_date BIGINT,
    renewal_date BIGINT,
    amount NUMERIC(19,2) DEFAULT 0,
    billing_cycle VARCHAR(50),
    auto_renewal BOOLEAN DEFAULT TRUE,
    features TEXT,
    created_at BIGINT,
    updated_at BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_subscriptions_store_id ON subscriptions(store_id);

-- billing_transactions
CREATE TABLE IF NOT EXISTS billing_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(10),
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(100),
    payment_provider VARCHAR(50),
    transaction_details TEXT,
    failure_reason TEXT,
    processed_at BIGINT,
    created_at BIGINT,
    updated_at BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);

-- Seed default plans
INSERT INTO subscription_plans (id, name, description, product_limit, staff_limit, payments_enabled, custom_domain_enabled, price, created_at)
VALUES
    (gen_random_uuid(), 'FREE', 'Free tier with basic features', 20, 1, FALSE, FALSE, 0.00, extract(epoch FROM now())::bigint),
    (gen_random_uuid(), 'STARTER', 'Starter plan', 100, 3, TRUE, FALSE, 19.00, extract(epoch FROM now())::bigint),
    (gen_random_uuid(), 'PRO', 'Professional plan', 1000, 10, TRUE, TRUE, 99.00, extract(epoch FROM now())::bigint),
    (gen_random_uuid(), 'ENTERPRISE', 'Enterprise plan - unlimited products & staff', 0, 0, TRUE, TRUE, 499.00, extract(epoch FROM now())::bigint)
ON CONFLICT DO NOTHING;

-- Notes:
-- product_limit = 0 indicates no limit (unlimited)
-- staff_limit = 0 indicates no limit

COMMIT;

