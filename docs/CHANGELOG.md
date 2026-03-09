# Changelog

All notable changes to CartWave Backend are documented here.  
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [2.0.0] — Feature Upgrade Release

### Added

#### Store Builder (V2)
- `Store` entity: `template`, `brandColor`, `customDomainName`, `subdomain`, `storeStatus`, `metaTitle`, `metaDescription`, `keywords` fields
- `StoreTemplate` enum: `MINIMAL`, `MODERN`, `BOLD`, `CLASSIC`
- `StoreStatus` enum: `ACTIVE`, `SUSPENDED`, `PENDING`
- Auto-generate `subdomain` as `{slug}.cartwave.store` on store creation
- New endpoints: `PUT /stores/{id}/branding`, `PUT /stores/{id}/domain`, `PUT /stores/{id}/seo`, `GET /stores/{id}/public`, `GET /stores/{storeId}/products`

#### Subscription Plans (V2)
- Naira-denominated plans: STARTER (free), BASIC (₦5,000), GROWTH (₦15,000), PRO (₦30,000), ENTERPRISE (custom)
- `GET /subscriptions/plans` made public (no auth required)
- New `POST /subscriptions/subscribe` and `POST /subscriptions/cancel` endpoints
- `SubscribeRequest` DTO with `planName`, `billingCycle`, `autoRenewal`

#### Product Management (V2)
- `Product` entity: `tags`, `isPublished`, `seoTitle`, `seoDescription` fields
- AWS S3 integration for product image upload (`AwsS3Config`, `AwsS3Service`)
- New endpoints: `POST /products/{id}/images`, `DELETE /products/{id}/images/{url}`, `PUT /products/{id}/publish`
- Public published product listing via store page

#### Order Management (V2)
- `GET /orders/store/{storeId}` — list orders by store
- `GET /orders/customer/{customerId}` — list orders by customer
- `PUT /orders/{orderId}/status` — status update (alias for PATCH)

#### Escrow System (V2)
- `EscrowTransaction` entity: `platformFeePercent`, `sellerAmount`, `releasedAt` fields
- `EscrowDispute` entity: `evidence`, `adminResolutionNotes` fields
- New `EscrowController` with: `POST /{id}/release`, `POST /{id}/dispute`, `PUT /dispute/{id}/resolve`, `GET /store/{storeId}`
- Automatic seller amount computation based on platform fee percentage (default 2.5%)
- Email notifications on escrow release and dispute events

#### Marketing — Coupons
- `Coupon` entity with `DiscountType` (PERCENT/FIXED), `maxUses`, `usedCount`, `expiresAt`
- `CouponService` with create, list, delete, validate, apply
- `MarketingController`: `POST/GET/DELETE /marketing/stores/{storeId}/coupons`, `POST /marketing/coupons/validate` (public)

#### Email Notifications (V2)
- `EmailQueue.sentAt` — tracks actual dispatch timestamp
- Typed convenience methods on `EmailQueueService`: `enqueueEscrowReleased`, `enqueueDisputeOpened`, `enqueueDisputeResolved`, `enqueueOrderConfirmed`, `enqueueOrderShipped`, `enqueueOrderDelivered`, `enqueuePasswordReset`, `enqueueSubscriptionExpiring`

#### Admin APIs (V2)
- `AdminService` — platform-wide user management, revenue summary, health check
- Expanded `AdminController`: users (list/get/suspend/activate), revenue, health, plan management
- `RevenueSummaryDTO`, `PlatformHealthDTO`, `UserAdminDTO`, `CreatePlanRequest` DTOs
- `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")` on all admin endpoints

#### Analytics & Dashboard (V2)
- `DashboardMetricsResponse` enriched with: `pendingOrders`, `deliveredOrders`, `productCount`, `lowStockProducts`, `subscriptionPlan`, `subscriptionStatus`
- `DashboardService.getMetrics()` populates all V2 fields

#### Security (V2)
- `@EnableMethodSecurity(prePostEnabled = true)` added to `SecurityConfig`
- Public routes added: `/api/v1/stores/*/public`, `/api/v1/stores/*/products`, `/api/v1/subscriptions/plans`, `/api/v1/marketing/coupons/validate`
- Admin route guard: `/api/v1/admin/**` requires `ADMIN` or `SUPER_ADMIN` at filter chain level

#### Infrastructure
- AWS SDK v2 (`software.amazon.awssdk:s3:2.26.21`) added to `pom.xml`
- Flyway `V2__feature_upgrades.sql` migration for all new columns and the `coupons` table
- `.env.example` with all required environment variables
- `docs/` directory with README, ARCHITECTURE, API, and CHANGELOG

### Changed
- `AdminController` class-level `@PreAuthorize` tightened to `ADMIN, SUPER_ADMIN` (removed `BUSINESS_OWNER, STAFF`)
- `PaymentRepository` extended with `sumAmountByStatus`, `sumAmountByStatusSince`, `countByDeletedFalse`, `findAllByStoreIdAndDeletedFalseOrderByCreatedAtDesc`
- `EmailQueueRepository` extended with `countByStatus`
- `SubscriptionRepository` extended with `countByStatus`

---

## [1.0.0] — Initial Release

- Full multi-tenant e-commerce backend with store, product, order, payment, escrow, subscription, customer, staff, cart, checkout, auth, dashboard, fraud detection, and KPI aggregation modules.
