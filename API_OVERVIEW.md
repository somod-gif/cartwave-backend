# API Overview (`/api/v1`)

## Auth
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`

## Store
- `POST /stores`
- `GET /stores`
- `GET /stores/{id}`
- `PUT /stores/{id}`
- `DELETE /stores/{id}`

## Product
- `POST /products`
- `GET /products`
- `GET /products/{id}`
- `PUT /products/{id}`
- `DELETE /products/{id}`

## Orders
- `POST /orders`
- `GET /orders`
- `GET /orders/{id}`
- `PATCH /orders/{id}/status`

## Billing/Payment
- `POST /billing/transactions`
- `POST /payments/initiate`
- `POST /payments/webhook`

## Subscription
- `POST /subscriptions`
- `GET /subscriptions/store/{storeId}`

## Email Queue
- `POST /emails/enqueue`

## Sample Payloads
### Login
```json
{"email":"owner@shop.com","password":"StrongPassword123"}
```
### Enqueue Email
```json
{"recipient":"buyer@shop.com","subject":"Order Confirmed","templateName":"order_confirmation","payloadJson":"{\"orderId\":\"123\"}"}
```
