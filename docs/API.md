# CartWave API Reference

Base URL: `http://localhost:8080/api/v1`

Interactive docs: `/swagger-ui/index.html`

## Authentication

All protected endpoints require a Bearer token in the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```

---

## Auth

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register a new business owner |
| POST | `/auth/login` | Public | Login, returns access + refresh tokens |
| POST | `/auth/refresh` | Public | Refresh access token |
| POST | `/auth/logout` | Auth | Invalidate refresh token |

---

## Stores

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/stores` | BUSINESS_OWNER | Create a store |
| GET | `/stores/me` | Auth | Get authenticated user's store |
| GET | `/stores/{id}/public` | **Public** | Get public store page |
| GET | `/stores/{storeId}/products` | **Public** | List published products for store |
| PUT | `/stores/{id}` | BUSINESS_OWNER | Update store details |
| PUT | `/stores/{id}/branding` | BUSINESS_OWNER | Update logo, banner, brand color, template |
| PUT | `/stores/{id}/domain` | BUSINESS_OWNER | Set custom domain (PRO+) |
| PUT | `/stores/{id}/seo` | BUSINESS_OWNER | Update meta title, description, keywords |

---

## Products

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/products` | BUSINESS_OWNER | Create product |
| GET | `/products` | Auth | List store products |
| GET | `/products/{id}` | Auth | Get product by ID |
| PUT | `/products/{id}` | BUSINESS_OWNER | Update product |
| DELETE | `/products/{id}` | BUSINESS_OWNER | Soft-delete product |
| POST | `/products/{id}/images` | BUSINESS_OWNER | Upload product images (multipart) |
| DELETE | `/products/{id}/images/{imageUrl}` | BUSINESS_OWNER | Remove a product image |
| PUT | `/products/{id}/publish` | BUSINESS_OWNER | Toggle product published state |

---

## Orders

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/orders` | Auth | Place an order |
| GET | `/orders/{orderId}` | Auth | Get order by ID |
| GET | `/orders/store/{storeId}` | BUSINESS_OWNER | All orders for a store |
| GET | `/orders/customer/{customerId}` | Auth | All orders by customer |
| PATCH | `/orders/{orderId}/status` | BUSINESS_OWNER | Update order status |
| PUT | `/orders/{orderId}/status` | BUSINESS_OWNER | Update order status (alias) |

---

## Escrow

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/escrow/store/{storeId}` | BUSINESS_OWNER | List escrow transactions for store |
| POST | `/escrow/{escrowId}/release` | ADMIN | Manually release escrow |
| POST | `/escrow/{escrowId}/dispute` | Auth | Raise a dispute |
| PUT | `/escrow/dispute/{disputeId}/resolve` | ADMIN | Resolve a dispute |

---

## Marketing (Coupons)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/marketing/stores/{storeId}/coupons` | BUSINESS_OWNER | Create coupon |
| GET | `/marketing/stores/{storeId}/coupons` | BUSINESS_OWNER | List coupons |
| DELETE | `/marketing/stores/{storeId}/coupons/{couponId}` | BUSINESS_OWNER | Delete coupon |
| POST | `/marketing/coupons/validate` | **Public** | Validate coupon code at checkout |

---

## Subscriptions

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/subscriptions/plans` | **Public** | List all available plans |
| GET | `/subscriptions/me` | Auth | Get current store subscription |
| POST | `/subscriptions/subscribe` | BUSINESS_OWNER | Subscribe to a plan |
| POST | `/subscriptions/cancel` | BUSINESS_OWNER | Cancel active subscription |

---

## Checkout

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/checkout` | Auth | Initiate checkout (creates order + escrow hold) |

---

## Payments

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/payments/initiate` | Auth | Initiate payment |
| POST | `/payments/confirm` | Auth | Confirm payment |
| POST | `/payments/webhook` | **Public** | Payment provider webhook |

---

## Dashboard

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/dashboard/metrics` | Auth | Store owner dashboard metrics |
| GET | `/dashboard/super-admin` | SUPER_ADMIN | Platform-level overview |

---

## Admin

All admin endpoints require `ADMIN` or `SUPER_ADMIN` role.

| Method | Path | Description |
|---|---|---|
| GET | `/admin/dashboard` | Admin dashboard (store context) |
| GET | `/admin/stats` | Platform stats alias |
| GET | `/admin/users` | List all users |
| GET | `/admin/users/{userId}` | Get user by ID |
| PUT | `/admin/users/{userId}/suspend` | Suspend a user |
| PUT | `/admin/users/{userId}/activate` | Activate a user |
| GET | `/admin/revenue` | Platform revenue summary |
| GET | `/admin/health` | Platform health indicators |
| POST | `/admin/plans` | Create subscription plan |
| DELETE | `/admin/plans/{planId}/deactivate` | Deactivate plan |

---

## Subscription Plans

| Plan | Price (NGN/month) | Products | Custom Domain |
|---|---|---|---|
| STARTER | Free | 5 | No |
| BASIC | ₦5,000 | 10 | No |
| GROWTH | ₦15,000 | 20 | No |
| PRO | ₦30,000 | 100 | Yes |
| ENTERPRISE | Custom | Unlimited | Yes |

---

## Error Responses

All errors follow the `ApiResponse` envelope:
```json
{
  "success": false,
  "message": "Human-readable error",
  "error": {
    "code": "MACHINE_READABLE_CODE",
    "details": "..."
  }
}
```

Common error codes: `UNAUTHORIZED`, `FORBIDDEN`, `RESOURCE_NOT_FOUND`, `VALIDATION_ERROR`, `BUSINESS_RULE_VIOLATION`, `PRODUCT_LIMIT_REACHED`, `COUPON_CODE_EXISTS`, `ESCROW_ALREADY_RELEASED`.
