-- Add NOT NULL constraints and defaults
ALTER TABLE users 
  ALTER COLUMN status SET DEFAULT 'ACTIVE',
  ALTER COLUMN role SET DEFAULT 'CUSTOMER';

ALTER TABLE stores
  ALTER COLUMN is_active SET DEFAULT true;

ALTER TABLE staff
  ALTER COLUMN status SET DEFAULT 'ACTIVE';

ALTER TABLE products
  ALTER COLUMN status SET DEFAULT 'ACTIVE';

ALTER TABLE orders
  ALTER COLUMN status SET DEFAULT 'PENDING',
  ALTER COLUMN payment_status SET DEFAULT 'PENDING';

ALTER TABLE subscriptions
  ALTER COLUMN status SET DEFAULT 'ACTIVE',
  ALTER COLUMN auto_renewal SET DEFAULT true;

ALTER TABLE billing_transactions
  ALTER COLUMN status SET DEFAULT 'PENDING';

-- Add check constraints for valid enum values
ALTER TABLE users
  ADD CONSTRAINT chk_user_role CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'BUSINESS_OWNER', 'STAFF', 'CUSTOMER')),
  ADD CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'BANNED'));

ALTER TABLE staff
  ADD CONSTRAINT chk_staff_role CHECK (role IN ('MANAGER', 'ADMIN', 'INVENTORY', 'SUPPORT', 'MARKETING', 'FINANCIAL')),
  ADD CONSTRAINT chk_staff_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED'));

ALTER TABLE products
  ADD CONSTRAINT chk_product_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED', 'OUT_OF_STOCK'));

ALTER TABLE orders
  ADD CONSTRAINT chk_order_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED')),
  ADD CONSTRAINT chk_payment_status CHECK (payment_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED'));

ALTER TABLE subscriptions
  ADD CONSTRAINT chk_subscription_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'PAUSED', 'CANCELLED', 'EXPIRED'));

ALTER TABLE billing_transactions
  ADD CONSTRAINT chk_billing_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'PROCESSING', 'REFUNDED'));

-- Add unique constraints
ALTER TABLE stores ADD CONSTRAINT uk_stores_slug UNIQUE (slug);
ALTER TABLE products ADD CONSTRAINT uk_products_sku_store UNIQUE (sku, store_id);
ALTER TABLE orders ADD CONSTRAINT uk_orders_order_number UNIQUE (order_number);
ALTER TABLE billing_transactions ADD CONSTRAINT uk_billing_transaction_id UNIQUE (transaction_id);

-- Add check constraints for amounts (must be positive)
ALTER TABLE products
  ADD CONSTRAINT chk_product_price_positive CHECK (price >= 0),
  ADD CONSTRAINT chk_product_cost_price_positive CHECK (cost_price IS NULL OR cost_price >= 0);

ALTER TABLE orders
  ADD CONSTRAINT chk_order_total_positive CHECK (total_amount >= 0);

ALTER TABLE subscriptions
  ADD CONSTRAINT chk_subscription_amount_positive CHECK (amount >= 0);

ALTER TABLE billing_transactions
  ADD CONSTRAINT chk_billing_amount_positive CHECK (amount >= 0);
