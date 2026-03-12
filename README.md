# CartWave Backend

Multi-tenant e-commerce SaaS platform backend built with **Spring Boot 3.4.2**, **PostgreSQL** (Neon), **JWT authentication**, **Redis caching**, and **Flyway** migrations.

## Quick Start

```bash
# Prerequisites: Java 21+, PostgreSQL

# 1. Run with local profile (H2, no Postgres needed)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 2. Run with PostgreSQL
export DATABASE_URL=jdbc:postgresql://localhost:5432/cartwave
export DB_USERNAME=cartwave
export DB_PASSWORD=yourpassword
./mvnw spring-boot:run

# 3. Docker
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
| `REDIS_URL` | — | Redis connection URL (optional — falls back to in-memory cache) |
| `SMTP_HOST` | `localhost` | SMTP server host |
| `SMTP_PORT` | `1025` | SMTP server port |
| `SMTP_USERNAME` | — | SMTP username |
| `SMTP_PASSWORD` | — | SMTP password |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated allowed CORS origins |
| `AWS_ACCESS_KEY_ID` | — | AWS S3 access key |
| `AWS_SECRET_ACCESS_KEY` | — | AWS S3 secret key |
| `AWS_S3_BUCKET` | — | S3 bucket name |
| `AWS_S3_REGION` | `us-east-1` | S3 region |
| `PAYSTACK_SECRET_KEY` | — | Paystack secret key (starts with `sk_`) |
| `PAYSTACK_PUBLIC_KEY` | — | Paystack public key (starts with `pk_`) |

## Architecture

- **Multi-tenant** — each store is an isolated tenant; JWT carries `storeId`, resolved via `TenantContext`
- **JWT auth** — stateless access tokens + DB-backed refresh token rotation with revocation
- **5 roles** — `SUPER_ADMIN`, `ADMIN`, `BUSINESS_OWNER`, `STAFF`, `CUSTOMER`
- **Redis caching** — store data, subscription plans, product catalog, dashboard metrics (optional, auto-fallback)
- **Rate limiting** — Bucket4j in-memory per-IP (login: 10/min, forgot-password: 5/10min, default: 200/min)
- **Paystack** — payment initialization + HMAC-SHA512 webhook signature verification
- **Background jobs** — email dispatch, escrow release, subscription expiry, KPI aggregation, fraud scanning
- **Security headers** — OWASP HSTS, CSP, X-Frame-Options, X-Content-Type-Options on every response

## Documentation

See **[DOCUMENTATION/API.md](DOCUMENTATION/API.md)** for the complete API reference.  
See **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** for system design and module overview.

