# CartWave Backend MVP

Spring Boot backend for the CartWave multi-tenant commerce MVP.

## What is included
- JWT auth with access and refresh tokens.
- Tenant-aware store access carried in JWT `storeId`.
- Public self-service signup for `BUSINESS_OWNER` and `CUSTOMER` only.
- Owner and staff backoffice for stores, products, orders, staff, billing, subscriptions, and dashboards.
- Customer profile, cart, checkout, order history, and internal payment stub flows.
- Public storefront endpoints under `/api/v1/public/stores/{slug}`.
- Flyway migrations with forward-only schema alignment in `V2__mvp_alignment.sql`.
- Scheduled jobs for subscription expiry, KPI snapshots, fraud flags, escrow release, and email dispatch.

## Requirements
- Java 21
- Docker optional, only for the PostgreSQL-backed integration test
- PostgreSQL for local runtime

## Configuration
Set these environment variables before starting the app:

- `DB_URL` or `DATABASE_URL`
- `DB_USER` or `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` as needed

Defaults in `application.yaml` keep mail local-friendly and expose:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/api/v1/health`

## Run
Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

The wrapper is configured to use a repo-local Maven cache at `.m2repo/`.

## Seeded data
Startup seeds subscription plans and one super admin account:

- Email: `superadmin@cartwave.local`
- Password: `Password123!`

## Auth and tenancy rules
- `POST /api/v1/auth/register` accepts only `CUSTOMER` and `BUSINESS_OWNER`.
- `CUSTOMER` registration requires `storeId`.
- `BUSINESS_OWNER` registers first, then creates a store after login.
- Login accepts optional `storeId`; it becomes required when the account can access multiple stores.
- Auth endpoints return raw `JwtAuthResponse`.
- All non-auth endpoints return `ApiResponse<T>`.

## Tests
Run:

```powershell
.\mvnw.cmd test
```

Included tests cover auth, JWT tenancy, checkout behavior, and a PostgreSQL-backed integration test. The PostgreSQL integration test uses Testcontainers and skips automatically when Docker is unavailable.

## Docs
- API summary: `API_OVERVIEW.md`
- Local usage: `QUICK_START.md`
- Frontend integration notes: `DOCUMENTATION/FRONTEND_AND_API_SPEC.md`
- Testing and Postman guide: `DOCUMENTATION/BACKEND_CAPABILITY_AND_FRONTEND_SPEC.md`
- Postman collection: `DOCUMENTATION/postman_collection.json`
