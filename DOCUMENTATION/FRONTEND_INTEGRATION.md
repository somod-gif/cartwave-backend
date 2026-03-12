# CartWave Backend — Frontend Integration Guide

**Version:** V2  
**Base URL:** `http://localhost:8080` (dev) | `https://your-domain.com` (prod)  
**Swagger UI:** `/swagger-ui/index.html`  
**API Docs (JSON):** `/api-docs`

---

## Is the Backend Ready for Frontend Integration?

**Yes — with full clarity on what is implemented.**

The backend is a production-grade, multi-tenant SaaS ecommerce platform. Every feature below has working endpoints, security, and database persistence. The only thing not wired is **real email templating** (emails queue and dispatch but use plain text — HTML templates are a frontend/backend task to finish together) and **real AWS S3 credentials** (image upload endpoints exist but need your S3 keys in `.env`).

---

## Authentication Model

CartWave uses **JWT Bearer tokens**.

Every protected request must include:
```
Authorization: Bearer <accessToken>
```

Tokens carry the user's `role` and an optional `storeId` (tenant context). When a user belongs to a specific store (staff, customer), the `storeId` embedded in their token determines which store's data they see.

### Token Lifetimes
| Token | Default Lifetime |
|---|---|
| Access Token | 15 minutes |
| Refresh Token | 7 days |

---

## Roles Overview

CartWave has **5 roles**. Three are platform-internal; two are store-facing.

| Role | Who They Are | Scope |
|---|---|---|
| `SUPER_ADMIN` | CartWave founder / CTO | Entire platform |
| `ADMIN` | CartWave internal team member | Entire platform |
| `BUSINESS_OWNER` | Merchant who owns a store | Their own store(s) |
| `STAFF` | Employee of a store | Their assigned store |
| `CUSTOMER` | Shopper buying from a store | Their store + their own data |

> **Registration rules:** Public registration (`POST /api/v1/auth/register`) only allows `CUSTOMER` and `BUSINESS_OWNER`. `ADMIN` accounts are created exclusively by `SUPER_ADMIN`. `SUPER_ADMIN` is seeded at startup.

---

## Role-by-Role Capability Breakdown

---

### SUPER_ADMIN — Full Platform God Mode

The single highest-authority account. Seeded on first startup from `.env`.

**Default credentials (change before production):**
- Email: `superadmin@cartwave.local`
- Password: `Password123!`

**How to log in:**
```
POST /api/v1/auth/login
{ "email": "superadmin@cartwave.local", "password": "Password123!" }
```

#### What SUPER_ADMIN Can Do

| Feature | Endpoint |
|---|---|
| Platform dashboard overview | `GET /api/v1/super-admin/dashboard` |
| Platform system statistics | `GET /api/v1/super-admin/system-stats` |
| Platform health check (disputes, emails, subscriptions) | `GET /api/v1/super-admin/health` |
| Revenue summary (total + last 30 days) | `GET /api/v1/super-admin/revenue` |
| List every user on the platform | `GET /api/v1/super-admin/users` |
| Get a specific user | `GET /api/v1/super-admin/users/{userId}` |
| Suspend any user | `PUT /api/v1/super-admin/users/{userId}/suspend` |
| Re-activate any user | `PUT /api/v1/super-admin/users/{userId}/activate` |
| **Create an internal ADMIN account** | `POST /api/v1/super-admin/admins` |
| List all internal ADMINs | `GET /api/v1/super-admin/admins` |
| Remove an internal ADMIN | `DELETE /api/v1/super-admin/admins/{adminId}` |
| List all stores on the platform | `GET /api/v1/super-admin/stores` |
| Create a subscription plan | `POST /api/v1/super-admin/plans` |
| Deactivate a subscription plan | `DELETE /api/v1/super-admin/plans/{planId}/deactivate` |
| Manually release an escrow hold | `POST /api/v1/escrow/{escrowId}/release` |
| Resolve an escrow dispute | `PUT /api/v1/escrow/dispute/{disputeId}/resolve` |
| Admin dashboard view | `GET /api/v1/admin/dashboard` |
| All ADMIN-level endpoints (listed below) | ✓ |

---

### ADMIN — CartWave Internal Team Member

Created by `SUPER_ADMIN` only. CartWave staff who support merchants and monitor the platform.

**How to create:**
```
POST /api/v1/super-admin/admins
Authorization: Bearer <super_admin_token>
{
  "email": "alice@cartwave.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "password": "SecurePass123!"
}
```

#### What ADMIN Can Do

| Feature | Endpoint |
|---|---|
| Admin dashboard with platform stats | `GET /api/v1/admin/dashboard` |
| Platform statistics alias | `GET /api/v1/admin/stats` |
| List all platform users | `GET /api/v1/admin/users` |
| Get user by ID | `GET /api/v1/admin/users/{userId}` |
| Suspend a user account | `PUT /api/v1/admin/users/{userId}/suspend` |
| Activate a user account | `PUT /api/v1/admin/users/{userId}/activate` |
| Revenue summary | `GET /api/v1/admin/revenue` |
| Platform health indicators | `GET /api/v1/admin/health` |
| Create subscription plan | `POST /api/v1/admin/plans` |
| Deactivate subscription plan | `DELETE /api/v1/admin/plans/{planId}/deactivate` |
| List all stores | `GET /api/v1/stores` |
| Create/update/delete any store | `POST/PUT/DELETE /api/v1/stores/...` |
| List/create/manage any store's products | `GET/POST/PUT/DELETE /api/v1/products/...` |
| View all orders | `GET /api/v1/orders` |
| Manage order status | `PATCH /api/v1/orders/{orderId}/status` |
| View escrow transactions for any store | `GET /api/v1/escrow/store/{storeId}` |
| Manually release escrow | `POST /api/v1/escrow/{escrowId}/release` |
| Resolve escrow disputes | `PUT /api/v1/escrow/dispute/{disputeId}/resolve` |
| Manage store coupons | `POST/GET/DELETE /api/v1/marketing/stores/{storeId}/coupons` |
| View store subscription | `GET /api/v1/subscriptions/current` |
| Change store subscription plan | `POST /api/v1/subscriptions/subscribe` |
| View billing transactions | `GET /api/v1/billing/transactions` |
| Store analytics/metrics | `GET /api/v1/dashboard/metrics` |
| Manage staff for any store | `POST/DELETE /api/v1/staff` |

---

### BUSINESS_OWNER — Merchant

Self-registers via the public API. Owns one or more stores. Has full control over their own store.

**How to register:**
```
POST /api/v1/auth/register
{
  "email": "merchant@example.com",
  "password": "MyPass123",
  "role": "BUSINESS_OWNER",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Login (multiple stores — must specify storeId):**
```
POST /api/v1/auth/login
{
  "email": "merchant@example.com",
  "password": "MyPass123",
  "storeId": "uuid-of-store"   ← required if they own more than one store
}
```

#### What BUSINESS_OWNER Can Do

| Feature | Endpoint |
|---|---|
| List their stores | `GET /api/v1/stores` |
| Create a new store | `POST /api/v1/stores` |
| Get store details | `GET /api/v1/stores/{storeId}` |
| Update store details | `PUT /api/v1/stores/{storeId}` |
| Delete a store | `DELETE /api/v1/stores/{storeId}` |
| Update store branding (logo, banner, colour, template) | `PUT /api/v1/stores/{id}/branding` |
| Set a custom domain | `PUT /api/v1/stores/{id}/domain` |
| Update SEO metadata | `PUT /api/v1/stores/{id}/seo` |
| Create products | `POST /api/v1/products` |
| List products | `GET /api/v1/products` |
| Update products | `PUT /api/v1/products/{id}` |
| Delete products | `DELETE /api/v1/products/{id}` |
| Upload product images to S3 | `POST /api/v1/products/{id}/images` |
| Remove a product image | `DELETE /api/v1/products/{id}/images/{imageUrl}` |
| Publish / unpublish a product | `PUT /api/v1/products/{id}/publish` |
| View store orders | `GET /api/v1/orders` |
| View order by ID | `GET /api/v1/orders/{orderId}` |
| Update order status | `PATCH /api/v1/orders/{orderId}/status` |
| Create coupons | `POST /api/v1/marketing/stores/{storeId}/coupons` |
| List coupons | `GET /api/v1/marketing/stores/{storeId}/coupons` |
| Delete coupons | `DELETE /api/v1/marketing/stores/{storeId}/coupons/{couponId}` |
| View escrow transactions | `GET /api/v1/escrow/store/{storeId}` |
| View store subscription | `GET /api/v1/subscriptions/current` |
| Subscribe / change plan | `POST /api/v1/subscriptions/subscribe` |
| Cancel subscription | `POST /api/v1/subscriptions/cancel` |
| View billing transactions | `GET /api/v1/billing/transactions` |
| View store analytics/metrics | `GET /api/v1/dashboard/metrics` |
| Add staff to store | `POST /api/v1/staff` |
| Remove staff from store | `DELETE /api/v1/staff/{staffId}` |
| List staff | `GET /api/v1/staff` |

---

### STAFF — Store Employee

Invited by `BUSINESS_OWNER` or `ADMIN`. Has operational access to the store but cannot manage subscriptions, billing, or add other staff.

**How staff is invited:**
```
POST /api/v1/staff
Authorization: Bearer <owner_token>
{ "userId": "existing-user-uuid", "role": "MANAGER" }
```
The user must already have a CartWave account. Staff roles: `MANAGER`, `CASHIER`, `VIEWER`.

#### What STAFF Can Do

| Feature | Endpoint |
|---|---|
| List their store | `GET /api/v1/stores` |
| View store details | `GET /api/v1/stores/{storeId}` |
| View, add, edit, delete products | `GET/POST/PUT/DELETE /api/v1/products` |
| Upload/remove product images | `POST/DELETE /api/v1/products/{id}/images` |
| Toggle product publish status | `PUT /api/v1/products/{id}/publish` |
| View orders | `GET /api/v1/orders` |
| Create orders | `POST /api/v1/orders` |
| Update orders | `PUT /api/v1/orders/{orderId}` |
| Update order status | `PATCH /api/v1/orders/{orderId}/status` |
| View billing transactions | `GET /api/v1/billing/transactions` |
| View store analytics | `GET /api/v1/dashboard/metrics` |
| View team (other staff) | `GET /api/v1/staff` |

**Staff CANNOT:** manage subscriptions, add/remove staff, create coupons, access escrow, or touch store settings.

---

### CUSTOMER — Shopper

Registers per-store (the `storeId` in registration ties them to that merchant's shop). Shoppers shop — they cart, checkout, and pay.

**How to register as a customer:**
```
POST /api/v1/customers/register
{
  "email": "shopper@example.com",
  "password": "pass123",
  "role": "CUSTOMER",
  "firstName": "Jane",
  "storeId": "uuid-of-the-store"   ← required
}
```

**Login must include storeId:**
```
POST /api/v1/auth/login
{
  "email": "shopper@example.com",
  "password": "pass123",
  "storeId": "uuid-of-the-store"
}
```

#### What CUSTOMER Can Do

| Feature | Endpoint |
|---|---|
| View their profile | `GET /api/v1/customers/me` |
| Get cart | `GET /api/v1/cart` |
| Add item to cart | `POST /api/v1/cart/items` |
| Update cart item | `PATCH /api/v1/cart/items/{itemId}` |
| Remove cart item | `DELETE /api/v1/cart/items/{itemId}` |
| Checkout (creates order + payment) | `POST /api/v1/checkout` |
| Initiate a payment | `POST /api/v1/payments/initiate` |
| View their orders | `GET /api/v1/orders` |
| View a specific order | `GET /api/v1/orders/{orderId}` |
| View their orders by customer ID | `GET /api/v1/orders/customer/{customerId}` |
| Raise a dispute on an escrow transaction | `POST /api/v1/escrow/{escrowId}/dispute` |

---

## Public Endpoints (No Authentication Required)

These are safe to call without a token — used for storefronts.

| Endpoint | Purpose |
|---|---|
| `POST /api/v1/auth/login` | Log in any user |
| `POST /api/v1/auth/register` | Register CUSTOMER or BUSINESS_OWNER |
| `POST /api/v1/auth/refresh` | Exchange refresh token for new access token |
| `POST /api/v1/customers/register` | Customer-specific registration with storeId |
| `GET /api/v1/stores/{id}/public` | Public storefront data (name, branding, SEO) |
| `GET /api/v1/stores/{storeId}/products` | Published products for a storefront |
| `GET /api/v1/subscriptions/plans` | List all available subscription plans |
| `POST /api/v1/marketing/coupons/validate` | Validate a coupon code at checkout |
| `GET /api/v1/health` | Server health check |

---

## Core Features In Detail

### Multi-Tenancy
Every store is a separate tenant. The JWT token carries a `storeId` — all data queries are automatically scoped to that store. A `BUSINESS_OWNER` with multiple stores must specify which store they are logging into via `storeId` in the login request.

### Subscriptions & Plan Limits
Stores are on a subscription plan (`FREE`, `STARTER`, `PRO`, etc.). Plans enforce:
- `productLimit` — max products the store can have
- `staffLimit` — max staff members
- `paymentsEnabled` — whether payment processing is unlocked
- `customDomainEnabled` — whether custom domain can be set

Creating products beyond the limit throws a `403`. Plans are created and managed by `SUPER_ADMIN`.

### Escrow System
Payments are held in escrow before being released to the seller. Flow:
1. Customer pays → payment recorded, escrow created with `HELD` status
2. After fulfilment period, escrow auto-releases (scheduled job, every 15 min)
3. Either party can raise a **dispute** → escrow status becomes `DISPUTED`
4. ADMIN/SUPER_ADMIN resolves the dispute → escrow released to seller or refunded

### Coupons / Marketing
Store owners create discount coupons with:
- Fixed amount or percentage discount
- Min order value
- Max usage count
- Expiry date

Customers apply them at checkout via the public `POST /api/v1/marketing/coupons/validate` endpoint.

### Store Builder
Business owners can customise their storefront:
- **Branding:** logo URL, banner URL, brand colour hex, template (`DEFAULT`, `MINIMAL`, `BOLD`, `ELEGANT`)
- **Domain:** custom domain name (requires `customDomainEnabled` plan feature)
- **SEO:** meta title, meta description, keywords

### Product Images
`POST /api/v1/products/{id}/images` accepts multipart form files (multiple files supported). Images are uploaded to AWS S3 and URLs are stored on the product. AWS credentials must be set in `.env`.

### Email Queue
All transactional emails (order confirm, escrow release, dispute notices, etc.) are queued in the database and dispatched every 30 seconds by a background job. Uses Resend SMTP (`smtp.resend.com:465`). Currently dispatches plain-text; HTML templates can be built on top.

---

## Response Format

All endpoints return a consistent envelope:

```json
{
  "success": true,
  "message": "Human-readable message",
  "data": { ... }
}
```

Errors return:
```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "MACHINE_CODE",
  "data": null
}
```

### Common HTTP Status Codes
| Code | Meaning |
|---|---|
| `200` | Success |
| `201` | Created |
| `400` | Validation error / bad request body |
| `401` | Missing or invalid JWT token |
| `403` | Role not permitted for this action |
| `404` | Resource not found |
| `409` | Conflict (e.g. duplicate email, slug already taken) |
| `429` | Too many login attempts (account locked) |
| `500` | Server error |

---

## Frontend App Architecture Suggestion

Based on the roles and features, here is how the frontend should be structured:

### App 1 — Public Storefront (per store, `storeId` in URL or subdomain)
- Browse published products
- Register / login as customer
- Cart → Checkout → Payment
- Apply coupons
- View orders

### App 2 — Merchant Dashboard (BUSINESS_OWNER + STAFF)
- Store setup wizard (branding, domain, SEO)
- Product management + image upload
- Order management
- Coupon management
- Staff management
- Subscription & billing
- Analytics dashboard
- Escrow transactions

### App 3 — CartWave Internal Panel (ADMIN + SUPER_ADMIN)
- User management (suspend/activate)
- All stores overview
- Revenue & health monitoring
- Subscription plan management
- Create / remove internal admin accounts (`SUPER_ADMIN` only)
- Escrow dispute resolution
- Email queue monitoring

---

## Known Limitations / What Is NOT Yet Built

| Item | Status |
|---|---|
| HTML email templates | Queuing works, plain text only — HTML needs design |
| Password reset / forgot password flow | Not implemented |
| Email verification flow | Field exists (`emailVerified`) but no verification email send |
| Real-time notifications (WebSocket) | Not implemented |
| Product search / filtering | No search endpoint — frontend must filter client-side |
| Refund processing | No dedicated refund endpoint — handled manually via escrow dispute |
| Customer address management | `addressesJson` field exists on Customer entity but no dedicated API |
| Customer wishlist | `wishlistJson` field exists but no dedicated API |
| Webhook signature verification | Webhook endpoint exists but no HMAC signature check |
| OAuth / Social login | Not implemented |

---

## Quick Integration Checklist

```
[ ] Set Authorization: Bearer <token> header on every protected request
[ ] Store both accessToken and refreshToken after login
[ ] Call POST /api/v1/auth/refresh when you get a 401 on an access token
[ ] For customer registration, always include storeId
[ ] For BUSINESS_OWNER login with multiple stores, always include storeId
[ ] Check the "errorCode" field in error responses for machine-readable codes
[ ] Image uploads use multipart/form-data — set the right Content-Type header
[ ] Coupon validation is public — call it before showing the cart total
[ ] Poll GET /api/v1/orders/{orderId} after checkout to track order + payment status
```
