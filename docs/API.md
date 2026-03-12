# CartWave API Reference

> **Total endpoints: 112** across 20 controllers

Base URL: `http://localhost:8080/api/v1`

Interactive docs: `/swagger-ui/index.html`

## Authentication

All protected endpoints require a Bearer token in the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```

### Token Lifetimes

| Token | Default TTL |
|-------|-------------|
| Access token | 15 minutes |
| Refresh token | 7 days |

### Refresh Token Rotation
- On login, an opaque refresh token is issued; its SHA-256 hash is stored server-side
- On refresh, the old token is revoked and a new pair is issued (rotation)
- `POST /auth/logout` revokes the token immediately

---

## Auth (8 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register a new user (BUSINESS_OWNER or CUSTOMER) |
| POST | `/auth/login` | Public | Login, returns access + refresh tokens |
| POST | `/auth/refresh` | Public | Refresh token pair (rotates refresh token) |
| POST | `/auth/forgot-password` | Public | Send password reset email |
| POST | `/auth/reset-password` | Public | Reset password with token |
| POST | `/auth/verify-email` | Public | Verify email address with token |
| POST | `/auth/resend-verification` | Public | Resend verification email (query: `email`) |
| POST | `/auth/logout` | Public | Revoke refresh token |

---

## Stores (10 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/stores` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN, STAFF | List accessible stores |
| POST | `/stores` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Create a store |
| GET | `/stores/{storeId}` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN, STAFF | Get store by ID |
| PUT | `/stores/{storeId}` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Update store details |
| DELETE | `/stores/{storeId}` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Soft-delete store |
| PUT | `/stores/{id}/branding` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Update logo, banner, brand color, template |
| PUT | `/stores/{id}/domain` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Set custom domain (PRO+) |
| PUT | `/stores/{id}/seo` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Update meta title, description, keywords |
| GET | `/stores/{id}/public` | **Public** | Get public storefront view |
| GET | `/stores/{storeId}/products` | **Public** | List published products for store |

---

## Public Catalog (3 endpoints)

No authentication required. Powers the customer-facing storefront.

| Method | Path | Description |
|---|---|---|
| GET | `/public/stores/{slug}` | Get store by URL slug |
| GET | `/public/stores/{slug}/products` | List products by store slug |
| GET | `/public/stores/{storeId}/products/search` | Search products with filters |

**Search query parameters:** `q`, `category`, `minPrice`, `maxPrice`, `inStock`, `page` (default 0), `size` (default 20)

---

## Products (16 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/products` | BUSINESS_OWNER, ADMIN, STAFF | Create product (enforces subscription limit) |
| GET | `/products` | BUSINESS_OWNER, ADMIN, STAFF | List products (paginated: `page`, `size`) |
| GET | `/products/{id}` | BUSINESS_OWNER, ADMIN, STAFF | Get product by ID |
| PUT | `/products/{id}` | BUSINESS_OWNER, ADMIN, STAFF | Update product |
| DELETE | `/products/{id}` | BUSINESS_OWNER, ADMIN, STAFF | Soft-delete product |
| POST | `/products/{id}/images` | BUSINESS_OWNER, ADMIN, STAFF | Upload images to S3 (multipart) |
| DELETE | `/products/{id}/images/{imageUrl}` | BUSINESS_OWNER, ADMIN, STAFF | Remove image from S3 |
| PUT | `/products/{id}/publish` | BUSINESS_OWNER, ADMIN, STAFF | Toggle publish status |
| GET | `/products/search` | BUSINESS_OWNER, ADMIN, STAFF, SUPER_ADMIN | Search with filters |
| GET | `/products/{id}/variants` | BUSINESS_OWNER, ADMIN, STAFF | List variants |
| POST | `/products/{id}/variants` | BUSINESS_OWNER, ADMIN, STAFF | Add variant |
| PUT | `/products/{id}/variants/{variantId}` | BUSINESS_OWNER, ADMIN, STAFF | Update variant |
| DELETE | `/products/{id}/variants/{variantId}` | BUSINESS_OWNER, ADMIN, STAFF | Delete variant |
| GET | `/products/{id}/reviews` | **Public** | List reviews (paginated) |
| POST | `/products/{id}/reviews` | CUSTOMER | Submit review (auto verified-purchase) |
| DELETE | `/products/{id}/reviews/{reviewId}` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Delete review |

**Search query parameters:** `q`, `category`, `minPrice`, `maxPrice`, `inStock`, `publishedOnly`, `page`, `size`

---

## Cart (5 endpoints)

All cart endpoints require `CUSTOMER` role.

| Method | Path | Description |
|---|---|---|
| GET | `/cart` | Get current cart |
| GET | `/cart/items` | Get cart items |
| POST | `/cart/items` | Add item to cart |
| PATCH | `/cart/items/{itemId}` | Update cart item quantity |
| DELETE | `/cart/items/{itemId}` | Remove item from cart |

---

## Checkout (1 endpoint)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/checkout` | CUSTOMER | Convert cart → order + billing transaction |

---

## Orders (9 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/orders` | CUSTOMER, BUSINESS_OWNER, ADMIN, STAFF, SUPER_ADMIN | List orders (paginated) |
| GET | `/orders/{orderId}` | CUSTOMER, BUSINESS_OWNER, ADMIN, STAFF, SUPER_ADMIN | Get order by ID |
| POST | `/orders` | BUSINESS_OWNER, ADMIN, STAFF | Create order (manual) |
| PUT | `/orders/{orderId}` | BUSINESS_OWNER, ADMIN, STAFF | Update order details |
| PATCH | `/orders/{orderId}/status` | BUSINESS_OWNER, ADMIN, STAFF | Update order status |
| PUT | `/orders/{orderId}/status` | BUSINESS_OWNER, ADMIN, STAFF | Update order status (alias) |
| GET | `/orders/store/{storeId}` | BUSINESS_OWNER, ADMIN, STAFF, SUPER_ADMIN | List orders for store |
| GET | `/orders/customer/{customerId}` | CUSTOMER, BUSINESS_OWNER, ADMIN, STAFF, SUPER_ADMIN | List orders for customer |
| GET | `/orders/{orderId}/tracking` | CUSTOMER, BUSINESS_OWNER, ADMIN, STAFF, SUPER_ADMIN | Order tracking timeline |

---

## Payments (5 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/payments/initiate` | CUSTOMER | Initiate payment |
| POST | `/payments/confirm` | CUSTOMER, BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Confirm payment |
| POST | `/payments/webhook` | **Public** | Generic payment webhook |
| POST | `/payments/paystack/webhook` | **Public** | Paystack webhook (HMAC-SHA512 verified) |
| POST | `/payments/refund` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Process refund |

---

## Escrow (4 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/escrow/store/{storeId}` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | List escrow transactions |
| POST | `/escrow/{escrowId}/release` | ADMIN, SUPER_ADMIN | Manually release escrow hold |
| POST | `/escrow/{escrowId}/dispute` | Authenticated | Raise a dispute |
| PUT | `/escrow/dispute/{disputeId}/resolve` | ADMIN, SUPER_ADMIN | Resolve dispute |

---

## Marketing — Coupons (4 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/marketing/stores/{storeId}/coupons` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Create coupon |
| GET | `/marketing/stores/{storeId}/coupons` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | List coupons |
| DELETE | `/marketing/stores/{storeId}/coupons/{couponId}` | BUSINESS_OWNER, ADMIN, SUPER_ADMIN | Delete coupon |
| POST | `/marketing/coupons/validate` | **Public** | Validate coupon code |

---

## Subscriptions (7 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/subscriptions/plans` | **Public** | List all active plans |
| GET | `/subscriptions/current` | BUSINESS_OWNER, ADMIN, STAFF | Get current subscription |
| GET | `/subscriptions` | BUSINESS_OWNER, ADMIN, STAFF | Get subscription (alias) |
| POST | `/subscriptions/subscribe` | BUSINESS_OWNER, ADMIN | Subscribe/change plan |
| POST | `/subscriptions/cancel` | BUSINESS_OWNER, ADMIN | Cancel subscription |
| POST | `/subscriptions/change` | BUSINESS_OWNER, ADMIN | Change plan (legacy) |
| POST | `/subscriptions` | BUSINESS_OWNER, ADMIN | Create/update subscription (legacy) |

---

## Billing (1 endpoint)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/billing/transactions` | BUSINESS_OWNER, ADMIN, STAFF | Billing transactions (paginated) |

---

## Customers (3 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/customers/me` | CUSTOMER | Get own profile |
| GET | `/customers/{customerId}` | BUSINESS_OWNER, ADMIN, STAFF, SUPER_ADMIN, CUSTOMER | Get customer by ID |
| POST | `/customers/register` | **Public** | Register as customer |

---

## Wishlist (3 endpoints)

All wishlist endpoints require `CUSTOMER` role.

| Method | Path | Description |
|---|---|---|
| GET | `/wishlist` | Get customer wishlist |
| POST | `/wishlist/{productId}` | Add product to wishlist |
| DELETE | `/wishlist/{productId}` | Remove from wishlist |

---

## Staff (3 endpoints)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/staff` | BUSINESS_OWNER, ADMIN, STAFF | List staff |
| POST | `/staff` | BUSINESS_OWNER, ADMIN | Add staff member |
| DELETE | `/staff/{staffId}` | BUSINESS_OWNER, ADMIN | Deactivate staff |

---

## Dashboard (1 endpoint)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/dashboard/metrics` | BUSINESS_OWNER, ADMIN, STAFF, SUPER_ADMIN | Store dashboard metrics |

---

## Admin (10 endpoints)

All admin endpoints require `ADMIN` or `SUPER_ADMIN` role.

| Method | Path | Description |
|---|---|---|
| GET | `/admin/dashboard` | Admin dashboard |
| GET | `/admin/stats` | Platform stats (alias) |
| GET | `/admin/users` | List all users |
| GET | `/admin/users/{userId}` | Get user by ID |
| PUT | `/admin/users/{userId}/suspend` | Suspend a user |
| PUT | `/admin/users/{userId}/activate` | Activate a user |
| GET | `/admin/revenue` | Revenue summary |
| GET | `/admin/health` | Platform health indicators |
| POST | `/admin/plans` | Create subscription plan |
| DELETE | `/admin/plans/{planId}/deactivate` | Deactivate plan |

---

## Super Admin (14 endpoints)

All super admin endpoints require `SUPER_ADMIN` role.

| Method | Path | Description |
|---|---|---|
| GET | `/super-admin/dashboard` | Platform-wide dashboard |
| GET | `/super-admin/system-stats` | System statistics |
| GET | `/super-admin/health` | Platform health indicators |
| GET | `/super-admin/revenue` | Total/monthly revenue |
| GET | `/super-admin/users` | List all users |
| GET | `/super-admin/users/{userId}` | Get user by ID |
| PUT | `/super-admin/users/{userId}/suspend` | Suspend user |
| PUT | `/super-admin/users/{userId}/activate` | Activate user |
| POST | `/super-admin/admins` | Create admin account |
| GET | `/super-admin/admins` | List admins |
| DELETE | `/super-admin/admins/{adminId}` | Remove admin |
| GET | `/super-admin/stores` | List all stores |
| POST | `/super-admin/plans` | Create subscription plan |
| DELETE | `/super-admin/plans/{planId}/deactivate` | Deactivate plan |

---

## Email Queue (1 endpoint)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/emails/enqueue` | **Public** | Queue email for sending (202 Accepted) |

---

## Health (1 endpoint)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/health` | **Public** | Health check |

---

## Subscription Plans

| Plan | Price (NGN/month) | Products | Custom Domain |
|---|---|---|---|
| STARTER | Free | 5 | No |
| BASIC | ₦5,000 | 10 | No |
| GROWTH | ₦15,000 | 20 | No |
| PRO | ₦30,000 | 100 | Yes |
| ENTERPRISE | Custom | Unlimited | Yes |

---

## Scheduled Background Jobs

| Job | Schedule | Purpose |
|---|---|---|
| EmailQueueProcessor | Every 30s | Dispatch pending emails (up to 50 per batch, 3 retries) |
| EscrowReleaseJob | Daily 02:00 | Release held escrows past `releaseAt` |
| SubscriptionExpirationJob | Hourly | Mark expired subscriptions |
| KpiAggregationJob | Every 30 min | Per-store daily KPI snapshots |
| FraudDetectionJob | Every 10 min | Flag suspicious orders |

---

## Security Features

- **Rate Limiting:** Bucket4j per-IP (login: 10/min, forgot-password: 5/10min, default: 200/min)
- **OWASP Headers:** HSTS, CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy
- **Session Timeout:** Redis-based 30-min inactivity tracking
- **Login Lockout:** 5 failed attempts → 15-minute lockout
- **CORS:** Configurable via `CORS_ALLOWED_ORIGINS` env var

---

## Pagination Convention

All paginated endpoints accept `?page=0&size=20` and return a Spring `Page<T>` envelope:
```json
{
  "content": [...],
  "totalElements": 143,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

---

## Error Responses

All errors follow the `ApiResponse` envelope:
```json
{
  "success": false,
  "message": "Human-readable error",
  "error": {
    "errorCode": "MACHINE_READABLE_CODE",
    "errorMessage": "...",
    "statusCode": 400,
    "timestamp": "2026-03-08T10:00:00Z",
    "path": "/api/v1/...",
    "fieldErrors": {}
  }
}
```

Common error codes: `UNAUTHORIZED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VALIDATION_ERROR`, `BUSINESS_RULE_VIOLATION`, `PRODUCT_LIMIT_REACHED`, `COUPON_CODE_EXISTS`, `ESCROW_ALREADY_RELEASED`, `INSUFFICIENT_STOCK`, `EMPTY_CART`, `ORDER_UPDATE_FORBIDDEN`.
