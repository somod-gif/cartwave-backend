# Frontend And API Spec

This document maps the current MVP backend to frontend behavior. For the exact route list, use `API_OVERVIEW.md` and runtime OpenAPI at `/api-docs`.

## Base assumptions
- Base URL: `http://localhost:8080/api/v1`
- Auth uses bearer tokens returned by `/auth/login` and `/auth/refresh`
- Auth endpoints return raw token payloads
- All resource endpoints return `ApiResponse<T>`
- Store context lives in the JWT `storeId`

## Personas
- `BUSINESS_OWNER`: creates stores, manages catalog, staff, subscriptions, billing, and dashboards
- `STAFF`: store-scoped backoffice access
- `CUSTOMER`: storefront profile, cart, checkout, and order history
- `ADMIN` and `SUPER_ADMIN`: broader operational views

## Recommended frontend surfaces
- Public storefront
  - Store landing page from `GET /public/stores/{slug}`
  - Product grid from `GET /public/stores/{slug}/products`
- Customer account
  - Profile from `GET /customers/me`
  - Cart CRUD from `/cart`
  - Checkout from `POST /checkout`
  - Orders from `GET /orders`
- Owner and staff console
  - Stores from `/stores`
  - Products from `/products`
  - Orders from `/orders`
  - Billing from `/billing/transactions`
  - Staff from `/staff`
  - Plans from `/subscriptions/plans` and `/subscriptions/current`
- Platform dashboards
  - Admin dashboard from `/admin/dashboard`
  - Super admin dashboard from `/super-admin/dashboard`

## Frontend flow notes
- Owner onboarding
  - Register as `BUSINESS_OWNER`
  - Login
  - Create store
  - Add products
  - Optionally add staff and change subscription
- Customer onboarding
  - Select a store
  - Register as `CUSTOMER` with `storeId`
  - Login with the same `storeId`
  - Browse public catalog
  - Add items to cart, checkout, then initiate payment
- Order operations
  - Customers can only view their own orders
  - Owners and staff can list store orders and patch status

## Client implementation guidance
- Keep `storeId` in app state for customer login and owner multi-store switching
- Use a shared API client that adds `Authorization: Bearer <token>`
- Handle `ApiResponse.success`, `ApiResponse.message`, and `ApiResponse.error`
- Do not assume guest checkout exists in MVP
