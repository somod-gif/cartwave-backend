# CartWave Backend — Environment Variables

> **Config file:** `src/main/resources/application.yaml`  
> **Loader:** `spring-dotenv` reads `.env` automatically at startup.  
> **Local dev:** Run with `SPRING_PROFILES_ACTIVE=local` to skip all external services (H2 in-memory, no SMTP needed).

---

## Quick Reference

| # | Variable | Required | Default | Category |
|---|----------|----------|---------|----------|
| 1 | `DATABASE_URL` | Prod ✦ | — | Database |
| 2 | `DB_URL` | Dev ✦ | `jdbc:postgresql://localhost:5432/cartwave` | Database |
| 3 | `DB_USERNAME` | Yes | `cartwave` | Database |
| 4 | `DB_PASSWORD` | Yes | *(empty)* | Database |
| 5 | `DB_CONNECTION_TIMEOUT_MS` | No | `30000` | Database / HikariCP |
| 6 | `DB_VALIDATION_TIMEOUT_MS` | No | `5000` | Database / HikariCP |
| 7 | `DB_INIT_FAIL_TIMEOUT_MS` | No | `60000` | Database / HikariCP |
| 8 | `DB_MIN_IDLE` | No | `2` | Database / HikariCP |
| 9 | `DB_MAX_POOL_SIZE` | No | `10` | Database / HikariCP |
| 10 | `DB_KEEPALIVE_MS` | No | `300000` | Database / HikariCP |
| 11 | `DB_MAX_LIFETIME_MS` | No | `1800000` | Database / HikariCP |
| 12 | `JWT_SECRET` | Yes | `dev-secret-…` | Auth / JWT |
| 13 | `JWT_ACCESS_EXPIRATION_MS` | No | `900000` | Auth / JWT |
| 14 | `JWT_REFRESH_EXPIRATION_MS` | No | `604800000` | Auth / JWT |
| 15 | `PORT` | No | `8080` | Server |
| 16 | `CORS_ALLOWED_ORIGINS` | No | `*` | Server |
| 17 | `SMTP_HOST` | No | `localhost` | Mail |
| 18 | `SMTP_PORT` | No | `1025` | Mail |
| 19 | `SMTP_USERNAME` | No | *(empty)* | Mail |
| 20 | `SMTP_PASSWORD` | No | *(empty)* | Mail |
| 21 | `SMTP_AUTH` | No | `false` | Mail |
| 22 | `SMTP_STARTTLS` | No | `false` | Mail |
| 23 | `JOB_EMAIL_DISPATCH_MS` | No | `30000` | Background Jobs |
| 24 | `JOB_ESCROW_RELEASE_CRON` | No | `0 */15 * * * *` | Background Jobs |
| 25 | `JOB_SUBSCRIPTION_EXPIRATION_CRON` | No | `0 0 * * * *` | Background Jobs |
| 26 | `JOB_KPI_AGGREGATION_CRON` | No | `0 */30 * * * *` | Background Jobs |
| 27 | `JOB_FRAUD_SCAN_CRON` | No | `0 */10 * * * *` | Background Jobs |

✦ `DATABASE_URL` and `DB_URL` are alternatives — the app checks `DATABASE_URL` first, then `DB_URL`. Use `DATABASE_URL` on Render (it's injected automatically); use `DB_URL` in your `.env` for local/dev.

---

## Detailed Reference

### 1. Database

| Variable | Type | Default | Description |
|----------|------|---------|-------------|
| `DATABASE_URL` | JDBC URL | — | Full PostgreSQL JDBC connection string. **Render injects this automatically** when a PostgreSQL database is attached. Takes priority over `DB_URL`. |
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/cartwave` | Fallback connection string used when `DATABASE_URL` is not set. Set this in your `.env` for local development against a real PostgreSQL instance. |
| `DB_USERNAME` | String | `cartwave` | PostgreSQL username. Fallback alias: `DB_USER`. |
| `DB_PASSWORD` | String | *(empty)* | PostgreSQL password. **No default — must be set in production.** |

**Important notes:**
- Do **not** include `channel_binding=require` in the JDBC URL when connecting through a PgBouncer-based pooler (e.g. Neon). PgBouncer does not support SCRAM channel binding and connections will fail.
- Recommended URL parameters for Neon: `?sslmode=require&connectTimeout=10&socketTimeout=30&tcpKeepAlive=true`

### 2. Database — HikariCP Connection Pool

These tune the HikariCP connection pool. Defaults are suitable for most deployments.

| Variable | Type | Default | Description |
|----------|------|---------|-------------|
| `DB_CONNECTION_TIMEOUT_MS` | Long (ms) | `30000` | Maximum time (ms) to wait for a connection from the pool. |
| `DB_VALIDATION_TIMEOUT_MS` | Long (ms) | `5000` | Maximum time (ms) for a connection validation check. |
| `DB_INIT_FAIL_TIMEOUT_MS` | Long (ms) | `60000` | Time (ms) before failing startup if no DB connection can be established. |
| `DB_MIN_IDLE` | Integer | `2` | Minimum idle connections kept in the pool. |
| `DB_MAX_POOL_SIZE` | Integer | `10` | Maximum total connections in the pool. For Neon free tier, keep ≤ 5. |
| `DB_KEEPALIVE_MS` | Long (ms) | `300000` | Interval (ms) for keep-alive pings on idle connections (5 min). |
| `DB_MAX_LIFETIME_MS` | Long (ms) | `1800000` | Maximum lifetime (ms) of a connection in the pool (30 min). |

### 3. Auth / JWT

| Variable | Type | Default | Description |
|----------|------|---------|-------------|
| `JWT_SECRET` | String | `dev-secret-dev-secret-dev-secret-dev-secret` | HMAC-SHA signing key for JWT tokens. **Must be at least 256 bits (32+ chars) in production.** Use a cryptographically random value. |
| `JWT_ACCESS_EXPIRATION_MS` | Long (ms) | `900000` | Access token lifetime (default: 15 minutes). |
| `JWT_REFRESH_EXPIRATION_MS` | Long (ms) | `604800000` | Refresh token lifetime (default: 7 days). |

**Security:** The default `JWT_SECRET` is for development only. Always override in production with a strong random secret (64+ characters recommended).

### 4. Server

| Variable | Type | Default | Description |
|----------|------|---------|-------------|
| `PORT` | Integer | `8080` | HTTP port the server listens on. Render sets this automatically. |
| `CORS_ALLOWED_ORIGINS` | String | `*` | Comma-separated list of allowed CORS origins. Set to your frontend's domain in production (e.g. `https://cartwave.app`). |

### 5. Mail (SMTP)

| Variable | Type | Default | Description |
|----------|------|---------|-------------|
| `SMTP_HOST` | String | `localhost` | SMTP server hostname. |
| `SMTP_PORT` | Integer | `1025` | SMTP server port. Default is MailHog/MailPit for local dev. Use `587` for production (TLS). |
| `SMTP_USERNAME` | String | *(empty)* | SMTP authentication username. |
| `SMTP_PASSWORD` | String | *(empty)* | SMTP authentication password. |
| `SMTP_AUTH` | Boolean | `false` | Enable SMTP authentication. Set `true` in production. |
| `SMTP_STARTTLS` | Boolean | `false` | Enable STARTTLS encryption. Set `true` in production. |

**Production example (SendGrid):**
```dotenv
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USERNAME=apikey
SMTP_PASSWORD=SG.xxxxxxxxxxxx
SMTP_AUTH=true
SMTP_STARTTLS=true
```

### 6. Background Jobs

| Variable | Type | Default | Description |
|----------|------|---------|-------------|
| `JOB_EMAIL_DISPATCH_MS` | Long (ms) | `30000` | Interval between email queue dispatch runs (30 seconds). |
| `JOB_ESCROW_RELEASE_CRON` | Cron | `0 */15 * * * *` | Cron for releasing mature escrow payments (every 15 min). |
| `JOB_SUBSCRIPTION_EXPIRATION_CRON` | Cron | `0 0 * * * *` | Cron for checking/expiring subscriptions (every hour). |
| `JOB_KPI_AGGREGATION_CRON` | Cron | `0 */30 * * * *` | Cron for aggregating KPI snapshots (every 30 min). |
| `JOB_FRAUD_SCAN_CRON` | Cron | `0 */10 * * * *` | Cron for running fraud detection scans (every 10 min). |

Cron format is **Spring 6-field**: `second minute hour day month weekday`

---

## Environment Profiles

### Local Development (H2 — no Docker)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

No `.env` or PostgreSQL needed. Uses H2 in-memory with `ddl-auto=create-drop`. See `application-local.yaml`.

### Development (PostgreSQL via .env)

```bash
# Edit .env with your Neon or local PostgreSQL credentials, then:
./mvnw spring-boot:run
```

Loads the default `application.yaml` profile. Flyway runs migrations against the configured database.

### Production (Render)

Set these environment variables in the Render dashboard:

| Variable | Value |
|----------|-------|
| `DATABASE_URL` | *(auto-injected by Render when PostgreSQL is attached)* |
| `DB_PASSWORD` | Your Neon/PostgreSQL password |
| `JWT_SECRET` | A strong 64+ char random string |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend-domain.com` |
| `SMTP_HOST` | `smtp.sendgrid.net` (or your provider) |
| `SMTP_PORT` | `587` |
| `SMTP_USERNAME` | Your SMTP username |
| `SMTP_PASSWORD` | Your SMTP password |
| `SMTP_AUTH` | `true` |
| `SMTP_STARTTLS` | `true` |

All other variables have safe defaults and are optional.

---

## Sample `.env` File

```dotenv
# Database
DB_URL=jdbc:postgresql://localhost:5432/cartwave?sslmode=prefer
DB_USERNAME=cartwave
DB_PASSWORD=changeme

# Auth
JWT_SECRET=REPLACE_WITH_A_CRYPTOGRAPHICALLY_RANDOM_64_CHAR_STRING_HERE_XX

# Server
PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000

# Mail (MailHog for local dev)
SMTP_HOST=localhost
SMTP_PORT=1025
SMTP_USERNAME=
SMTP_PASSWORD=
SMTP_AUTH=false
SMTP_STARTTLS=false
```
