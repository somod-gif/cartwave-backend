# CartWave Backend

Multi-tenant SaaS e-commerce platform backend built with Spring Boot 3 and PostgreSQL.

## Quick Start

```bash
# 1. Copy environment variables
cp .env.example .env
# Edit .env with your values

# 2. Build & run
./mvnw spring-boot:run

# 3. API docs available at
# http://localhost:8080/swagger-ui/index.html
```

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.4.2 |
| Database | PostgreSQL (Neon) |
| Migrations | Flyway |
| Auth | JWT (stateless) |
| Object Storage | AWS S3 (SDK v2) |
| Email | Spring Mail + async queue |
| Docs | Springdoc OpenAPI 3 |
| Build | Maven |
| Container | Docker |

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for system design.

## API Reference

See [API.md](API.md) for all endpoints.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history.

## Running with Docker

```bash
docker build -t cartwave-backend .
docker run -p 8080:8080 --env-file .env cartwave-backend
```

## Database Migrations

Flyway runs automatically on startup.  Migration files are in `src/main/resources/db/migration/`.

| Version | Description |
|---|---|
| V1 | Initial schema — all core tables |
| V2 | Feature upgrades — store builder, escrow V2, coupons, email sentAt |
