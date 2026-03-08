# CartWave API Reference

Complete API documentation for building a frontend against the CartWave backend. All endpoints return JSON. Base URL: `http://localhost:8080`

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [Response Format](#2-response-format)
3. [Roles & Permissions](#3-roles--permissions)
4. [Enums & Status Values](#4-enums--status-values)
5. [API Endpoints](#5-api-endpoints)
   - [Auth](#51-auth)
   - [Stores](#52-stores)
   - [Public Catalog](#53-public-catalog)
   - [Products](#54-products)
   - [Cart](#55-cart)
   - [Checkout](#56-checkout)
   - [Orders](#57-orders)
   - [Payments](#58-payments)
   - [Customers](#59-customers)
   - [Staff](#510-staff)
   - [Subscriptions](#511-subscriptions)
   - [Billing](#512-billing)
   - [Dashboard](#513-dashboard)
   - [Admin Dashboard](#514-admin-dashboard)
   - [Super Admin](#515-super-admin)
   - [Email Queue](#516-email-queue)
   - [Health](#517-health)
6. [Database Schema](#6-database-schema)
7. [Frontend Integration Guide](#7-frontend-integration-guide)

---

## 1. Authentication

**Type:** JWT Bearer Token (stateless, no cookies/sessions)

### How it works

1. Call `POST /api/v1/auth/login` or `POST /api/v1/auth/register` to get tokens
2. Include the access token in every authenticated request:
   ```
   Authorization: Bearer <accessToken>
   ```
3. When the access token expires (15 min default), call `POST /api/v1/auth/refresh` with the refresh token to get a new pair
4. The JWT contains: `userId`, `email`, `role`, `storeId`, `permissions`

### Token Lifetimes

| Token | Default TTL |
|-------|-------------|
| Access token | 15 minutes |
| Refresh token | 7 days |

### Public Endpoints (no token required)

```
POST   /api/v1/auth/login
POST   /api/v1/auth/register
POST   /api/v1/auth/refresh
POST   /api/v1/customers/register
GET    /api/v1/health
GET    /api/v1/public/stores/{slug}
GET    /api/v1/public/stores/{slug}/products
```

All other endpoints require a valid Bearer token.

---

## 2. Response Format

Every API response is wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "error": null
}
```

**Error response:**

```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "error": {
    "errorCode": "VALIDATION_ERROR",
    "errorMessage": "Validation failed",
    "statusCode": 400,
    "timestamp": "2026-03-08T10:00:00Z",
    "path": "/api/v1/auth/login",
    "fieldErrors": {
      "email": "must not be blank",
      "password": "must not be blank"
    }
  }
}
```

**Null fields are omitted** from JSON responses (`@JsonInclude(NON_NULL)`).

---

## 3. Roles & Permissions

| Role | Description | Can Access |
|------|-------------|------------|
| `SUPER_ADMIN` | Platform owner | Everything + super admin dashboard, system-wide stats |
| `BUSINESS_OWNER` | Store owner | Store management, products, orders, staff, subscriptions, billing, dashboards |
| `ADMIN` | Store administrator | Same as BUSINESS_OWNER (assigned to a store) |
| `STAFF` | Store staff member | Products, orders, billing, dashboard (read-heavy, limited write) |
| `CUSTOMER` | End-user shopper | Cart, checkout, payments, own orders, own profile |

### Role-Based Access Matrix

| Endpoint Group | SUPER_ADMIN | BUSINESS_OWNER | ADMIN | STAFF | CUSTOMER |
|---------------|:-----------:|:--------------:|:-----:|:-----:|:--------:|
| Auth (login/register/refresh) | — | — | — | — | — |
| Public catalog | — | — | — | — | — |
| Stores CRUD | Read | Full | Full | Read | — |
| Products CRUD | — | Full | Full | Full | — |
| Cart | — | — | — | — | Full |
| Checkout | — | — | — | — | Full |
| Orders (list/view) | Yes | Yes | Yes | Yes | Own only |
| Orders (create/update/status) | — | Yes | Yes | Yes | — |
| Payments (initiate) | — | — | — | — | Full |
| Payments (confirm/webhook) | Yes | Yes | — | — | Yes |
| Staff management | — | Full | Full | Read | — |
| Subscriptions | — | Full | Full | Read | — |
| Billing transactions | — | Yes | Yes | Yes | — |
| Dashboard metrics | Yes | Yes | Yes | Yes | — |
| Admin dashboard | — | Yes | Yes | Yes | — |
| Super admin dashboard | Full | — | — | — | — |
| Customers (view) | Yes | Yes | Yes | Yes | Own only |
| Email queue | Open | Open | Open | Open | Open |

*(— = public/no auth required, Open = no role restriction on endpoint)*

---

## 4. Enums & Status Values

### User Roles
`SUPER_ADMIN` | `ADMIN` | `BUSINESS_OWNER` | `STAFF` | `CUSTOMER`

### Staff Roles
`MANAGER` | `ADMIN` | `INVENTORY` | `SUPPORT` | `MARKETING` | `FINANCIAL`

### User Status
`ACTIVE` | `INACTIVE` | `SUSPENDED` | `BANNED`

### Order Status
`PENDING` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED`  
*(can also go to `CANCELLED` or `REFUNDED` from most states)*

### Payment Status
`PENDING` | `PROCESSING` | `COMPLETED` | `FAILED` | `REFUNDED` | `PARTIALLY_REFUNDED`

### Product Status
`ACTIVE` | `INACTIVE` | `ARCHIVED` | `OUT_OF_STOCK`

### Cart Status
`ACTIVE` | `CHECKED_OUT` | `ABANDONED`

### Billing Status
`PENDING` | `HOLD` | `RELEASED` | `COMPLETED` | `FAILED` | `PROCESSING` | `REFUNDED`

### Escrow Status
`HELD` | `RELEASED` | `DISPUTED` | `REFUNDED` | `CANCELLED`

### Escrow Dispute Status
`OPEN` | `UNDER_REVIEW` | `RESOLVED` | `REJECTED`

### Email Status
`PENDING` | `SENT` | `FAILED`

### Subscription Status
`ACTIVE` | `INACTIVE` | `PAUSED` | `CANCELLED` | `EXPIRED`

### Staff Status
`ACTIVE` | `INACTIVE` | `ON_LEAVE` | `TERMINATED`

### Fraud Severity
`LOW` | `MEDIUM` | `HIGH`

---

## 5. API Endpoints

### 5.1 Auth

#### POST `/api/v1/auth/register`
Register a new user. **Public.**

**Request:**
```json
{
  "email": "owner@example.com",        // required, valid email
  "password": "securePassword123",      // required
  "role": "BUSINESS_OWNER",            // required: "BUSINESS_OWNER" or "CUSTOMER"
  "firstName": "John",                 // optional
  "lastName": "Doe",                   // optional
  "phoneNumber": "+1234567890",        // optional
  "storeId": null                      // optional, UUID — required if role is CUSTOMER
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "uuid",
    "email": "owner@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "BUSINESS_OWNER",
    "status": "ACTIVE",
    "emailVerified": false,
    "profilePictureUrl": null
  }
}
```

---

#### POST `/api/v1/auth/login`
Authenticate and receive JWT tokens. **Public.**

**Request:**
```json
{
  "email": "owner@example.com",     // required, valid email
  "password": "securePassword123",  // required
  "storeId": "uuid"                // optional — for staff/customer scoped to a store
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

---

#### POST `/api/v1/auth/refresh`
Get a new token pair using a refresh token. **Public.**

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."  // required
}
```

**Response:** Same shape as login response.

---

### 5.2 Stores

All endpoints require auth. BUSINESS_OWNER/ADMIN/SUPER_ADMIN can create/update/delete. STAFF has read access.

#### GET `/api/v1/stores`
List all stores accessible to the authenticated user.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "My Store",
      "slug": "my-store",
      "description": "A great store",
      "country": "US",
      "currency": "USD",
      "subscriptionPlan": "FREE",
      "isActive": true,
      "logoUrl": "https://...",
      "websiteUrl": "https://...",
      "customDomain": false,
      "bannerUrl": "https://...",
      "businessAddress": "123 Main St",
      "businessRegistrationNumber": "REG123",
      "businessPhoneNumber": "+1234567890",
      "businessEmail": "store@example.com",
      "ownerId": "uuid",
      "createdAt": "2026-03-08T10:00:00Z",
      "updatedAt": "2026-03-08T10:00:00Z"
    }
  ]
}
```

---

#### POST `/api/v1/stores`
Create a new store. Requires BUSINESS_OWNER/ADMIN/SUPER_ADMIN.

**Request:**
```json
{
  "name": "My Store",                           // required
  "slug": "my-store",                           // required, unique, URL-friendly
  "description": "A great store",               // optional
  "country": "US",                              // optional
  "currency": "USD",                            // optional
  "logoUrl": "https://...",                      // optional
  "bannerUrl": "https://...",                    // optional
  "websiteUrl": "https://mystore.com",          // optional
  "businessAddress": "123 Main St",             // optional
  "businessRegistrationNumber": "REG123",       // optional
  "businessPhoneNumber": "+1234567890",         // optional
  "businessEmail": "store@example.com"          // optional
}
```

**Response (201):** `StoreDTO` (same shape as GET response item)

---

#### GET `/api/v1/stores/{storeId}`
Get a single store by ID.

---

#### PUT `/api/v1/stores/{storeId}`
Update a store. Requires BUSINESS_OWNER/ADMIN/SUPER_ADMIN.

**Request:**
```json
{
  "name": "Updated Name",                      // optional
  "description": "Updated description",        // optional
  "country": "US",                             // optional
  "currency": "USD",                           // optional
  "subscriptionPlan": "PREMIUM",               // optional
  "isActive": true,                            // optional
  "logoUrl": "https://...",                     // optional
  "bannerUrl": "https://...",                   // optional
  "websiteUrl": "https://...",                  // optional
  "businessAddress": "456 Oak Ave",            // optional
  "businessRegistrationNumber": "REG456",      // optional
  "businessPhoneNumber": "+0987654321",        // optional
  "businessEmail": "new@example.com",          // optional
  "customDomain": true                         // optional
}
```

---

#### DELETE `/api/v1/stores/{storeId}`
Soft-delete a store. Requires BUSINESS_OWNER/ADMIN/SUPER_ADMIN.

**Response:** `ApiResponse<Void>` with `success: true`

---

### 5.3 Public Catalog

**No authentication required.** These endpoints power the customer-facing storefront.

#### GET `/api/v1/public/stores/{slug}`
Get a store's public profile by its URL slug.

**Response:** `ApiResponse<StoreDTO>`

---

#### GET `/api/v1/public/stores/{slug}/products`
List all products in a store (public catalog).

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "storeId": "uuid",
      "name": "Product Name",
      "description": "Product description",
      "price": 29.99,
      "costPrice": 15.00,
      "stock": 100,
      "lowStockThreshold": 10,
      "sku": "SKU-001",
      "status": "ACTIVE",
      "imageUrl": "https://...",
      "images": "url1,url2,url3",
      "category": "Electronics",
      "attributes": "{\"color\":\"red\",\"size\":\"M\"}"
    }
  ]
}
```

---

### 5.4 Products

Requires auth. BUSINESS_OWNER/ADMIN/STAFF roles.

#### POST `/api/v1/products`
Create a product. **(201)**

**Request:**
```json
{
  "storeId": "uuid",                   // optional (may be inferred from JWT)
  "name": "Product Name",
  "description": "Description",
  "price": 29.99,
  "costPrice": 15.00,
  "stock": 100,
  "lowStockThreshold": 10,
  "sku": "SKU-001",
  "status": "ACTIVE",
  "imageUrl": "https://...",
  "images": "url1,url2",
  "category": "Electronics",
  "attributes": "{\"color\":\"red\"}"
}
```

**Response:** `ApiResponse<ProductDTO>`

---

#### GET `/api/v1/products`
List all products for the authenticated user's store.

---

#### GET `/api/v1/products/{id}`
Get a single product by ID.

---

#### PUT `/api/v1/products/{id}`
Update a product. Same request body as POST.

---

#### DELETE `/api/v1/products/{id}`
Soft-delete a product.

---

### 5.5 Cart

All cart endpoints require `CUSTOMER` role.

#### GET `/api/v1/cart`
Get the current customer's active cart.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "storeId": "uuid",
    "customerId": "uuid",
    "status": "ACTIVE",
    "subtotal": 59.98,
    "total": 59.98,
    "currency": "USD",
    "items": [
      {
        "id": "uuid",
        "productId": "uuid",
        "productName": "Product Name",
        "quantity": 2,
        "unitPrice": 29.99,
        "lineTotal": 59.98
      }
    ]
  }
}
```

---

#### GET `/api/v1/cart/items`
Same as `GET /api/v1/cart` — returns the full cart with items.

---

#### POST `/api/v1/cart/items`
Add an item to the cart.

**Request:**
```json
{
  "productId": "uuid",   // required
  "quantity": 2           // required, minimum 1
}
```

**Response:** `ApiResponse<CartDTO>` (full updated cart)

---

#### PATCH `/api/v1/cart/items/{itemId}`
Update the quantity of a cart item.

**Request:**
```json
{
  "productId": "uuid",   // required
  "quantity": 3           // required, minimum 1
}
```

**Response:** `ApiResponse<CartDTO>` (full updated cart)

---

#### DELETE `/api/v1/cart/items/{itemId}`
Remove an item from the cart.

**Response:** `ApiResponse<Void>`

---

### 5.6 Checkout

Requires `CUSTOMER` role.

#### POST `/api/v1/checkout`
Convert the active cart into an order and billing transaction.

**Request:**
```json
{
  "deliveryAddress": "123 Main St, City",  // required
  "customerEmail": "john@example.com",     // optional
  "customerPhoneNumber": "+1234567890",    // optional
  "notes": "Leave at door",               // optional
  "paymentMethod": "CARD",                // optional
  "paymentProvider": "STRIPE"             // optional
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "orderId": "uuid",
    "orderNumber": "ORD-20260308-ABC123",
    "billingTransactionId": "uuid",
    "transactionId": "TXN-1234567890",
    "totalAmount": 59.98,
    "orderStatus": "PENDING",
    "paymentStatus": "PENDING"
  }
}
```

---

### 5.7 Orders

#### GET `/api/v1/orders`
List orders. Customers see only their own orders. Business roles see store orders.

Requires: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`, `STAFF`, `SUPER_ADMIN`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "storeId": "uuid",
      "orderNumber": "ORD-20260308-ABC123",
      "customerId": "uuid",
      "totalAmount": 59.98,
      "shippingCost": 5.00,
      "taxAmount": 3.50,
      "discountAmount": 0.00,
      "status": "PENDING",
      "paymentStatus": "PENDING",
      "deliveryAddress": "123 Main St",
      "customerEmail": "john@example.com",
      "customerPhoneNumber": "+1234567890",
      "notes": "Leave at door",
      "completedAt": null,
      "releaseAt": null,
      "createdAt": "2026-03-08T10:00:00Z"
    }
  ]
}
```

---

#### GET `/api/v1/orders/{orderId}`
Get a single order by ID.

---

#### POST `/api/v1/orders`
Create an order directly (admin/staff use). **(201)**

Requires: `BUSINESS_OWNER`, `ADMIN`, `STAFF`

**Request:** `OrderDTO` fields.

---

#### PUT `/api/v1/orders/{orderId}`
Update an order.

Requires: `BUSINESS_OWNER`, `ADMIN`, `STAFF`

---

#### PATCH `/api/v1/orders/{orderId}/status`
Update only the order status.

Requires: `BUSINESS_OWNER`, `ADMIN`, `STAFF`

**Request:**
```json
{
  "status": "CONFIRMED"   // required — see Order Status enum values
}
```

---

### 5.8 Payments

#### POST `/api/v1/payments/initiate`
Initiate a payment for an order. Requires `CUSTOMER`.

**Request:**
```json
{
  "orderId": "uuid",              // required
  "paymentMethod": "CARD",        // required
  "paymentProvider": "STRIPE"     // required
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "transactionId": "TXN-1234567890",
    "status": "PENDING",
    "paymentProvider": "STRIPE",
    "paymentMethod": "CARD"
  }
}
```

---

#### POST `/api/v1/payments/confirm`
Confirm a payment (after external payment succeeds/fails).

Requires: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`, `SUPER_ADMIN`

**Request:**
```json
{
  "transactionId": "TXN-1234567890",  // required
  "status": "SUCCESS",                // required: "SUCCESS" or "FAILED"
  "providerReference": "pi_abc123"    // optional — external provider reference
}
```

---

#### POST `/api/v1/payments/webhook`
Receive payment status updates (webhook endpoint).

Requires: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`, `SUPER_ADMIN`

**Request:**
```json
{
  "transactionId": "TXN-1234567890",  // required
  "status": "SUCCESS",                // required
  "failureReason": null               // optional — reason if failed
}
```

---

### 5.9 Customers

#### POST `/api/v1/customers/register`
Register as a customer. **Public.**

**Request:** Same as auth register with `role: "CUSTOMER"`.

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "storeId": "uuid",
    "email": "customer@example.com",
    "firstName": "Jane",
    "lastName": "Doe",
    "phone": "+1234567890"
  }
}
```

---

#### GET `/api/v1/customers/me`
Get the authenticated customer's profile. Requires `CUSTOMER`.

---

#### GET `/api/v1/customers/{customerId}`
Get any customer's profile. Requires `BUSINESS_OWNER`, `ADMIN`, `STAFF`, `SUPER_ADMIN`, or self.

---

### 5.10 Staff

#### GET `/api/v1/staff`
List all staff for the current store.

Requires: `BUSINESS_OWNER`, `ADMIN`, `STAFF`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "userId": "uuid",
      "storeId": "uuid",
      "role": "MANAGER",
      "status": "ACTIVE",
      "permissionLevel": "FULL",
      "notes": "Senior manager",
      "hiredAt": 1709856000000,
      "createdAt": "2026-03-08T10:00:00Z"
    }
  ]
}
```

---

#### POST `/api/v1/staff`
Add a staff member to the store.

Requires: `BUSINESS_OWNER`, `ADMIN`

**Request:**
```json
{
  "userId": "uuid",        // the user's UUID to add as staff
  "role": "MANAGER"        // StaffRole: MANAGER, ADMIN, INVENTORY, SUPPORT, MARKETING, FINANCIAL
}
```

---

#### DELETE `/api/v1/staff/{staffId}`
Remove a staff member.

Requires: `BUSINESS_OWNER`, `ADMIN`

---

### 5.11 Subscriptions

#### GET `/api/v1/subscriptions/plans`
List all available subscription plans.

Requires: `BUSINESS_OWNER`, `ADMIN`, `STAFF`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "FREE",
      "description": "Basic free plan",
      "productLimit": 10,
      "staffLimit": 1,
      "paymentsEnabled": false,
      "customDomainEnabled": false,
      "price": 0.00,
      "active": true
    }
  ]
}
```

---

#### GET `/api/v1/subscriptions/current` or GET `/api/v1/subscriptions`
Get the current store's active subscription.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "storeId": "uuid",
    "planName": "FREE",
    "status": "ACTIVE",
    "amount": 0.00,
    "billingCycle": "MONTHLY",
    "autoRenewal": true,
    "startDate": 1709856000000,
    "endDate": 1712534400000,
    "renewalDate": 1712534400000,
    "features": "basic",
    "planId": "uuid",
    "createdAt": "2026-03-08T10:00:00Z"
  }
}
```

---

#### POST `/api/v1/subscriptions/change` or POST `/api/v1/subscriptions`
Change the store's subscription plan.

Requires: `BUSINESS_OWNER`, `ADMIN`

**Request:**
```json
{
  "planName": "PREMIUM",       // required
  "billingCycle": "MONTHLY",   // optional: "MONTHLY" or "YEARLY"
  "autoRenewal": true          // optional
}
```

---

### 5.12 Billing

#### GET `/api/v1/billing/transactions`
List billing transactions for the current store.

Requires: `BUSINESS_OWNER`, `ADMIN`, `STAFF`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "storeId": "uuid",
      "orderId": "uuid",
      "transactionId": "TXN-1234567890",
      "amount": 59.98,
      "currency": "USD",
      "status": "COMPLETED",
      "paymentMethod": "CARD",
      "paymentProvider": "STRIPE",
      "transactionDetails": "...",
      "failureReason": null,
      "processedAt": 1709856000000,
      "releaseAt": 1710460800000,
      "createdAt": "2026-03-08T10:00:00Z"
    }
  ]
}
```

---

### 5.13 Dashboard

#### GET `/api/v1/dashboard/metrics`
Get dashboard KPIs for the current store.

Requires: `BUSINESS_OWNER`, `ADMIN`, `STAFF`, `SUPER_ADMIN`

**Response:**
```json
{
  "success": true,
  "data": {
    "totalOrders": 150,
    "revenue": 45230.50,
    "activeCustomers": 89,
    "pendingEscrow": 12
  }
}
```

---

### 5.14 Admin Dashboard

#### GET `/api/v1/admin/dashboard` or GET `/api/v1/admin/stats`
Store-level admin statistics.

Requires: `ADMIN`, `BUSINESS_OWNER`, `STAFF`

**Response:**
```json
{
  "success": true,
  "data": {
    "productCount": 45,
    "orderCount": 150,
    "pendingOrders": 12,
    "lowStockProducts": 3,
    "staffCount": 5,
    "unresolvedFraudFlags": 1,
    "revenue": 45230.50
  }
}
```

---

### 5.15 Super Admin

#### GET `/api/v1/super-admin/dashboard` or GET `/api/v1/super-admin/system-stats`
Platform-wide statistics.

Requires: `SUPER_ADMIN` only.

**Response:**
```json
{
  "success": true,
  "data": {
    "storeCount": 25,
    "userCount": 1200,
    "ownerCount": 25,
    "customerCount": 1150,
    "orderCount": 3400,
    "revenue": 892450.00
  }
}
```

---

### 5.16 Email Queue

#### POST `/api/v1/emails/enqueue`
Queue an email for sending. **(202)**

**Request:**
```json
{
  "recipient": "user@example.com",           // required, valid email
  "subject": "Order Confirmation",           // required
  "templateName": "order_confirmation",      // required
  "payloadJson": "{\"orderNumber\":\"ORD-123\"}"  // optional — template variables as JSON string
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "recipient": "user@example.com",
    "subject": "Order Confirmation",
    "templateName": "order_confirmation",
    "status": "PENDING",
    "retryCount": 0
  }
}
```

---

### 5.17 Health

#### GET `/api/v1/health`
Health check endpoint. **Public.**

**Response:**
```json
{
  "success": true,
  "data": {
    "status": "UP"
  }
}
```

---

## 6. Database Schema

17 tables with soft-delete support (`deleted` boolean column).

```
users
├── stores (owner_user_id → users.id)
│   ├── products (store_id → stores.id)
│   ├── customers (store_id → stores.id, user_id → users.id)
│   │   ├── carts (customer_id → customers.id, store_id → stores.id)
│   │   │   └── cart_items (cart_id → carts.id, product_id → products.id)
│   │   └── orders (customer_id → customers.id, store_id → stores.id)
│   │       ├── order_items (order_id → orders.id, product_id → products.id)
│   │       ├── payments (order_id → orders.id, store_id → stores.id)
│   │       └── escrow_transactions (order_id → orders.id, store_id → stores.id)
│   │           └── escrow_disputes (escrow_transaction_id → escrow_transactions.id)
│   ├── staff (store_id → stores.id, user_id → users.id)
│   ├── subscriptions (store_id → stores.id)
│   ├── billing_transactions (store_id → stores.id)
│   ├── fraud_flags (store_id → stores.id)
│   └── kpi_snapshots (store_id)
├── subscription_plans (standalone)
└── email_queue (standalone)
```

### Key Tables

| Table | Description |
|-------|-------------|
| `users` | All platform users (owners, staff, customers, admins) |
| `stores` | Tenant stores, each owned by a user |
| `products` | Store products with pricing, stock, SKU, images |
| `customers` | Customer profiles scoped to a store |
| `orders` | Customer orders with amounts, status, delivery info |
| `order_items` | Line items within an order |
| `carts` | Active shopping carts |
| `cart_items` | Items in a cart |
| `payments` | Payment records linked to orders |
| `billing_transactions` | Financial transaction records |
| `escrow_transactions` | Funds held in escrow until order completion |
| `escrow_disputes` | Disputes raised against escrow transactions |
| `staff` | Staff members assigned to stores |
| `subscriptions` | Store subscription records |
| `subscription_plans` | Available subscription plan definitions |
| `fraud_flags` | Automated fraud detection flags |
| `kpi_snapshots` | Periodic KPI snapshots for analytics |
| `email_queue` | Queued emails processed by background job |

---

## 7. Frontend Integration Guide

### Recommended User Flows

#### Business Owner Onboarding
```
1. POST /api/v1/auth/register     { role: "BUSINESS_OWNER", ... }
2. POST /api/v1/auth/login        → get tokens
3. POST /api/v1/stores            → create store, get storeId
4. POST /api/v1/products          → add products
5. GET  /api/v1/admin/dashboard   → view stats
```

#### Customer Shopping Flow
```
1. GET  /api/v1/public/stores/{slug}           → view store
2. GET  /api/v1/public/stores/{slug}/products  → browse products
3. POST /api/v1/customers/register             → sign up (with storeId)
4. POST /api/v1/auth/login                     → get tokens (with storeId)
5. POST /api/v1/cart/items                     → add to cart
6. GET  /api/v1/cart                           → view cart
7. POST /api/v1/checkout                       → place order
8. POST /api/v1/payments/initiate              → start payment
9. POST /api/v1/payments/confirm               → confirm payment
10. GET /api/v1/orders                         → view order history
```

#### Store Management
```
1. GET  /api/v1/stores                         → list my stores
2. GET  /api/v1/products                       → list products
3. GET  /api/v1/orders                         → list orders
4. PATCH /api/v1/orders/{id}/status            → update order status
5. GET  /api/v1/staff                          → list staff
6. POST /api/v1/staff                          → add staff
7. GET  /api/v1/subscriptions/current          → check subscription
8. GET  /api/v1/billing/transactions           → view billing
9. GET  /api/v1/dashboard/metrics              → KPIs
10. GET /api/v1/admin/dashboard                → admin stats
```

### Token Management (Frontend)

```javascript
// Store tokens after login
const { accessToken, refreshToken } = await login(email, password);
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);

// Axios interceptor for auto-refresh
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const { accessToken, refreshToken: newRefresh } = await refresh(
        localStorage.getItem('refreshToken')
      );
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', newRefresh);
      error.config.headers.Authorization = `Bearer ${accessToken}`;
      return axios(error.config);
    }
    return Promise.reject(error);
  }
);

// Set default auth header
axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
```

### CORS

The backend allows all origins by default. For production, set `CORS_ALLOWED_ORIGINS` to your frontend domain(s).

Allowed methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`

### Error Handling

All errors follow the same `ApiResponse` shape. Check `success === false` and read `error.fieldErrors` for validation errors:

```javascript
try {
  const res = await axios.post('/api/v1/auth/register', data);
  // res.data.success === true
  // res.data.data contains the result
} catch (err) {
  const { error } = err.response.data;
  // error.errorCode — e.g. "VALIDATION_ERROR"
  // error.fieldErrors — { "email": "must not be blank" }
  // error.statusCode — HTTP status code
}
```

### Background Jobs (No Frontend Action Needed)

These run automatically on the server:

| Job | Interval | What It Does |
|-----|----------|--------------|
| Email dispatch | Every 30s | Sends queued emails |
| Escrow release | Every 15 min | Releases held funds for completed orders |
| Subscription expiry | Every hour | Marks expired subscriptions |
| KPI aggregation | Every 30 min | Snapshots revenue, orders, customer metrics |
| Fraud scan | Every 10 min | Flags suspicious orders |
