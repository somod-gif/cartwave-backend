CartWave — Frontend & API Integration Specification

Last updated: 2026-02-20

Purpose
- Compile the backend capabilities, API surface, authentication contract, and a detailed frontend specification (pages, user flows, components, testing guidance) into a single reference document inside the repo.

1. High-level overview
- Backend: multi-tenant e‑commerce SaaS (stores, products, orders, subscriptions, billing, staff, users) built with Spring Boot, Spring Data JPA, PostgreSQL, Flyway migrations, JWT auth, and OpenAPI (springdoc).
- Frontend target: SPA to manage stores (owner/staff), customer storefront and checkout, and admin dashboards.

2. Backend capabilities (summary)
- Entities: User, Store, Staff, Product, Order, Subscription, BillingTransaction, plus enums for statuses/roles.
- Persistence: PostgreSQL via Spring Data JPA; migrations via Flyway in src/main/resources/db/migration.
- Auth: JWT tokens, Spring Security; endpoints under /api/v1/auth. SecurityConfig permits Swagger & auth & health endpoints and protects all other routes.
- Docs: OpenAPI available at /v3/api-docs and Swagger UI at /swagger-ui.html.
- Utilities: MapStruct mappers, validation annotations, BCrypt password hashing.

3. Authentication contract
- Register
  - POST /api/v1/auth/register
  - Body: {"email","password","role"}
  - Response: 201 Created + User object
- Login
  - POST /api/v1/auth/login
  - Body: {"email","password"}
  - Response: 200 OK + {"token":"<JWT>"}
- Protected endpoints require Authorization: Bearer <JWT>

4. Core API surface (examples — use runtime Swagger for exact models)
- Public
  - GET /api/v1/health
  - POST /api/v1/auth/register
  - POST /api/v1/auth/login
  - GET /v3/api-docs
  - GET /swagger-ui.html
- Stores
  - GET /stores
  - POST /stores
  - GET /stores/{id}
  - PUT /stores/{id}
- Products
  - GET /api/v1/products
  - POST /api/v1/products
  - GET /api/v1/products/{id}
  - PUT /api/v1/products/{id}
  - DELETE /api/v1/products/{id}
- Orders
  - GET /orders
  - POST /orders
  - GET /orders/{id}
  - PUT /orders/{id}
- Subscriptions
  - GET /subscriptions
  - POST /subscriptions
  - PUT /subscriptions/{id}
- Billing
  - GET /billing/transactions
  - POST /billing/transactions
- Staff/User/Admin endpoints exist — consult Swagger.

5. Data model notes (frontend-relevant fields)
- User: id, email, firstName, lastName, role, status, profilePictureUrl, bio, createdAt, updatedAt
- Store: id, name, slug, ownerId, subscriptionPlan, logoUrl, bannerUrl, websiteUrl, businessAddress
- Product: id, storeId, name, description, price, costPrice, stock, sku, status, imageUrl, images, category, attributes
- Order: id, storeId, customerId, orderNumber, status, paymentStatus, totalAmount, deliveryAddress, completedAt
- Subscription: id, storeId, planName, billingCycle, amount, autoRenewal, startDate, endDate
- BillingTransaction: id, storeId, transactionId, amount, currency, status, paymentMethod, paymentProvider, transactionDetails, failureReason

6. Frontend architecture (recommended)
- Framework: React + TypeScript (recommended) or Vue 3 / Angular.
- App shell: Vite or Next.js (if SSR needed). Use Vite+React for fastest dev feedback.
- UI library: Tailwind CSS or Material UI / AntDesign.
- State/data fetching: React Query or Redux Toolkit Query (RTK Query) for server state; small local state with context/hooks.
- HTTP client: axios with an auth interceptor to attach Authorization header; central API client module.
- Auth storage: Prefer secure HttpOnly cookie + Refresh tokens if backend supports. If not, localStorage for access token (with refresh in memory) but be careful of XSS.
- Routing: React Router.
- i18n: react-i18next (if multiple locales planned).

7. Frontend pages and components
- Public / Storefront
  - Home / Store list (if multi-store public) — ProductCard, SearchBar, Filters
  - Product listing page — ProductList, Pagination, Sorting, Filters
  - Product detail page — Image gallery, Attributes, Add to Cart
  - Cart & Checkout pages — CartSummary, AddressForm, PaymentForm
  - Auth pages: Login, Register, Forgot password (if implemented)
- Customer account
  - Profile page — ProfileForm
  - Orders list & detail — OrderList, OrderDetail
  - Subscriptions — SubscriptionList, SubscriptionDetail
- Store Owner dashboard
  - Dashboard Overview — KPI cards (revenue, orders, low stock), Charts (sales)
  - Products management — ProductTable (server-side pagination), ProductForm (create/edit), ImageUploader
  - Orders management — OrdersTable, OrderActionButtons (mark shipped, refund)
  - Store settings — StoreForm (branding, business info)
  - Staff management — StaffTable, InviteStaffForm, PermissionSelector
  - Billing & Subscriptions — BillingTable, SubscriptionManagement
- Super Admin
  - Global user management, store management
- Shared components
  - Header/Nav (store selector), Footer, ProtectedRoute wrapper, Modal, Toast/Notifications, DataTable, ConfirmDialog

8. User flows (step-by-step)
- Owner onboarding
  1. Register -> Login
  2. Create a Store -> Fill branding & business info
  3. Add products or import via CSV (if supported)
  4. Invite staff -> assign roles
  5. Monitor orders & billing
- Customer buy flow
  1. Browse products -> add to cart
  2. Checkout -> submit shipping + payment -> call create order & create billing transaction
  3. View order confirmation & tracking
- Staff processing
  1. Login -> view assigned/pending orders -> update order status
- Subscription lifecycle
  1. Owner subscribes to a plan -> subscription created -> billing transactions recorded for renewals

9. API-to-UI mapping (high level)
- AuthController: Login/Register -> Login page, Register page
- ProductController: Product pages & management
- StoreController: Store settings & selection
- OrderController: Orders list/detail UI
- SubscriptionController: Subscriptions UI
- BillingController: Transactions UI

10. Testing & QA
- Swagger UI: use runtime Swagger to inspect routes and sample payloads.
- Postman: create a collection that includes:
  - Health
  - Register, Login
  - Create store, Create product
  - Create order (checkout flow)
  - Retrieve orders, update order status
  - Retrieve billing transactions
- Add Postman environment with variables: baseUrl (http://localhost:8080), authToken
- Use automated tests: Jest + React Testing Library for frontend; Cypress or Playwright for E2E against a running backend.

11. Local dev and environment
- Backend env vars used: DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET
- Frontend env vars: VITE_API_BASE_URL or NEXT_PUBLIC_API_URL set to http://localhost:8080
- For local testing we temporarily set spring.jpa.hibernate.ddl-auto=update. Revert to validate in staging/production.

12. Security & production notes
- Store JWT refresh tokens in HttpOnly cookies and use CSRF tokens or sameSite protection.
- Use HTTPS and strong CORS settings in production.
- Make Flyway migrations part of a deployment job; prefer running migrations separately in CI/CD and keep ddl-auto=validate.

13. Deliverables & next steps (pick one or more)
- I can generate a Postman collection (JSON) and add it to DOCUMENTATION/postman_collection.json inside this repo.
- I can scaffold a minimal React+TS starter in a new /frontend folder with:
  - Login/Register, Product list, API client integration using axios + React Query.
- I can create a more detailed per-endpoint mapping spreadsheet (CSV/Markdown) with request/response examples from controllers.

Tell me which of the deliverables above you want next. If you want the Postman collection, I will generate it now and add it to DOCUMENTATION/. If you want the React starter, I will scaffold it in /frontend/ and wire a few pages to the running API.
