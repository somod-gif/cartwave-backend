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
| `auth` | Registration, login, JWT token generation & refresh |
| `store` | Store CRUD, branding, subdomain, SEO, public listing |
| `product` | Product lifecycle, S3 image upload, publish toggle |
| `order` | Order creation, status transitions, revenue tracking |
| `escrow` | Payment hold, auto-release job, dispute management |
| `payment` | Paystack/Flutterwave integration, webhook handler |
| `billing` | Billing transactions, captured revenue |
| `subscription` | Plan management, store plan assignment, limits |
| `marketing` | Coupon creation, validation, apply-at-checkout |
| `customer` | Customer profiles per store |
| `staff` | Store staff invite/manage |
| `cart` | Cart management |
| `checkout` | Checkout pipeline (coupon apply, escrow hold) |
| `email` | Async email queue + dispatcher job |
| `admin` | Platform-level user management, revenue, health |
| `dashboard` | Store owner metrics + super-admin overview |
| `analytics` | KPI snapshots (KpiAggregationJob) |
| `fraud` | Fraud flag detection job |
| `jobs` | All scheduled background jobs |
| `config` | AWS S3, security filters, CORS |
| `security` | JWT filter, UserDetails, SecurityConfig |
| `common` | BaseEntity, ApiResponse, exception hierarchy |
| `tenant` | TenantContext thread-local |

## Security Model

- All endpoints require a valid JWT unless listed in `SecurityConfig.permitAll()`
- Roles: `SUPER_ADMIN > ADMIN > BUSINESS_OWNER > STAFF > CUSTOMER`
- Method-level: `@PreAuthorize("hasAnyRole(...)")` on controllers
- Route-level: `SecurityConfig` guards `/api/v1/admin/**` at the filter chain level
- Public routes: store public page, product listing, subscription plans, coupon validation

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
