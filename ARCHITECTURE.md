# CartWave Backend - Production-Grade Architecture

## Overview

CartWave is a production-grade, enterprise-scale E-commerce backend platform built with Spring Boot, designed specifically for Africa's market. This backend implements clean architecture, multi-tenancy, and security-first principles.

## Tech Stack

- **Java 25** - Latest LTS version for performance and latest features
- **Spring Boot 4.0.2** - Latest Spring Boot version
- **Spring Security 6** - Modern security framework
- **Spring Data JPA** - ORM and data access
- **PostgreSQL (Neon Serverless)** - Fully managed PostgreSQL
- **JWT (JJWT)** - Custom JWT authentication with access + refresh tokens
- **MapStruct** - Type-safe DTO mapping
- **Flyway** - Database migrations
- **HikariCP** - Connection pooling optimized for serverless
- **Lombok** - Reduce boilerplate
- **Maven** - Build management

## Architecture

### Layered Architecture

```
Controller Layer (REST Endpoints)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Entity Layer (Database Models)
```

### Multi-Tenancy Strategy

- **Shared Schema, Store-based Isolation**
- Every entity belongs to a store (storeId)
- TenantContext using ThreadLocal stores storeId per request
- JWT contains storeId, extracted in JwtAuthenticationFilter
- All repository queries automatically filtered by storeId
- Future-ready for schema-based multi-tenancy

### Security Model

**Roles:**
- `SUPER_ADMIN` - Platform administrator
- `ADMIN` - Store administrator
- `BUSINESS_OWNER` - Store owner
- `STAFF` - Store staff member
- `CUSTOMER` - Customer

**Authentication Flow:**
1. User registers or logs in
2. Server validates credentials using BCrypt
3. Access token (15 min) and refresh token (7 days) generated
4. Client sends Access token in Authorization header
5. JwtAuthenticationFilter validates and extracts claims
6. TenantContext set for the request
7. SecurityContext populated with user details and roles

## Project Structure

```
src/main/java/com/cartwave/
├── config/                 # Configuration classes
│   ├── AuditAwareImpl.java        # JPA auditing implementation
│   ├── JpaAuditingConfig.java    # JPA auditing configuration
│   └── WebConfig.java            # Web configuration
├── security/               # Security-related classes
│   ├── config/
│   │   └── SecurityConfig.java   # Spring Security configuration
│   ├── dto/
│   │   └── JwtClaims.java        # JWT claims DTO
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java  # JWT filter
│   └── service/
│       └── JwtService.java       # JWT operations
├── tenant/                 # Multi-tenancy support
│   └── TenantContext.java        # ThreadLocal tenant management
├── common/                 # Shared utilities
│   ├── dto/
│   │   ├── ApiResponse.java      # Standard API response wrapper
│   │   ├── ErrorDetails.java     # Error response details
│   │   └── PageableResponse.java # Pagination wrapper
│   └── entity/
│       └── BaseEntity.java       # Base entity with auditing
├── exception/              # Exception handling
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   ├── TenantAccessDeniedException.java
│   ├── ValidationException.java
│   └── GlobalExceptionHandler.java  # Centralized exception handler
├── auth/                   # Authentication module
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── mapper/
├── user/                   # User management
│   ├── entity/
│   ├── repository/
│   └── ... (other modules follow same pattern)
├── store/                  # Store management
├── staff/                  # Staff management
├── product/                # Product catalog
├── order/                  # Order management
├── subscription/           # Subscription management
├── billing/                # Billing and transactions
├── admin/                  # Admin operations
└── superadmin/             # Super admin operations

src/main/resources/
├── application.yaml         # Configuration
└── db/migration/
    ├── V1__init.sql        # Initial schema
    ├── V2__add_indexes.sql # Indexes
    └── V3__add_constraints.sql  # Constraints
```

## Database Schema

### Core Tables

**users**
- id (UUID, PK)
- email (UNIQUE)
- password (BCrypt hashed)
- firstName, lastName
- phoneNumber
- role (enum)
- status (enum)
- emailVerified
- lastLoginAt
- createdAt, updatedAt, deleted

**stores**
- id (UUID, PK)
- name, slug (UNIQUE)
- ownerId (FK to users)
- country, currency
- subscriptionPlan
- isActive
- Audit fields

**staff**
- id, userId (FK), storeId (FK)
- permissionLevel, role, status
- Audit fields

**products**
- id, storeId (FK)
- name, description, price, costPrice
- stock, sku (index)
- status, category
- Audit fields

**orders**
- id, storeId (FK), customerId (FK)
- orderNumber (UNIQUE)
- totalAmount, shippingCost, taxAmount, discountAmount
- status, paymentStatus
- Audit fields

**subscriptions**
- id, storeId (FK, UNIQUE)
- planName, amount, billingCycle
- startDate, endDate, renewalDate
- autoRenewal
- Audit fields

**billing_transactions**
- id, storeId (FK)
- transactionId (UNIQUE)
- amount, currency, status
- paymentMethod, paymentProvider
- Audit fields

### Indexes

- **Single column**: email, slug, store_id, user_id, status, deleted
- **Composite**: (user_id, store_id), (store_id, status)
- **Partial**: WHERE deleted = false for soft delete queries

## API Endpoints

### Base Path: `/api/v1`

**Auth Module:**
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/refresh` - Token refresh
- `GET /auth/me` - Current user info

**Store Module:**
- `GET /stores/{storeId}` - Get store
- `PUT /stores/{storeId}` - Update store

**Product Module:**
- `GET /products/{productId}` - Get product
- `GET /products` - List products (paginated)
- `POST /products` - Create product
- `PUT /products/{productId}` - Update product

**Order Module:**
- `GET /orders/{orderId}` - Get order
- `GET /orders` - List orders (paginated)
- `POST /orders` - Create order
- `PUT /orders/{orderId}` - Update order

**Subscription Module:**
- `GET /subscriptions/current` - Get current subscription

**Billing Module:**
- `GET /billing/transactions` - Get transactions (paginated)

**Admin Module:**
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/stats` - Admin statistics

**Super Admin Module:**
- `GET /super-admin/dashboard` - Super admin dashboard
- `GET /super-admin/system-stats` - System statistics

## Configuration

### Application Properties

```yaml
# Database (Neon)
spring.datasource.url: ${DB_URL}
spring.datasource.username: ${DB_USERNAME}
spring.datasource.password: ${DB_PASSWORD}

# HikariCP (Serverless optimized)
maximum-pool-size: 5
minimum-idle: 1
idle-timeout: 10000
connection-timeout: 20000
max-lifetime: 30000

# JWT
jwt.secret: ${JWT_SECRET}
jwt.access-token-expiration: 900000    # 15 minutes
jwt.refresh-token-expiration: 604800000  # 7 days

# JPA
ddl-auto: validate  # Never use update/create in production
```

## Key Features

### 1. **BaseEntity with Auditing**
- All entities extend BaseEntity
- Automatic createdAt, updatedAt tracking
- Soft delete with `deleted` field
- UUID primary keys

### 2. **Global Exception Handling**
- Centralized error handling via @RestControllerAdvice
- Standard error response format
- Proper HTTP status codes
- Detailed error information for debugging

### 3. **JWT Authentication**
- Access + Refresh token pattern
- Claims include userId, email, role, storeId
- Token validation before every request
- Automatic tenant context extraction

### 4. **Multi-Tenancy**
- Transparent tenant isolation
- TenantContext for thread-safe tenant management
- Auto-filtering queries by storeId
- Support for future schema-based multi-tenancy

### 5. **DTO Pattern**
- Zero entity exposure to API clients
- MapStruct for type-safe mapping
- Separation of concerns
- Consistent API contract

### 6. **RBAC (Role-Based Access Control)**
- Method-level security with @PreAuthorize
- Fine-grained role-based access
- Public, protected, and admin endpoints

### 7. **Database Migrations**
- Flyway for schema versioning
- Initial schema, indexes, and constraints
- Repeatable, versioned migrations
- Production-safe deployment

## Development Setup

### Prerequisites
- Java 25
- Maven 3.8+
- PostgreSQL (or Neon account)

### Local Development

1. **Clone repository**
   ```bash
   git clone <repo-url>
   cd cartwave-backend
   ```

2. **Configure environment variables**
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/cartwave
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_password
   export JWT_SECRET=your-256-bit-secret-key-minimum
   ```

3. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access API**
   - Base URL: `http://localhost:8080/api/v1`
   - Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html` (if added)

## Deployment Considerations

### Production Checklist

- [ ] Use strong JWT secret (256+ bits)
- [ ] Enable HTTPS only
- [ ] Configure CORS properly
- [ ] Set up logging and monitoring
- [ ] Use Read Replicas for Neon PostgreSQL
- [ ] Implement rate limiting
- [ ] Add request validation
- [ ] Set up backup strategy
- [ ] Configure connection pooling for expected load
- [ ] Monitor HikariCP metrics
- [ ] Use environment-specific configurations
- [ ] Implement API versioning strategy

### Scaling Considerations

- Stateless design enables horizontal scaling
- Multi-tenancy allows efficient resource sharing
- TenantContext ensures proper isolation
- Database indexes optimized for common queries
- HikariCP for connection efficiency

## Security Best Practices

1. **Password Hashing**: BCrypt with 12 rounds
2. **JWT Storage**: Secure cookies (HttpOnly, Secure flags)
3. **CORS**: Restrict to known domains
4. **Rate Limiting**: Implement per IP

 and per user
5. **Input Validation**: Use @Valid annotations
6. **SQL Injection**: Use parameterized queries (JPA handles this)
7. **XSS Protection**: Content-Security-Policy headers
8. **CSRF**: Implement token-based CSRF protection

## Future Enhancements

- [ ] Audit log system
- [ ] Feature flags system
- [ ] Webhook system
- [ ] Country tax configuration
- [ ] Currency conversion service
- [ ] Embedded finance engine
- [ ] Logistics partner integration
- [ ] API rate limiting
- [ ] Request/Response caching
- [ ] Elasticsearch integration
- [ ] Message queue (RabbitMQ/Kafka)
- [ ] Microservices architecture

## Troubleshooting

### Connection Issues
- Verify Neon credentials
- Check HikariCP pool configuration
- Review PostgreSQL logs

### JWT Not Working
- Verify JWT secret is set
- Check token format (Bearer prefix)
- Validate token not expired

### Tenant Context Issues
- Ensure JwtAuthenticationFilter is registered
- Verify storeId is present in JWT
- Check TenantContext isn't cleared prematurely

## Support & Documentation

- Spring Boot: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- JJWT: https://github.com/jwtk/jjwt
- Flyway: https://flywaydb.org
- MapStruct: https://mapstruct.org

---

**Built with production standards. Ready for Africa scale.**
