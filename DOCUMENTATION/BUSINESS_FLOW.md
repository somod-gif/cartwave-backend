# CartWave — Business Flow & Logic

This document describes every business flow in CartWave, the rules enforced, the lifecycle of key entities, and how the system components interact.

---

## Table of Contents

1. [Platform Model](#1-platform-model)
2. [User Lifecycle](#2-user-lifecycle)
3. [Store Lifecycle](#3-store-lifecycle)
4. [Product Lifecycle](#4-product-lifecycle)
5. [Shopping Flow (Cart → Checkout → Order)](#5-shopping-flow)
6. [Payment Flow](#6-payment-flow)
7. [Escrow Flow](#7-escrow-flow)
8. [Order Lifecycle](#8-order-lifecycle)
9. [Subscription & Plan Enforcement](#9-subscription--plan-enforcement)
10. [Staff Management Flow](#10-staff-management-flow)
11. [Fraud Detection](#11-fraud-detection)
12. [Email System](#12-email-system)
13. [Analytics & KPI](#13-analytics--kpi)
14. [Multi-Tenancy Model](#14-multi-tenancy-model)
15. [Complete End-to-End Flow](#15-complete-end-to-end-flow)

---

## 1. Platform Model

CartWave is a **multi-tenant SaaS e-commerce platform**.

```
CartWave Platform
├── Super Admin (platform operator)
└── Stores (tenants)
    ├── Business Owner (store creator)
    ├── Admin(s) (store managers)
    ├── Staff (store employees)
    └── Customers (shoppers)
```

**Key concept:** Every store is an independent tenant. Data never leaks between stores. A user can belong to multiple stores in different roles (e.g., own one store, be staff at another, shop at a third).

---

## 2. User Lifecycle

### Registration

```
User registers
  ├── As BUSINESS_OWNER → can create stores
  └── As CUSTOMER → must provide storeId → Customer profile created for that store
```

**Rules:**
- Only `BUSINESS_OWNER` and `CUSTOMER` can self-register (public endpoint)
- `ADMIN`, `STAFF`, `SUPER_ADMIN` are created by existing privileged users
- Customers always belong to a specific store
- Email must be unique across entire platform

### Login

```
User logs in
  ├── Single store → auto-selected
  ├── Multiple stores → must specify storeId
  └── JWT issued with: userId, role, storeId, tenantId, permissions
```

**Rules:**
- 5 failed login attempts → 15-minute account lockout
- Customer profile auto-created if logging into a store for the first time
- JWT access token expires in 15 minutes, refresh token in 7 days

### User Status Flow

```
ACTIVE ──→ INACTIVE ──→ SUSPENDED ──→ BANNED
  ↑                                      │
  └──────────── (can be reactivated) ────┘
```

---

## 3. Store Lifecycle

### Creation Flow

```
1. Business Owner registers
2. Business Owner logs in → receives JWT (no storeId yet)
3. Business Owner creates store → POST /stores
   └── System auto-provisions:
       ├── Store record (active=true, currency=USD)
       └── FREE subscription (30-day, auto-renewal)
4. Owner can now log in with storeId → full tenant context
```

### Store Properties

| Property | Rule |
|----------|------|
| `slug` | Must be unique across platform. Used in public URLs. |
| `subscriptionPlan` | Determines feature limits. Defaults to `FREE`. |
| `isActive` | Soft-toggle. Inactive stores don't appear in public queries. |
| `customDomain` | Only settable if subscription plan allows it (PRO/ENTERPRISE). |
| `currency` | Defaults to `USD`. Used for cart and billing. |

### Store Access Rules

| Role | Can See | Can Edit | Can Delete |
|------|---------|----------|------------|
| SUPER_ADMIN | All stores | All stores | All stores |
| ADMIN | All stores | All stores | All stores |
| BUSINESS_OWNER | Own stores only | Own stores only | Own stores only |
| STAFF | Current tenant only | — | — |
| CUSTOMER | — | — | — |

---

## 4. Product Lifecycle

### Creation Rules

```
Staff/Owner attempts to create product
  │
  ├── Check subscription plan product limit
  │   ├── FREE: 20 products max
  │   ├── STARTER: 100 products max
  │   ├── PRO: 1,000 products max
  │   └── ENTERPRISE: unlimited (limit=0)
  │
  ├── If limit exceeded → 400 error "Product limit exceeded for your subscription plan"
  │
  └── If within limit → Product created
      ├── Status defaults to ACTIVE
      └── Stock defaults to 0
```

### Product Status Flow

```
ACTIVE ←──→ INACTIVE
  │              │
  │              └──→ ARCHIVED
  │
  └──→ OUT_OF_STOCK (auto-set when stock reaches 0 during checkout)
         │
         └──→ ACTIVE (when stock is replenished via product update)
```

### Public Visibility

Only `ACTIVE` products appear in the public catalog (`GET /public/stores/{slug}/products`). Other statuses are hidden from customers.

---

## 5. Shopping Flow

### Complete Cart-to-Checkout Flow

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: BROWSE (Public — no auth required)                   │
│                                                              │
│ Customer visits → GET /public/stores/{slug}                  │
│ Customer browses → GET /public/stores/{slug}/products        │
└──────────────────────────┬──────────────────────────────────┘
                           │ Customer decides to buy
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 2: REGISTER & LOGIN (if not already)                    │
│                                                              │
│ POST /customers/register { email, password,                  │
│                            role: "CUSTOMER", storeId }       │
│ POST /auth/login { email, password, storeId }                │
│ → Receives JWT with CUSTOMER role and storeId                │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: ADD TO CART (Requires CUSTOMER auth)                 │
│                                                              │
│ POST /cart/items { productId, quantity: 2 }                   │
│   ├── Validates: product is ACTIVE and not deleted            │
│   ├── If product already in cart → adds to existing quantity  │
│   ├── Recalculates: lineTotal, subtotal, total                │
│   └── Returns full cart with all items                        │
│                                                              │
│ PATCH /cart/items/{itemId} { productId, quantity: 5 }         │
│   └── Sets absolute quantity, recalculates                    │
│                                                              │
│ DELETE /cart/items/{itemId}                                    │
│   └── Removes item, recalculates                              │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 4: CHECKOUT                                             │
│                                                              │
│ POST /checkout {                                              │
│   deliveryAddress: "123 Main St",                             │
│   customerEmail: "john@example.com",                          │
│   paymentMethod: "CARD",                                      │
│   paymentProvider: "STRIPE"                                   │
│ }                                                             │
│                                                              │
│ What happens server-side:                                     │
│  1. Validate cart is not empty → else EMPTY_CART error        │
│  2. For each item in cart:                                    │
│     ├── Find product → else PRODUCT_NOT_FOUND                 │
│     └── Check stock ≥ quantity → else INSUFFICIENT_STOCK     │
│  3. Create Order (status=PENDING, paymentStatus=PENDING)      │
│     └── Order number: CW-{epoch}-{random6}                    │
│  4. For each item:                                            │
│     ├── Deduct stock from product                             │
│     ├── If stock ≤ 0 → auto-set status to OUT_OF_STOCK      │
│     └── Create OrderItem record                               │
│  5. Create BillingTransaction (status=PENDING)                │
│     └── Transaction ID: txn_{random16}                        │
│  6. Mark cart as CHECKED_OUT, soft-delete items               │
│  7. Return: orderId, orderNumber, transactionId, totalAmount  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 5: PAYMENT (see Payment Flow section below)             │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Payment Flow

### Three-Step Payment Lifecycle

```
STEP 1: INITIATE
  POST /payments/initiate { orderId, paymentMethod: "CARD", paymentProvider: "STRIPE" }
  
  Server actions:
    ├── Find order by orderId + storeId
    ├── Find billing transaction for this order
    │   └── If none exists → PAYMENT_NOT_READY error
    ├── Set billing status → PROCESSING
    ├── Set order paymentStatus → PROCESSING
    └── Return: transactionId, status, provider, method

                    │
                    ▼
              
  (Customer pays via external provider: Stripe, Paystack, etc.)

                    │
                    ▼

STEP 2a: CONFIRM (Success)
  POST /payments/confirm { transactionId, status: "SUCCESS", providerReference: "pi_abc123" }
  
  Server actions:
    ├── Find billing transaction by transactionId
    ├── Find associated order
    ├── Create/update Payment record (status=COMPLETED, confirmedAt=now)
    ├── Set billing status → HOLD
    ├── Set billing processedAt → now
    ├── Set order paymentStatus → COMPLETED
    ├── Calculate releaseAt → now + 2 days
    └── Create Escrow hold (status=HELD, amount=orderTotal, releaseAt=+48h)

STEP 2b: CONFIRM (Failure)
  POST /payments/confirm { transactionId, status: "FAILED" }
  
  Server actions:
    ├── Set billing status → FAILED (with failureReason)
    ├── Set order paymentStatus → FAILED
    └── Create Payment record (status=FAILED)

STEP 3 (Alternative): WEBHOOK
  POST /payments/webhook { transactionId, status, failureReason }
  
  → Converts to PaymentConfirmRequest and delegates to confirm flow
  → Used by external payment providers to push status updates
```

### Payment Status Diagram

```
PENDING ──→ PROCESSING ──→ COMPLETED ──→ (funds held in escrow)
                │
                └──→ FAILED
                       │
                       └──→ REFUNDED (manual)
                              │
                              └──→ PARTIALLY_REFUNDED
```

---

## 7. Escrow Flow

Escrow protects both buyers and sellers by holding funds between payment confirmation and order delivery.

### How It Works

```
Payment confirmed (SUCCESS)
  │
  └── Escrow created
      Status: HELD
      Amount: order total
      Release date: payment time + 48 hours
      │
      ├──── PATH A: Order delivered ────────────────────────┐
      │     └── Staff/Owner sets order status → DELIVERED   │
      │         └── Escrow immediately → RELEASED           │
      │             └── Funds available to store owner       │
      │                                                     │
      ├──── PATH B: Auto-release (no manual action) ───────┐
      │     └── Daily job at 2 AM checks all HELD escrows   │
      │         └── If releaseAt has passed → RELEASED      │
      │                                                     │
      └──── PATH C: Dispute ────────────────────────────────┐
            └── Customer/Owner raises dispute                │
                └── Escrow → DISPUTED                       │
                    └── EscrowDispute created (OPEN)        │
                        │                                   │
                        ├── UNDER_REVIEW                    │
                        ├── RESOLVED → funds released       │
                        └── REJECTED → funds returned       │
```

### Escrow Business Rules

| Rule | Detail |
|------|--------|
| Hold period | 48 hours from payment confirmation |
| Auto-release | Daily at 2 AM for expired holds |
| Manual release | Triggered when order status → DELIVERED |
| Dispute | Can be raised at any time while HELD. Freezes the escrow. |

---

## 8. Order Lifecycle

### Status Transitions

```
PENDING ──→ CONFIRMED ──→ PROCESSING ──→ SHIPPED ──→ DELIVERED
   │                                                      │
   │                                                      ├── completedAt = now
   │                                                      ├── releaseAt = now + 48h
   │                                                      └── escrow → RELEASED
   │
   ├──→ CANCELLED (from any pre-delivery state)
   └──→ REFUNDED (after payment completed)
```

### Who Can Do What

| Action | Allowed Roles |
|--------|---------------|
| View own orders | CUSTOMER |
| View all store orders | BUSINESS_OWNER, ADMIN, STAFF, SUPER_ADMIN |
| Create order (manual) | BUSINESS_OWNER, ADMIN, STAFF |
| Update order details | BUSINESS_OWNER, ADMIN, STAFF (NOT customer) |
| Change order status | BUSINESS_OWNER, ADMIN, STAFF (NOT customer) |

### Side Effects of Status Changes

| New Status | What Happens |
|------------|-------------|
| DELIVERED | `completedAt` set, `releaseAt` calculated (+48h), billing transaction `releaseAt` updated, escrow released |
| CANCELLED | (Currently: status update only) |
| REFUNDED | (Currently: status update only) |

---

## 9. Subscription & Plan Enforcement

### Plans Comparison

| Feature | FREE | STARTER | PRO | ENTERPRISE |
|---------|:----:|:-------:|:---:|:----------:|
| Monthly price | $0 | $19 | $99 | $499 |
| Product limit | 20 | 100 | 1,000 | Unlimited |
| Staff limit | 1 | 3 | 10 | Unlimited |
| Payments enabled | No | Yes | Yes | Yes |
| Custom domain | No | No | Yes | Yes |

### Where Limits Are Enforced

```
Create Product → ProductService checks SubscriptionService.assertCanCreateProducts()
                   └── If currentProducts + 1 > planLimit → 400 "Product limit exceeded"
                   └── If planLimit = 0 (ENTERPRISE) → unlimited, always passes

Add Staff     → StaffService checks SubscriptionService.assertCanAddStaff()
                   └── If currentStaff + 1 > planLimit → 400 "Staff limit exceeded"

View Billing  → BillingService checks SubscriptionService.isFeatureEnabled("payments")
                   └── If plan doesn't have paymentsEnabled → 400 "Payments not allowed"

Custom Domain → StoreService checks SubscriptionService.isFeatureEnabled("custom_domain")
                   └── If plan doesn't have customDomainEnabled → 400 "Custom domain not allowed"
```

### Plan Change Flow

```
Owner requests plan change → POST /subscriptions/change { planName: "PRO" }
  │
  ├── Lookup plan by name
  ├── Create or update subscription record
  ├── Set status=ACTIVE, billingCycle, autoRenewal
  ├── Set startDate=now, endDate=now+30days
  ├── Update store.subscriptionPlan
  └── Old subscription replaced (not versioned)
```

### Subscription Expiry

```
Hourly background job runs:
  │
  └── Find active subscriptions where endDate ≤ now
      └── Set status → EXPIRED
          (Store still works but enforcement kicks in on next operation)
```

---

## 10. Staff Management Flow

### Adding Staff

```
Owner/Admin adds staff → POST /staff { userId, role: "MANAGER" }
  │
  ├── Check subscription staff limit → assertCanAddStaff()
  ├── Verify user exists
  ├── Check no duplicate staff record → STAFF_EXISTS error if duplicate
  ├── Create staff record (status=ACTIVE)
  └── If user was CUSTOMER → auto-promote role to STAFF
```

### Removing Staff

```
Owner/Admin removes staff → DELETE /staff/{staffId}
  │
  ├── Set staff status → TERMINATED
  ├── Soft-delete staff record
  └── If no remaining staff memberships across ANY store
      └── Downgrade user role back to CUSTOMER
```

### Staff Roles

| Staff Role | Intended Purpose |
|-----------|------------------|
| MANAGER | Full store management |
| ADMIN | Administrative tasks |
| INVENTORY | Product and stock management |
| SUPPORT | Customer support and order handling |
| MARKETING | Marketing features |
| FINANCIAL | Billing and financial oversight |

*(Note: All staff roles currently have the same technical permissions — `ROLE_STAFF`. Differentiation is for UI organization.)*

---

## 11. Fraud Detection

### Automated Scanning

The fraud detection job runs **every 10 minutes** across all active stores.

### Detection Rules

| Rule | Trigger | Severity | Why |
|------|---------|----------|-----|
| HIGH_VALUE_ORDER | Order total ≥ $10,000 | MEDIUM | Unusually large purchase |
| FAILED_PAYMENT | Payment status = FAILED | HIGH | Payment fraud indicator |

### How It Works

```
FraudDetectionJob runs
  │
  └── For each active store:
      │
      ├── Query orders with totalAmount ≥ 10,000
      │   └── For each: check if already flagged (deduplicate)
      │       └── If new → create FraudFlag (severity=MEDIUM, reason="HIGH_VALUE_ORDER")
      │
      └── Query orders with paymentStatus = FAILED
          └── For each: check if already flagged (deduplicate)
              └── If new → create FraudFlag (severity=HIGH, reason="FAILED_PAYMENT")
```

### Dashboard Integration

The admin dashboard shows `unresolvedFraudFlags` — the count of flags where `reviewed=false`. This alerts store owners to review suspicious activity.

---

## 12. Email System

### How It Works

```
Application enqueues email → POST /emails/enqueue
  │
  └── Email record created in email_queue table
      ├── status: PENDING
      ├── template: e.g., "order_confirmation"
      └── payloadJson: template variables as JSON

Background job runs every 30 seconds:
  │
  └── Fetch up to 50 PENDING emails (oldest first)
      │
      ├── Send via SMTP (JavaMailSender)
      │   ├── Success → status = SENT
      │   └── Failure → retryCount++, store error message
      │       └── After 3 retries → status = FAILED (stops retrying)
      │
      └── Next cycle picks up remaining PENDING emails
```

### Email Status Flow

```
PENDING ──→ SENT (success)
   │
   └──→ PENDING (retry 1) ──→ PENDING (retry 2) ──→ PENDING (retry 3) ──→ FAILED
```

---

## 13. Analytics & KPI

### Real-Time Metrics (Computed on Request)

**Dashboard Metrics** (`GET /dashboard/metrics`):
- `totalOrders` — count of orders in store
- `revenue` — sum of captured (COMPLETED) billing transactions
- `activeCustomers` — count of customers in store
- `pendingEscrow` — count of HELD escrow transactions

**Admin Dashboard** (`GET /admin/dashboard`):
- `productCount` — total products
- `orderCount` — total orders
- `pendingOrders` — orders with status=PENDING
- `lowStockProducts` — products with stock ≤ 5
- `staffCount` — active staff members
- `unresolvedFraudFlags` — flags with reviewed=false
- `revenue` — captured billing sum

**Super Admin Dashboard** (`GET /super-admin/dashboard`):
- `storeCount` — total active stores
- `userCount` — total users
- `ownerCount` — users with role=BUSINESS_OWNER
- `customerCount` — total customer profiles
- `orderCount` — total orders across platform
- `revenue` — platform-wide captured revenue

### Periodic Snapshots (Background Job)

Every 30 minutes, per active store:
```
KpiAggregationJob
  └── For each active store:
      └── Create/update daily kpi_snapshot:
          ├── revenue (sum of COMPLETED billing for that day)
          ├── orderCount (orders created that day)
          ├── customerCount (customers created that day)
          └── snapshotDate (today)
```

These snapshots enable trend charts and historical reporting on the frontend.

---

## 14. Multi-Tenancy Model

### How Tenant Isolation Works

```
Request arrives with JWT
  │
  └── JwtAuthenticationFilter extracts tenantId from token
      └── Sets TenantContext.setTenantId(tenantId)
          │
          └── ALL service/repository calls use TenantContext.getTenantId()
              to scope queries: findByIdAndStoreId(id, storeId)
              │
              └── Data from other stores is NEVER accessible
```

### Key Properties

| Aspect | Implementation |
|--------|---------------|
| Tenant identifier | `storeId` (UUID), carried in JWT |
| Isolation level | Application-level (shared database, filtered queries) |
| Context propagation | `ThreadLocal<UUID>` via `TenantContext` |
| Context lifecycle | Set at filter entry, cleared in `finally` block |
| Cross-tenant access | Only `SUPER_ADMIN` can see across tenants (dashboard only) |

### Multi-Store Users

A single user can have access to multiple stores:
```
User (email: john@example.com)
  ├── BUSINESS_OWNER of Store A
  ├── STAFF at Store B
  └── CUSTOMER at Store C

Login requires storeId to pick which context to use.
JWT issued for one store at a time.
```

---

## 15. Complete End-to-End Flow

### Owner Journey

```
1. REGISTER as BUSINESS_OWNER
2. LOGIN → JWT (no storeId yet for first login)
3. CREATE STORE → auto-provisions FREE subscription
4. LOGIN again with storeId → full tenant context
5. ADD PRODUCTS (up to plan limit)
6. SHARE public URL: /public/stores/{slug}
7. MANAGE incoming orders (status updates)
8. VIEW dashboard metrics and admin stats
9. ADD STAFF (up to plan limit)
10. UPGRADE subscription for more capacity
11. REVIEW fraud flags
12. VIEW billing transactions and escrow status
```

### Customer Journey

```
1. BROWSE store at /public/stores/{slug} (no auth)
2. VIEW products at /public/stores/{slug}/products (no auth)
3. REGISTER as CUSTOMER (with storeId)
4. LOGIN with storeId → JWT
5. ADD items to cart
6. ADJUST quantities or remove items
7. CHECKOUT → order + billing transaction created
8. INITIATE PAYMENT → billing goes to PROCESSING
9. PAY via external provider
10. CONFIRM PAYMENT → escrow hold created, order COMPLETED
11. TRACK order status (PENDING → CONFIRMED → SHIPPED → DELIVERED)
12. ORDER DELIVERED → escrow released, funds available to store
```

### Money Flow

```
Customer pays $100
  │
  └── Payment confirmed → $100 held in ESCROW (HELD)
      │
      ├── Order delivered within 48h → escrow RELEASED → store gets $100
      │
      ├── 48h passes without delivery → auto-release → store gets $100
      │
      └── Dispute raised → escrow DISPUTED → manual review
          ├── RESOLVED → funds released
          └── REJECTED → funds returned to customer
```
