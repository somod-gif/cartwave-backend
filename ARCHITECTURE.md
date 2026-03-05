# CartWave Architecture

## Multi-tenancy
- Tenant boundary is `store_id` across business-domain tables.
- JWT carries identity and optional store context claim.
- Service layer enforces store-scoped repository access.

## Escrow lifecycle
1. Payment success creates escrow transaction in HOLD state.
2. Scheduled `EscrowAutoReleaseJob` checks releasable transactions.
3. If delivery confirmed and no dispute, funds are released.
4. Disputes are tracked in `disputes` and block release.

## Email system
- `email_queue` table persists pending mail jobs.
- API enqueues transactional events (registration/orders/payments/subscriptions/escrow).
- `EmailDispatcherJob` retries and marks SENT/FAILED with error metadata.

## Background jobs
- Escrow auto-release (cron)
- Subscription expiration checker (cron)
- KPI aggregation (cron)
- Email dispatcher (fixed delay)
- Fraud scanner (cron)

## Security
- Spring Security with JWT filter.
- BCrypt password storage.
- Login attempt throttling lockout in auth service.
- Global exception handler and CORS config.
