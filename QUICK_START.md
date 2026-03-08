# Quick Start

## Prerequisites
- Java 21
- PostgreSQL
- Docker optional for the PostgreSQL integration test

## Configure
PowerShell example:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/cartwave"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "postgres"
$env:JWT_SECRET = "change-this-secret-change-this-secret"
```

## Start the app
```powershell
.\mvnw.cmd spring-boot:run
```

Useful URLs:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/api/v1/health`

## Owner flow

### 1. Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"owner@example.com\",\"password\":\"Password123!\",\"role\":\"BUSINESS_OWNER\",\"firstName\":\"Ada\",\"lastName\":\"Owner\"}"
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"owner@example.com\",\"password\":\"Password123!\"}"
```

### 3. Create a store
```bash
curl -X POST http://localhost:8080/api/v1/stores ^
  -H "Authorization: Bearer OWNER_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"CartWave Demo\",\"slug\":\"cartwave-demo\",\"country\":\"NG\",\"currency\":\"NGN\"}"
```

## Customer flow

### 1. Register against a store
```bash
curl -X POST http://localhost:8080/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"buyer@example.com\",\"password\":\"Password123!\",\"role\":\"CUSTOMER\",\"storeId\":\"STORE_ID\"}"
```

### 2. Login with tenant context
```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"buyer@example.com\",\"password\":\"Password123!\",\"storeId\":\"STORE_ID\"}"
```

### 3. Add to cart
```bash
curl -X POST http://localhost:8080/api/v1/cart/items ^
  -H "Authorization: Bearer CUSTOMER_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"productId\":\"PRODUCT_ID\",\"quantity\":2}"
```

### 4. Checkout
```bash
curl -X POST http://localhost:8080/api/v1/checkout ^
  -H "Authorization: Bearer CUSTOMER_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"deliveryAddress\":\"14 Admiralty Way, Lekki, Lagos\",\"customerEmail\":\"buyer@example.com\",\"paymentMethod\":\"CARD\",\"paymentProvider\":\"INTERNAL\"}"
```

### 5. Initiate payment
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate ^
  -H "Authorization: Bearer CUSTOMER_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"orderId\":\"ORDER_ID\",\"paymentMethod\":\"CARD\",\"paymentProvider\":\"INTERNAL\"}"
```

## Seeded super admin
- Email: `superadmin@cartwave.local`
- Password: `Password123!`

## Run tests
```powershell
.\mvnw.cmd test
```

The suite includes a Testcontainers PostgreSQL integration test. It skips automatically if Docker is not available.
