-- Ensure referenced columns exist (idempotent, safe for existing DBs)
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE stores ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE staff ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE products ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE products ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE products ADD COLUMN IF NOT EXISTS sku VARCHAR(255);

ALTER TABLE orders ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE billing_transactions ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE billing_transactions ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE billing_transactions ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(255);

-- Add indexes for Users table
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_deleted ON users(deleted);

-- Add indexes for Stores table
CREATE INDEX IF NOT EXISTS idx_stores_slug ON stores(slug);
CREATE INDEX IF NOT EXISTS idx_stores_owner_id ON stores(owner_id);
CREATE INDEX IF NOT EXISTS idx_stores_is_active ON stores(is_active);
CREATE INDEX IF NOT EXISTS idx_stores_deleted ON stores(deleted);

-- Add indexes for Staff table
CREATE INDEX IF NOT EXISTS idx_staff_store_id ON staff(store_id);
CREATE INDEX IF NOT EXISTS idx_staff_user_id ON staff(user_id);
CREATE INDEX IF NOT EXISTS idx_staff_deleted ON staff(deleted);

-- Add indexes for Products table
CREATE INDEX IF NOT EXISTS idx_products_store_id ON products(store_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_deleted ON products(deleted);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);

-- Add indexes for Orders table
CREATE INDEX IF NOT EXISTS idx_orders_store_id ON orders(store_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_deleted ON orders(deleted);
CREATE INDEX IF NOT EXISTS idx_orders_order_number ON orders(order_number);

-- Add indexes for Subscriptions table
CREATE INDEX IF NOT EXISTS idx_subscriptions_store_id ON subscriptions(store_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_deleted ON subscriptions(deleted);

-- Add indexes for Billing Transactions table
CREATE INDEX IF NOT EXISTS idx_billing_store_id ON billing_transactions(store_id);
CREATE INDEX IF NOT EXISTS idx_billing_status ON billing_transactions(status);
CREATE INDEX IF NOT EXISTS idx_billing_deleted ON billing_transactions(deleted);
CREATE INDEX IF NOT EXISTS idx_billing_transaction_id ON billing_transactions(transaction_id);

-- Add composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_staff_user_store ON staff(user_id, store_id) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_products_store_status ON products(store_id, status) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_orders_store_status ON orders(store_id, status) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_billing_store_status ON billing_transactions(store_id, status) WHERE deleted = false;
