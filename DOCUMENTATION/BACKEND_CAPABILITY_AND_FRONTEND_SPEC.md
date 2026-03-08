# Backend Capability And Testing Guide

## Runtime endpoints
- Health: `GET /api/v1/health`
- Swagger UI: `GET /swagger-ui.html`
- OpenAPI JSON: `GET /api-docs`

## MVP capabilities
- Multi-tenant store model with JWT tenant context
- Public owner and customer registration with role restrictions
- Store, product, order, billing, subscription, staff, cart, checkout, and dashboard modules
- Internal payment initiation and webhook simulation
- Background jobs for subscription expiry, KPI snapshots, fraud flags, escrow release, and email delivery

## Postman usage
- Import `DOCUMENTATION/postman_collection.json`
- Set `baseUrl` to `http://localhost:8080`
- Fill or capture these variables as you go:
  - `accessToken`
  - `refreshToken`
  - `storeId`
  - `storeSlug`
  - `productId`
  - `cartItemId`
  - `orderId`
  - `transactionId`
  - `staffId`

## Suggested test sequence
1. Register and login an owner.
2. Create a store and save the returned `storeId` and `slug`.
3. Create a product for that store.
4. Register and login a customer with the same `storeId`.
5. Add the product to cart and checkout.
6. Initiate payment and simulate webhook completion.
7. Review orders, billing, subscription data, and dashboards.

## Automated tests
- `AuthServiceTest`
- `JwtTokenProviderTest`
- `CheckoutServiceTest`
- `PostgresIntegrationTest`

`PostgresIntegrationTest` validates Spring Boot startup, Flyway migration application, and the health endpoint against PostgreSQL through Testcontainers. It skips automatically when Docker is unavailable.
