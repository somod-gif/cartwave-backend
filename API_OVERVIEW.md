# API Overview

Base path: `/api/v1`

## Response contract
- Auth endpoints return `JwtAuthResponse`.
- All other endpoints return `ApiResponse<T>`.

## Public endpoints
- `GET /health`
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /public/stores/{slug}`
- `GET /public/stores/{slug}/products`
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/api-docs`

## Auth rules
- Public registration supports only `CUSTOMER` and `BUSINESS_OWNER`.
- Customer registration requires `storeId`.
- Login may include `storeId`.
- Login fails without `storeId` when the account can access multiple stores.

## Owner, staff, and admin endpoints
- `GET /stores`
- `POST /stores`
- `GET /stores/{storeId}`
- `PUT /stores/{storeId}`
- `DELETE /stores/{storeId}`
- `GET /products`
- `POST /products`
- `GET /products/{id}`
- `PUT /products/{id}`
- `DELETE /products/{id}`
- `GET /orders`
- `GET /orders/{orderId}`
- `POST /orders`
- `PUT /orders/{orderId}`
- `PATCH /orders/{orderId}/status`
- `GET /billing/transactions`
- `GET /subscriptions/current`
- `GET /subscriptions/plans`
- `POST /subscriptions/change`
- `GET /staff`
- `POST /staff`
- `DELETE /staff/{staffId}`
- `GET /admin/dashboard`
- `GET /admin/stats`
- `GET /super-admin/dashboard`
- `GET /super-admin/system-stats`

## Customer endpoints
- `GET /customers/me`
- `GET /cart`
- `POST /cart/items`
- `PATCH /cart/items/{itemId}`
- `DELETE /cart/items/{itemId}`
- `POST /checkout`
- `GET /orders`
- `GET /orders/{orderId}`
- `POST /payments/initiate`
- `POST /payments/webhook`

## Utility endpoint
- `POST /emails/enqueue`

## Request samples

### Register owner
```json
{
  "email": "owner@example.com",
  "password": "Password123!",
  "role": "BUSINESS_OWNER",
  "firstName": "Ada",
  "lastName": "Owner"
}
```

### Register customer
```json
{
  "email": "buyer@example.com",
  "password": "Password123!",
  "role": "CUSTOMER",
  "firstName": "Tobi",
  "lastName": "Buyer",
  "storeId": "00000000-0000-0000-0000-000000000000"
}
```

### Login
```json
{
  "email": "buyer@example.com",
  "password": "Password123!",
  "storeId": "00000000-0000-0000-0000-000000000000"
}
```

### Create store
```json
{
  "name": "CartWave Demo",
  "slug": "cartwave-demo",
  "description": "Demo storefront",
  "country": "NG",
  "currency": "NGN"
}
```

### Add cart item
```json
{
  "productId": "00000000-0000-0000-0000-000000000000",
  "quantity": 2
}
```

### Checkout
```json
{
  "deliveryAddress": "14 Admiralty Way, Lekki, Lagos",
  "customerEmail": "buyer@example.com",
  "customerPhoneNumber": "+2348000000000",
  "paymentMethod": "CARD",
  "paymentProvider": "INTERNAL"
}
```
