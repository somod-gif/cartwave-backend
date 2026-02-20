-- USERS
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- STORES
CREATE TABLE stores (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    owner_id UUID REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- STAFF
CREATE TABLE staff (
    id UUID PRIMARY KEY,
    store_id UUID REFERENCES stores(id),
    user_id UUID REFERENCES users(id),
    role VARCHAR(100),
    status VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- PRODUCTS
CREATE TABLE products (
    id UUID PRIMARY KEY,
    store_id UUID REFERENCES stores(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(255),
    price NUMERIC(19,2) NOT NULL,
    cost_price NUMERIC(19,2),
    status VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- ORDERS
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    store_id UUID REFERENCES stores(id),
    customer_id UUID REFERENCES users(id),
    order_number VARCHAR(255),
    status VARCHAR(50),
    payment_status VARCHAR(50),
    total_amount NUMERIC(19,2),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- SUBSCRIPTIONS
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    store_id UUID REFERENCES stores(id),
    status VARCHAR(50),
    auto_renewal BOOLEAN DEFAULT TRUE,
    amount NUMERIC(19,2),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- BILLING TRANSACTIONS
CREATE TABLE billing_transactions (
    id UUID PRIMARY KEY,
    store_id UUID REFERENCES stores(id),
    transaction_id VARCHAR(255),
    status VARCHAR(50),
    amount NUMERIC(19,2),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);