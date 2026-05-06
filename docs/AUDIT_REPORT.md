# CartWave Backend Audit Report

_Last updated: 2026-05-06_

## Scope

This report captures the current enterprise-readiness audit of the `/api/v1` backend, including API surface, security model, tenancy boundaries, and production hardening backlog.

## Findings Summary

- Layering is mostly clean (`Controller -> Service -> Repository`) across core modules.
- Multi-tenancy primitives exist (`TenantContext`, store-scoped repositories), but enforcement is still mixed between repository filters and service-level assertions.
- Most core modules exist (auth, store, product, cart, checkout, payment, order, escrow, subscription, marketing, dashboard, admin).
- API naming and role semantics are inconsistent in docs (`BUSINESS_OWNER` / `CUSTOMER`) versus product roles (`SELLER` / `BUYER`).
- CI test reliability is blocked on Java 25 + ByteBuddy mismatch and Docker/Testcontainers availability.

## Role Mapping (Canonical)

| Product Role | Current Code Role |
|---|---|
| `SUPER_ADMIN` | `SUPER_ADMIN` |
| `ADMIN` | `ADMIN` |
| `SELLER` | `BUSINESS_OWNER` |
| `STAFF` | `STAFF` |
| `BUYER` | `CUSTOMER` |

## `/api/v1` Module Coverage

| Module | Status | Notes |
|---|---|---|
| Auth | Partial | register/login/refresh present; verification/reset flows should be validated end-to-end |
| Store | Good | create/update/branding/domain/seo and public catalog paths exist |
| Product | Good | CRUD + publish + image endpoints exist |
| Cart & Checkout | Good | cart operations + checkout pipeline present |
| Payment | Partial | initiate/confirm/webhook present, provider adapters still skeletal |
| Escrow | Good | hold/release/dispute flows present |
| Orders | Good | lifecycle APIs exist; transition guards should be regression-tested |
| Staff | Partial | entities exist; invitation/access lifecycle needs endpoint verification |
| Dashboard | Good | admin and super-admin metrics endpoints exist |
| Subscription | Partial | plan and status endpoints exist; enforcement checks need broader tests |
| Marketing (Coupons) | Good | create/list/delete/validate present |

## Security Hardening Backlog

1. Add explicit refresh-token rotation and token revocation persistence checks in integration tests.
2. Confirm CSRF strategy for browser clients (stateless API is currently token-based).
3. Introduce global rate limiting policy (IP + user + route buckets) and abuse dashboards.
4. Ensure all list endpoints enforce pagination defaults and upper bounds.
5. Add automated tenancy leak tests across all store-scoped repositories.

## Performance Backlog

1. Add query-level profiling for product listing and order retrieval.
2. Add cache hit-rate telemetry for product/store redis caches.
3. Add asynchronous webhook reconciliation and dead-letter handling for payment events.

