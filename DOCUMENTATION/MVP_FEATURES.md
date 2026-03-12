# CartWave Backend — MVP Features

This document lists every feature delivered in the CartWave backend MVP, organized by domain.

---

## Platform Core

| Feature | Status | Details |
|---------|:------:|---------|
| Multi-tenant architecture | Done | Each store is an isolated tenant. JWT carries `storeId`/`tenantId`, enforced via `TenantContext` on every request. All queries scoped to tenant. |
| Role-based access control | Done | 5 user roles (`SUPER_ADMIN`, `ADMIN`, `BUSINESS_OWNER`, `STAFF`, `CUSTOMER`) with `@PreAuthorize` on every endpoint. |
| JWT authentication | Done | Stateless Bearer tokens. Access token (15 min) + refresh token (7 days). Claims include `userId`, `role`, `storeId`, `tenantId`, `permissions`. |
| Login brute-force protection | Done | 5 failed attempts → 15-minute lockout (in-memory tracking). |
| Self-service registration | Done | Public signup for `BUSINESS_OWNER` and `CUSTOMER` roles only. All other roles created by admins/owners. |
| Multi-store login | Done | Users with access to multiple stores must specify `storeId` at login. Single-store users auto-resolve. |
| Soft delete | Done | All entities use `deleted` boolean — no data is permanently removed. |
| Global error handling | Done | Standardized `ApiResponse` error format with field-level validation errors, error codes, and HTTP status mapping. |
| CORS configuration | Done | Configurable allowed origins via `CORS_ALLOWED_ORIGINS` env var. |
| Health check endpoint | Done | `GET /api/v1/health` — public, no auth required. |
| Seed data on startup | Done | Auto-creates 4 subscription plans + super admin account on first boot. |
| Database migrations | Done | Flyway-managed schema with `V1__init.sql` creating all 17 tables. |
| Local dev profile | Done | H2 in-memory database with `--spring.profiles.active=local` for zero-dependency development. |

---

## Store Management

| Feature | Status | Details |
|---------|:------:|---------|
| Create store | Done | Owners register → create store with unique slug. Auto-provisions `FREE` subscription (30-day). |
| Update store | Done | Update name, description, branding (logo, banner), business details, custom domain flag. |
| Delete store | Done | Soft-delete. Sets `active=false`. |
| List stores | Done | Role-aware: super admins see all, owners see their own, staff see their assigned store. |
| Get store by ID | Done | Tenant-scoped access control. |
| Public store profile | Done | `GET /api/v1/public/stores/{slug}` — no auth, lookup by URL-friendly slug. |
| Store branding fields | Done | `logoUrl`, `bannerUrl`, `websiteUrl`, `businessAddress`, `businessRegistrationNumber`, `businessPhoneNumber`, `businessEmail`. |
| Custom domain gate | Done | `customDomain` flag only settable if subscription plan supports it (PRO/ENTERPRISE). |

---

## Product Catalog

| Feature | Status | Details |
|---------|:------:|---------|
| Create product | Done | With plan-enforced limit (FREE=20, STARTER=100, PRO=1000, ENTERPRISE=unlimited). |
| Update product | Done | Partial update, tenant-scoped. |
| Delete product | Done | Soft-delete. |
| List products (admin) | Done | All products for the authenticated user's store. |
| Get product by ID | Done | Tenant-scoped. |
| Public product catalog | Done | `GET /api/v1/public/stores/{slug}/products` — returns only `ACTIVE` products. |
| Product statuses | Done | `ACTIVE`, `INACTIVE`, `ARCHIVED`, `OUT_OF_STOCK`. Auto-set to `OUT_OF_STOCK` when stock hits 0 during checkout. |
| Stock management | Done | `stock` and `lowStockThreshold` fields. Stock deducted on checkout. Dashboard shows low-stock count (≤5). |
| Product attributes | Done | `category`, `attributes` (JSON string), `sku`, `images` (comma-separated URLs). |
| Cost price tracking | Done | `costPrice` field for margin calculation. |

---

## Shopping Cart

| Feature | Status | Details |
|---------|:------:|---------|
| Auto-create cart | Done | Cart created automatically on first interaction if none exists. |
| Add item to cart | Done | Validates product is `ACTIVE` and not deleted. Increments quantity if item already in cart. |
| Update item quantity | Done | Sets absolute quantity. Recalculates totals. |
| Remove item from cart | Done | Soft-delete. Recalculates totals. |
| View cart | Done | Returns full cart with items, line totals, subtotal, total, currency. |
| Cart status lifecycle | Done | `ACTIVE` → `CHECKED_OUT` (on checkout) or `ABANDONED`. |
| Multi-currency support | Done | Cart currency defaults to store currency (or `USD`). |

---

## Checkout

| Feature | Status | Details |
|---------|:------:|---------|
| Cart-to-order conversion | Done | Single `POST /api/v1/checkout` converts active cart into order + billing transaction. |
| Stock validation | Done | Validates sufficient stock for every item before creating order. Throws `INSUFFICIENT_STOCK` if not met. |
| Stock deduction | Done | Atomically deducts stock for each item. Auto-marks product `OUT_OF_STOCK` if depleted. |
| Order number generation | Done | Format: `CW-{epoch}-{uuid6}` (e.g., `CW-1709856000-a1b2c3`). |
| Transaction ID generation | Done | Format: `txn_{uuid16}`. |
| Cart cleanup | Done | Soft-deletes cart items, sets cart status to `CHECKED_OUT`. |
| Empty cart guard | Done | Throws `EMPTY_CART` if no items in cart. |

---

## Order Management

| Feature | Status | Details |
|---------|:------:|---------|
| List orders | Done | Customers see only their own orders. Business roles see all store orders. |
| Get order by ID | Done | Access-controlled: customers can only view their own orders. |
| Create order (admin) | Done | Manual order creation by staff/owners. |
| Update order | Done | Update delivery/contact details. Customers cannot update (throws `ORDER_UPDATE_FORBIDDEN`). |
| Update order status | Done | `PENDING` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED` (also `CANCELLED`, `REFUNDED`). |
| Delivery completion | Done | When status → `DELIVERED`: sets `completedAt`, calculates `releaseAt` (+48h), triggers escrow release. |
| Order financial fields | Done | `totalAmount`, `shippingCost`, `taxAmount`, `discountAmount`. |

---

## Payments

| Feature | Status | Details |
|---------|:------:|---------|
| Initiate payment | Done | `POST /api/v1/payments/initiate` — sets billing to `PROCESSING`, order payment to `PROCESSING`. |
| Confirm payment | Done | `POST /api/v1/payments/confirm` — on success: billing → `HOLD`, creates escrow hold, order → `COMPLETED`. On failure: billing → `FAILED`. |
| Webhook endpoint | Done | `POST /api/v1/payments/webhook` — accepts external payment provider callbacks. Delegates to confirm flow. |
| Payment providers | Done | `paymentProvider` and `paymentMethod` fields (ready for Stripe, Paystack, etc. — internal stub for now). |
| Plan-gated payments | Done | Billing transactions only accessible if subscription plan has `paymentsEnabled=true`. FREE plan blocked. |

---

## Escrow System

| Feature | Status | Details |
|---------|:------:|---------|
| Escrow hold | Done | Created on successful payment confirmation. Holds funds with `releaseAt` = payment time + 48 hours. |
| Escrow release (manual) | Done | Triggered immediately when order status set to `DELIVERED`. |
| Escrow release (scheduled) | Done | Background job runs daily at 2 AM, releases all `HELD` escrows past their `releaseAt` date. |
| Escrow dispute | Done | Any user can raise a dispute. Sets escrow → `DISPUTED`, creates `EscrowDispute` record with `OPEN` status. |
| Dispute lifecycle | Done | `OPEN` → `UNDER_REVIEW` → `RESOLVED` / `REJECTED`. |

---

## Subscriptions & Plans

| Feature | Status | Details |
|---------|:------:|---------|
| 4 subscription plans | Done | FREE ($0, 20 products, 1 staff), STARTER ($19, 100/3), PRO ($99, 1000/10, custom domain), ENTERPRISE ($499, unlimited). |
| Plan enforcement | Done | Product and staff limits enforced on creation. Feature gates for payments and custom domains. |
| Change plan | Done | Upgrade/downgrade via `POST /api/v1/subscriptions/change`. |
| Auto-renewal | Done | `autoRenewal` flag, configurable billing cycle (`MONTHLY`/`YEARLY`). |
| Subscription expiry | Done | Hourly job expires active subscriptions past `endDate`. |
| View current plan | Done | `GET /api/v1/subscriptions/current`. |
| List available plans | Done | `GET /api/v1/subscriptions/plans`. |

---

## Staff Management

| Feature | Status | Details |
|---------|:------:|---------|
| Add staff | Done | Plan limit enforced. Assigns user to store with `StaffRole`. Auto-promotes `CUSTOMER` → `STAFF`. |
| Remove staff | Done | Soft-delete, sets status `TERMINATED`. Auto-demotes back to `CUSTOMER` if no other staff memberships. |
| List staff | Done | Tenant-scoped. |
| Staff roles | Done | `MANAGER`, `ADMIN`, `INVENTORY`, `SUPPORT`, `MARKETING`, `FINANCIAL`. |
| Staff statuses | Done | `ACTIVE`, `INACTIVE`, `ON_LEAVE`, `TERMINATED`. |

---

## Customer Profiles

| Feature | Status | Details |
|---------|:------:|---------|
| Self-registration | Done | `POST /api/v1/customers/register` — public, requires `storeId`. |
| Auto-create on login | Done | Customer profile auto-created if user logs in as customer to a new store. |
| View own profile | Done | `GET /api/v1/customers/me`. |
| View any customer | Done | Business roles can view any customer in their store. |

---

## Dashboards & Analytics

| Feature | Status | Details |
|---------|:------:|---------|
| Store dashboard metrics | Done | `totalOrders`, `revenue`, `activeCustomers`, `pendingEscrow`. |
| Admin dashboard | Done | `productCount`, `orderCount`, `pendingOrders`, `lowStockProducts`, `staffCount`, `unresolvedFraudFlags`, `revenue`. |
| Super admin dashboard | Done | Platform-wide: `storeCount`, `userCount`, `ownerCount`, `customerCount`, `orderCount`, `revenue`. |
| KPI snapshots | Done | Every 30 min: per-store daily snapshots of revenue, order count, customer count. |

---

## Fraud Detection

| Feature | Status | Details |
|---------|:------:|---------|
| Automated scanning | Done | Every 10 min across all stores. |
| High-value order flag | Done | Orders ≥ $10,000 → `MEDIUM` severity flag. |
| Failed payment flag | Done | Failed payment status → `HIGH` severity flag. |
| Deduplication | Done | Same order+reason not flagged twice. |
| Dashboard count | Done | `unresolvedFraudFlags` shown in admin dashboard. |

---

## Email System

| Feature | Status | Details |
|---------|:------:|---------|
| Email queue | Done | `POST /api/v1/emails/enqueue` — template-based emails with JSON payload. |
| Background dispatch | Done | Every 30 seconds, processes up to 50 pending emails. |
| Retry policy | Done | Up to 3 retries on failure, then marked `FAILED`. |
| SMTP integration | Done | Configurable via `SMTP_HOST`/`SMTP_PORT` env vars. |

---

## API Summary

| Category | Endpoints |
|----------|:---------:|
| Auth | 8 |
| Stores | 10 |
| Public Catalog | 3 |
| Products | 16 |
| Cart | 5 |
| Checkout | 1 |
| Orders | 9 |
| Payments | 5 |
| Customers | 3 |
| Staff | 3 |
| Subscriptions | 7 |
| Billing | 1 |
| Marketing (Coupons) | 4 |
| Wishlist | 3 |
| Dashboard | 1 |
| Admin | 10 |
| Super Admin | 14 |
| Email | 1 |
| Health | 1 |
| **Total** | **112** |

---

## V3 Enterprise Features

| Feature | Status | Details |
|---------|:------:|---------|
| Password reset flow | Done | `POST /auth/forgot-password` sends reset email → `POST /auth/reset-password` with token. Token stored as `password_reset_token` with TTL (`password_reset_expires_at`). |
| Email verification | Done | `POST /auth/verify-email` with token. `POST /auth/resend-verification` to resend. Token stored as `email_verification_token`. |
| Refresh token rotation | Done | SHA-256 hashed tokens in `refresh_tokens` table. Old token revoked on refresh. `POST /auth/logout` revokes immediately. |
| Product variants | Done | `ProductVariant` entity — name, SKU (unique), price, stock, imageUrl. Full CRUD under `/products/{id}/variants`. |
| Product reviews | Done | `Review` entity — rating (1–5), comment, verified-purchase flag. Public read, customer write, admin delete. Unique per customer+product. |
| Product search | Done | `GET /products/search` with filters: `q`, `category`, `minPrice`, `maxPrice`, `inStock`, `publishedOnly`. Public search via `/public/stores/{storeId}/products/search`. |
| Wishlist | Done | `Wishlist` entity — per-customer saved items. `GET/POST/DELETE /wishlist/{productId}`. Requires CUSTOMER role. |
| Order tracking timeline | Done | `OrderTracking` entity — records each status change with note and updatedBy. `GET /orders/{orderId}/tracking`. |
| Paystack integration | Done | `PaystackService` — `initializeTransaction()` → Paystack API, returns `authorization_url`. Webhook at `/payments/paystack/webhook` with HMAC-SHA512 signature verification. |
| Payment refunds | Done | `POST /payments/refund` — process refund. Requires BUSINESS_OWNER/ADMIN/SUPER_ADMIN. |
| Rate limiting | Done | Bucket4j per-IP: login 10/min, forgot-password 5/10min, default 200/min. |
| OWASP security headers | Done | HSTS, CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy, Permissions-Policy. Path-aware CSP relaxation for Swagger UI. |
| Session timeout | Done | Redis-based 30-min inactivity tracking. Graceful fallback when Redis unavailable. |
| Redis caching | Done | `@Cacheable` on public store, products, plans, dashboard. `ConcurrentMapCache` fallback if no Redis. |
| Super Admin module | Done | 14 endpoints — platform dashboard, system stats, health, revenue, user management, admin CRUD, store listing, plan management. |
| Email templates | Done | 15 Thymeleaf templates: welcome, email_verification, password_reset, order_confirmed/shipped/delivered, payment_successful/failed, subscription_created/renewed/expiring, store_created, escrow_released, dispute_opened/resolved. |
| Pagination | Done | All list endpoints paginated with `?page=0&size=20`, returns Spring `Page<T>`. |

---

## Database: 24 Tables

`users`, `stores`, `products`, `product_variants`, `customers`, `staff`, `orders`, `order_items`, `order_tracking`, `carts`, `cart_items`, `payments`, `billing_transactions`, `escrow_transactions`, `escrow_disputes`, `subscriptions`, `subscription_plans`, `coupons`, `reviews`, `wishlists`, `refresh_tokens`, `fraud_flags`, `kpi_snapshots`, `email_queue`

---

## Background Jobs: 5

| Job | Schedule |
|-----|----------|
| Email dispatch | Every 30 seconds |
| Escrow release | Daily at 2 AM |
| Subscription expiry | Hourly |
| KPI aggregation | Every 30 minutes |
| Fraud scan | Every 10 minutes |
