# CartWave Backend - Quick Start Guide

## Prerequisites

- **Java 25** (or Java 21+ that supports Spring Boot 4.0.2)
- **Maven 3.8+**
- **PostgreSQL 12+** (or Neon serverless account)
- **Git**

## Environment Setup

### 1. Install Java 25

**Windows (using Chocolatey):**
```bash
choco install openjdk25
```

**macOS (using Homebrew):**
```bash
brew install openjdk@25
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get install openjdk-25-jdk
```

### 2. Verify Installation

```bash
java -version
mvn -version
```

## Project Setup

### 1. Clone Repository

```bash
cd ~/Desktop
git clone https://github.com/somod-gif/CartWave.git
cd CartWave/cartwave-backend
```

### 2. Configure Environment Variables

**Windows (PowerShell):**
```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/cartwave"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_password"
$env:JWT_SECRET = "your-super-secret-key-change-this-in-production-at-least-256-bits"
```

**macOS/Linux (Bash):**
```bash
export DB_URL="jdbc:postgresql://localhost:5432/cartwave"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_password"
export JWT_SECRET="your-super-secret-key-change-this-in-production-at-least-256-bits"
```

### 3. Build Project

```bash
mvn clean install
```

## Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

Or:

```bash
java -jar target/cartwave-backend-0.0.1-SNAPSHOT.jar
```

### Expected Output

```
2025-02-18 10:30:45 - Starting CartWave Backend...
2025-02-18 10:30:50 - Flyway migrations completed
2025-02-18 10:30:55 - Security bean initialized
2025-02-18 10:31:00 - Tomcat started on port(s): 8080 (http)
2025-02-18 10:31:01 - CartWave Backend started successfully!
```

### Access the API

**Base URL:** `http://localhost:8080/api/v1`

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
```

## First API Call

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "emailVerified": false
  }
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900000
  }
}
```

### 3. Get Current User (Authenticated)

```bash
curl http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

## Project Structure Overview

```
cartwave-backend/
├── src/
│   ├── main/
│   │   ├── java/com/cartwave/
│   │   │   ├── auth/              # Authentication
│   │   │   ├── user/              # User management
│   │   │   ├── store/             # Store management
│   │   │   ├── product/           # Product catalog
│   │   │   ├── order/             # Order management
│   │   │   ├── subscription/      # Subscriptions
│   │   │   ├── billing/           # Billing
│   │   │   ├── admin/             # Admin operations
│   │   │   ├── superadmin/        # Super admin
│   │   │   ├── security/          # JWT & Security
│   │   │   ├── tenant/            # Multi-tenancy
│   │   │   ├── common/            # Shared utilities
│   │   │   ├── exception/         # Exception handling
│   │   │   └── config/            # Configuration
│   │   └── resources/
│   │       ├── application.yaml           # Configuration
│   │       └── db/migration/              # Flyway SQL
│   └── test/                      # Test classes
├── pom.xml                        # Maven configuration
├── ARCHITECTURE.md                # Detailed architecture
├── IMPLEMENTATION_SUMMARY.md      # What's been done
└── QUICK_START.md                 # This file
```

## Common Development Tasks

### Create a New Entity

```java
@Entity
@Table(name = "your_table")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class YourEntity extends BaseEntity {
    // Your fields
}
```

### Create a Repository

```java
@Repository
public interface YourRepository extends JpaRepository<YourEntity, UUID> {
    @Query("SELECT ... WHERE deleted = false")
    Optional<YourEntity> findById(...);
}
```

### Create a Service

```java
@Service
@RequiredArgsConstructor
@Transactional
public class YourService {
    private final YourRepository repository;
    
    public YourDTO getById(UUID id) {
        var item = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(...));
        return mapper.toDTO(item);
    }
}
```

### Create a Controller

```java
@RestController
@RequestMapping("/your-endpoint")
@RequiredArgsConstructor
public class YourController {
    private final YourService service;
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<YourDTO>> getById(@PathVariable UUID id) {
        YourDTO dto = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
```

## Debugging

### View SQL Logs

Set in `application.yaml`:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### View Request/Response

```yaml
logging:
  level:
    org.springframework.web: DEBUG
```

## Common Issues & Solutions

### Issue: `Database connection refused`
**Solution:** Verify DB_URL, DB_USERNAME, DB_PASSWORD are correct and database is running

### Issue: `Invalid JWT token`
**Solution:** Ensure JWT_SECRET is set and consistent across restarts

### Issue: `Port 8080 already in use`
**Solution:** 
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# macOS/Linux
lsof -i :8080
kill -9 <PID>
```

### Issue: `Flyway migration error`
**Solution:** Check `db/migration/*.sql` files and ensure they're valid SQL

### Issue: `Tenant context not set`
**Solution:** Ensure `storeId` is present in JWT claims and filtered by TenantContext

## Testing Endpoints

### Using cURL

```bash
# Create a store
curl -X POST http://localhost:8080/api/v1/stores \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Store","slug":"my-store","country":"Nigeria","currency":"NGN"}'

# Get products
curl http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer YOUR_TOKEN"

# Create an order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"orderNumber":"ORD-001","totalAmount":5000}'
```

### Using Postman

1. Import the base collection
2. Set environment variables:
   - `base_url`: http://localhost:8080/api/v1
   - `access_token`: (from login response)
3. Use templates provided

## Database Access

### Direct PostgreSQL Connection

```bash
psql -h localhost -U postgres -d cartwave
```

### Useful Queries

```sql
-- Check users
SELECT id, email, role, status FROM users WHERE deleted = false;

-- Check stores
SELECT id, name, slug, is_active FROM stores WHERE deleted = false;

-- Check orders
SELECT id, order_number, status, payment_status FROM orders WHERE deleted = false;

-- Reset database (development only)
TRUNCATE TABLE users, stores, products, orders CASCADE;
```

## Next Steps

1. ✅ Read `ARCHITECTURE.md` for detailed design
2. ✅ Read `IMPLEMENTATION_SUMMARY.md` for what's done
3. ✅ Explore module implementations (auth, product, order, etc.)
4. ✅ Add business logic services
5. ✅ Implement request validation
6. ✅ Add API documentation (Swagger)
7. ✅ Write tests
8. ✅ Set up CI/CD pipeline

## Resources

- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **JWT Guide**: https://jwt.io/introduction
- **MapStruct**: https://mapstruct.org
- **Flyway**: https://flywaydb.org

## Getting Help

### Check Logs

```bash
tail -f target/logs/cartwave.log
```

### Debug Mode

```bash
mvn spring-boot:run -Ddebug
```

### Common Patterns

Review existing implementations:
- Auth module for security patterns
- Product module for simple CRUD
- Order module for complex workflows
- Admin module for RBAC patterns

---

**Ready to build? Start with `mvn spring-boot:run` and make your first API call! 🚀**
