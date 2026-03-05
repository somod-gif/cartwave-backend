# CartWave Backend MVP

Spring Boot backend for a multi-tenant e-commerce SaaS supporting SUPER_ADMIN, ADMIN, BUSINESS_OWNER, STAFF, and CUSTOMER personas.

## Features
- JWT authentication with access and refresh tokens.
- Multi-tenant API base path `/api/v1` with store-scoped domain data.
- Modules: stores, products, auth/customers, orders, billing/payments, subscriptions, email queue.
- Flyway bootstrap migration (`V1__init.sql`) for core platform schema.
- Scheduled jobs for escrow release, subscription expiration, KPI aggregation, fraud scan, and email dispatch.
- OpenAPI docs via Swagger UI.

## Setup
1. Java 21 + Maven 3.9+
2. Set env vars:
   - `DB_URL` (defaults to Neon URL)
   - `DB_USER` (defaults `neondb_owner`)
   - `DB_PASSWORD` (required)
   - `JWT_SECRET`
   - `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`
3. Run: `mvn spring-boot:run`

## Run and Test
- App: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Tests: `mvn test`

## Docker
```bash
docker build -t cartwave-backend .
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://... \
  -e DB_USER=neondb_owner \
  -e DB_PASSWORD=... \
  -e JWT_SECRET=... \
  cartwave-backend
```

## Deployment
- Build immutable image from multi-stage `Dockerfile`.
- Inject secrets via runtime env vars.
- Point to managed PostgreSQL (Neon) and SMTP provider.
