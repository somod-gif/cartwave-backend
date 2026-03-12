# CartWave Architecture

## Overview

CartWave is a **multi-tenant SaaS e-commerce platform** where each merchant operates their own isolated store. Tenancy is resolved per-request via a JWT claim, and a `TenantContext` thread-local carries the `storeId` throughout the request lifecycle.

## Request Flow

```
Client
  │
  ▼
JwtAuthenticationFilter        ← Validates JWT, sets SecurityContext
  │
  ▼
TenantResolutionFilter         ← Extracts storeId from JWT → TenantContext
  │
  ▼
Controller (@PreAuthorize)     ← Role-based access check
  │
  ▼
Service                        ← Business logic (scoped to storeId)
  │
  ▼
Repository (JPA)               ← All queries include WHERE store_id = :storeId AND deleted = false
  │
  ▼
PostgreSQL (Neon)
```

## Module Map

| Package | Responsibility |
|---|---|
| `auth` | Registration, login, JWT token generation & refresh, password reset, email verification |
| `store` | Store CRUD, branding, subdomain, SEO, public listing, public catalog |
| `product` | Product lifecycle, S3 image upload, publish toggle, variants, reviews, search |
| `order` | Order creation, status transitions, tracking timeline, revenue tracking |
| `escrow` | Payment hold, auto-release job, dispute management |
| `payment` | Paystack integration, webhook handler (HMAC-SHA512), refunds |
| `billing` | Billing transactions, captured revenue |
| `subscription` | Plan management, store plan assignment, limits |
| `marketing` | Coupon creation, validation, apply-at-checkout |
| `customer` | Customer profiles per store, wishlist management |
| `staff` | Store staff invite/manage |
| `cart` | Cart management (auto-create, quantity update) |
| `checkout` | Checkout pipeline (stock validation, coupon apply, escrow hold) |
| `email` | Async email queue + dispatcher job, Thymeleaf templates |
| `admin` | Platform-level user management, revenue, health, plan management |
| `superadmin` | Platform-wide dashboard, admin CRUD, store listing, system stats |
| `dashboard` | Store owner metrics |
| `analytics` | KPI snapshots (KpiAggregationJob) |
| `fraud` | Fraud flag detection job |
| `jobs` | All scheduled background jobs |
| `config` | AWS S3, security filters, CORS, Redis |
| `security` | JWT filter, UserDetails, SecurityConfig, rate limiting, session timeout |
| `common` | BaseEntity, ApiResponse, exception hierarchy, health check |
| `tenant` | TenantContext thread-local |

## Security Model

- All endpoints require a valid JWT unless listed in `SecurityConfig.permitAll()`
- Roles: `SUPER_ADMIN > ADMIN > BUSINESS_OWNER > STAFF > CUSTOMER`
- Method-level: `@PreAuthorize("hasAnyRole(...)")` on controllers
- Route-level: `SecurityConfig` guards `/api/v1/admin/**` at the filter chain level
- Public routes: store public page, product listing, subscription plans, coupon validation, public catalog (by slug), product search, product reviews, customer registration, health check

## Multi-Tenancy

- **Tenant ID = Store ID** (UUID)
- `TenantContext.getTenantId()` returns the storeId for the current request thread
- All repository queries scope to `storeId AND deleted = false`
- No cross-tenant data leakage — each service method asserts ownership before mutation

## Escrow Flow

```
Customer pays
  │
  ▼
PaymentService.confirm() ─► EscrowService.createOrUpdateHold()
                                 │  status = HELD
                                 │  releaseAt = now + 7 days
                                 ▼
                         EscrowReleaseJob (daily sweep)
                                 │  releases HELD where releaseAt < now
                                 ▼
                          seller payout email sent
```

Disputes: buyer calls `POST /escrow/{id}/dispute` → status = DISPUTED → admin resolves via `PUT /escrow/dispute/{id}/resolve`.

## Email Queue

All outgoing emails go through `EmailQueueService.enqueue()` → stored as `PENDING` in `email_queue` table → `EmailQueueProcessor` dispatches every 30 seconds via `JavaMailSender`.

## Scheduled Jobs

| Job | Default Schedule | Purpose |
|---|---|---|
| `EscrowReleaseJob` | Daily 02:00 | Release held escrows |
| `EmailQueueProcessor` | Every 30s | Dispatch pending emails |
| `SubscriptionExpirationJob` | Hourly | Mark expired subscriptions |
| `KpiAggregationJob` | Every 30 min | Platform KPI snapshots |
| `FraudDetectionJob` | Every 10 min | Flag suspicious orders |

## V3 Enterprise Additions

### Filter Chain Order
```
RateLimitFilter          ← Bucket4j per-IP rate limiting (before auth)
  │
SecurityHeadersFilter    ← OWASP headers + X-Powered-By: CartWave
  │
JwtAuthenticationFilter  ← JWT validation, sets SecurityContext
  │
SessionTimeoutFilter     ← Redis 30-min inactivity check (after auth)
```

### Refresh Token Rotation
- On login, a random opaque token is generated; its SHA-256 hash is stored in `refresh_tokens` table
- On refresh, the hash is validated, old token revoked, new token issued (rotation)
- `POST /api/v1/auth/logout` revokes the token immediately
- Expired tokens are purged by a scheduled cleanup (via `@Modifying` query)

### Redis Caching
- Optional — if `REDIS_URL` is absent, the app falls back to `ConcurrentMapCacheManager`
- Cache TTLs defined in `application.yaml` under `spring.cache.redis.time-to-live`
- Cache names and keys documented per service in the module map

### Paystack Integration
- `PaystackService.initializeTransaction()` → `POST https://api.paystack.co/transaction/initialize`
- Webhook at `POST /api/v1/payments/paystack/webhook` verifies `X-Paystack-Signature` using HMAC-SHA512 before processing

### Pagination Convention
All paginated list endpoints accept `?page=0&size=20` query params and return a Spring `Page<T>` envelope:
```json
{
  "content": [...],
  "totalElements": 143,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

