# CartWave Backend

Multi-tenant e-commerce platform backend built with **Spring Boot 3**, **PostgreSQL**, **JWT authentication**, and **Flyway** migrations.

## Quick Start

```bash
# Prerequisites: Java 17+, PostgreSQL (or use local H2 profile)

# 1. Run with local H2 database (no Postgres needed)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 2. Or run with PostgreSQL
export DATABASE_URL=jdbc:postgresql://localhost:5432/cartwave
export DB_USERNAME=cartwave
export DB_PASSWORD=yourpassword
./mvnw spring-boot:run

# 3. Or Docker
docker build -t cartwave-backend .
docker run -p 8080:8080 -e DATABASE_URL=... cartwave-backend
```

**Base URL:** `http://localhost:8080`  
**Health check:** `GET /api/v1/health`

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | Server port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/cartwave` | PostgreSQL connection URL |
| `DB_USERNAME` | `cartwave` | Database username |
| `DB_PASSWORD` | — | Database password |
| `JWT_SECRET` | dev fallback | Secret key for signing JWTs |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` (15 min) | Access token TTL |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7 days) | Refresh token TTL |
| `SMTP_HOST` | `localhost` | SMTP server host |
| `SMTP_PORT` | `1025` | SMTP server port |
| `CORS_ALLOWED_ORIGINS` | `*` | Allowed CORS origins |

## Architecture

- **Multi-tenant** — each store is an isolated tenant; JWT carries `storeId`, resolved via `TenantContext`
- **JWT auth** — stateless, Bearer token in `Authorization` header
- **5 roles** — `SUPER_ADMIN`, `ADMIN`, `BUSINESS_OWNER`, `STAFF`, `CUSTOMER`
- **17 database tables** — managed by Flyway migration
- **Background jobs** — email dispatch, escrow release, subscription expiry, KPI aggregation, fraud scanning

## Documentation

See **[DOCUMENTATION/API.md](DOCUMENTATION/API.md)** for the complete API reference — all 38 endpoints, request/response schemas, enums, authentication guide, and frontend integration spec.

