# CartWave — Frontend Specification

This document defines the type of frontend to build, the pages/screens needed, technology recommendations, and how each screen maps to the backend API.

---

## What to Build

CartWave needs **two frontend applications** (or one app with two modes):

### 1. Seller Dashboard (Admin Panel)
A management interface for **Business Owners, Admins, and Staff** to run their store.

### 2. Customer Storefront
A public-facing shopping experience for **Customers** to browse, cart, checkout, and track orders.

Both share the same backend API (`/api/v1/*`) and the same JWT auth system.

---

## Technology Recommendations

| Concern | Recommendation | Why |
|---------|---------------|-----|
| Framework | **Next.js** (React) or **Nuxt.js** (Vue) | SSR for storefront SEO, SPA for dashboard speed |
| Styling | Tailwind CSS + shadcn/ui (React) or Vuetify (Vue) | Rapid UI development, consistent design |
| State management | Zustand (React) or Pinia (Vue) | Lightweight, stores auth tokens + cart state |
| HTTP client | Axios | Interceptors for token refresh, clean API layer |
| Forms | React Hook Form + Zod (React) or VeeValidate + Yup (Vue) | Matches backend validation structure |
| Auth storage | `localStorage` for tokens | Stateless JWT — no cookies needed |
| Routing | File-based (Next/Nuxt) | Natural page structure |
| Deployment | Vercel (Next.js) or Netlify | Pairs well with Spring Boot backend on Railway/Render |

### Alternative: Single SPA

If you prefer one codebase, use **React Router** or **Vue Router** with route guards that redirect based on user role:
- `/dashboard/*` → requires `BUSINESS_OWNER`, `ADMIN`, or `STAFF`
- `/store/{slug}/*` → public + `CUSTOMER`

---

## Auth Implementation

### Token Flow

```
Register → Login → Store tokens → Attach to requests → Auto-refresh on 401
```

### Required Auth State

```typescript
interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: {
    id: string;          // UUID
    email: string;
    firstName: string;
    lastName: string;
    role: 'SUPER_ADMIN' | 'ADMIN' | 'BUSINESS_OWNER' | 'STAFF' | 'CUSTOMER';
    storeId: string;     // UUID — current store context
  } | null;
  isAuthenticated: boolean;
}
```

### API Client Setup

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

// Attach token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auto-refresh on 401
api.interceptors.response.use(
  res => res,
  async err => {
    if (err.response?.status === 401 && !err.config._retry) {
      err.config._retry = true;
      const { data } = await api.post('/auth/refresh', {
        refreshToken: localStorage.getItem('refreshToken'),
      });
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      err.config.headers.Authorization = `Bearer ${data.accessToken}`;
      return api(err.config);
    }
    return Promise.reject(err);
  }
);
```

### Route Guards

```typescript
// Example: Next.js middleware or React Router wrapper
function requireRole(allowedRoles: string[]) {
  const user = getAuthUser();
  if (!user) redirect('/login');
  if (!allowedRoles.includes(user.role)) redirect('/unauthorized');
}
```

---

## App 1: Seller Dashboard

### Pages & Screen Map

| Page | Route | API Endpoints | Role Required |
|------|-------|--------------|---------------|
| **Login** | `/login` | `POST /auth/login` | — |
| **Register** | `/register` | `POST /auth/register` (role=BUSINESS_OWNER) | — |
| **Dashboard Home** | `/dashboard` | `GET /dashboard/metrics`, `GET /admin/dashboard` | BUSINESS_OWNER, ADMIN, STAFF |
| **Store Setup** | `/dashboard/store/create` | `POST /stores` | BUSINESS_OWNER |
| **Store Settings** | `/dashboard/store/settings` | `GET /stores/{id}`, `PUT /stores/{id}` | BUSINESS_OWNER, ADMIN |
| **Products List** | `/dashboard/products` | `GET /products` | BUSINESS_OWNER, ADMIN, STAFF |
| **Add Product** | `/dashboard/products/new` | `POST /products` | BUSINESS_OWNER, ADMIN, STAFF |
| **Edit Product** | `/dashboard/products/{id}` | `GET /products/{id}`, `PUT /products/{id}` | BUSINESS_OWNER, ADMIN, STAFF |
| **Orders List** | `/dashboard/orders` | `GET /orders` | BUSINESS_OWNER, ADMIN, STAFF |
| **Order Detail** | `/dashboard/orders/{id}` | `GET /orders/{id}`, `PATCH /orders/{id}/status` | BUSINESS_OWNER, ADMIN, STAFF |
| **Staff List** | `/dashboard/staff` | `GET /staff` | BUSINESS_OWNER, ADMIN |
| **Add Staff** | `/dashboard/staff/add` | `POST /staff` | BUSINESS_OWNER, ADMIN |
| **Billing** | `/dashboard/billing` | `GET /billing/transactions` | BUSINESS_OWNER, ADMIN, STAFF |
| **Subscription** | `/dashboard/subscription` | `GET /subscriptions/current`, `GET /subscriptions/plans`, `POST /subscriptions/change` | BUSINESS_OWNER, ADMIN |
| **Customer List** | `/dashboard/customers` | (Use orders to infer customers) | BUSINESS_OWNER, ADMIN, STAFF |
| **Customer Detail** | `/dashboard/customers/{id}` | `GET /customers/{id}` | BUSINESS_OWNER, ADMIN, STAFF |

### Dashboard Home Widgets

Display data from `GET /dashboard/metrics` + `GET /admin/dashboard`:

```
┌─────────────────┬──────────────────┬─────────────────┬──────────────────┐
│  Total Orders   │    Revenue       │ Active Customers│ Pending Escrow   │
│     150         │   $45,230.50     │      89         │      12          │
├─────────────────┴──────────────────┴─────────────────┴──────────────────┤
│                                                                         │
│  Product Count: 45    │  Pending Orders: 12   │  Low Stock: 3          │
│  Staff Count: 5       │  Fraud Flags: 1       │                        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Product Form Fields

```
Name*            [text input]
Description      [textarea]
Price*           [number input, decimal]
Cost Price       [number input, decimal]
Stock            [number input, integer]
Low Stock Alert  [number input, integer]
SKU              [text input]
Status           [dropdown: ACTIVE, INACTIVE, ARCHIVED]
Category         [text input]
Image URL        [text/file upload input]
Additional Images [text/file upload, comma-separated]
Attributes       [JSON editor or key-value pairs]
```

### Order Status Update UI

Dropdown or buttons showing the valid transitions:
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
                                              ↕
                                    CANCELLED / REFUNDED
```
Uses: `PATCH /orders/{id}/status` with `{ "status": "CONFIRMED" }`

### Subscription Page

Show current plan details + upgrade/downgrade options:
```
Current Plan: FREE ($0/mo)
Products: 5/20 used | Staff: 1/1 used
Payments: ❌ Not available | Custom Domain: ❌

[Upgrade to STARTER - $19/mo]  [Upgrade to PRO - $99/mo]  [Enterprise - $499/mo]
```

---

## App 2: Customer Storefront

### Pages & Screen Map

| Page | Route | API Endpoints | Role Required |
|------|-------|--------------|---------------|
| **Store Home** | `/{slug}` | `GET /public/stores/{slug}` | — (public) |
| **Product Catalog** | `/{slug}/products` | `GET /public/stores/{slug}/products` | — (public) |
| **Product Detail** | `/{slug}/products/{id}` | (from catalog data) | — (public) |
| **Customer Register** | `/{slug}/register` | `POST /customers/register` | — (public) |
| **Customer Login** | `/{slug}/login` | `POST /auth/login` (with storeId) | — |
| **Cart** | `/{slug}/cart` | `GET /cart`, `POST/PATCH/DELETE /cart/items/*` | CUSTOMER |
| **Checkout** | `/{slug}/checkout` | `POST /checkout` | CUSTOMER |
| **Payment** | `/{slug}/payment` | `POST /payments/initiate`, `POST /payments/confirm` | CUSTOMER |
| **Order Confirmation** | `/{slug}/order/{id}` | `GET /orders/{id}` | CUSTOMER |
| **Order History** | `/{slug}/orders` | `GET /orders` | CUSTOMER |
| **My Profile** | `/{slug}/profile` | `GET /customers/me` | CUSTOMER |

### Store Home Layout

```
┌─────────────────────────────────────────────────────┐
│  [Logo]  Store Name                    [Login] [Cart]│
│  Store description text                              │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │ Product 1│  │ Product 2│  │ Product 3│          │
│  │ [Image]  │  │ [Image]  │  │ [Image]  │          │
│  │ $29.99   │  │ $39.99   │  │ $19.99   │          │
│  │[Add Cart]│  │[Add Cart]│  │[Add Cart]│          │
│  └──────────┘  └──────────┘  └──────────┘          │
│                                                      │
└─────────────────────────────────────────────────────┘
```

### Cart Page

```
┌─────────────────────────────────────────────────────┐
│ Your Cart                                            │
├─────────────────────────────────────────────────────┤
│ Product Name       Qty [- 2 +]    $59.98    [Remove]│
│ Another Product    Qty [- 1 +]    $39.99    [Remove]│
├─────────────────────────────────────────────────────┤
│                              Subtotal:      $99.97   │
│                              Total:         $99.97   │
│                                                      │
│                              [Proceed to Checkout →] │
└─────────────────────────────────────────────────────┘
```

### Checkout Form

```
Delivery Address*   [textarea]
Email               [email input]
Phone               [tel input]
Notes               [textarea]
Payment Method      [dropdown: CARD, BANK_TRANSFER, etc.]
Payment Provider    [dropdown: STRIPE, PAYSTACK, etc.]

[Place Order]
```

### Payment Flow (Frontend)

```
1. User clicks "Place Order" → POST /checkout → get orderId + transactionId
2. Show payment page → POST /payments/initiate with orderId
3. Redirect to external payment provider OR show card form
4. On return/callback → POST /payments/confirm with transactionId + status
5. Show order confirmation page with order details
```

---

## App 3: Super Admin Panel (Optional)

| Page | Route | API Endpoints |
|------|-------|--------------|
| **Platform Dashboard** | `/admin` | `GET /super-admin/dashboard` |
| **System Stats** | `/admin/stats` | `GET /super-admin/system-stats` |

Shows: total stores, total users, total orders, platform-wide revenue.

---

## Error Handling (Frontend)

Every API error returns:

```json
{
  "success": false,
  "message": "Error message",
  "error": {
    "errorCode": "VALIDATION_ERROR",
    "errorMessage": "Validation failed",
    "statusCode": 400,
    "fieldErrors": { "email": "must not be blank" }
  }
}
```

### Implementation Pattern

```typescript
try {
  const { data } = await api.post('/auth/register', formData);
  if (data.success) {
    // Handle success — data.data contains the result
  }
} catch (err) {
  const error = err.response?.data?.error;
  if (error?.fieldErrors) {
    // Map field errors to form inputs
    Object.entries(error.fieldErrors).forEach(([field, msg]) => {
      setFieldError(field, msg as string);
    });
  } else {
    // Show general error toast
    showToast(error?.errorMessage || 'Something went wrong');
  }
}
```

### HTTP Status → UI Action

| Status | Frontend Action |
|--------|----------------|
| 200/201/202 | Success — update UI |
| 400 | Show validation errors on form fields |
| 401 | Attempt token refresh → if fails, redirect to login |
| 403 | Show "Access Denied" message |
| 404 | Show "Not Found" page |
| 500 | Show generic error toast |

---

## Data Types Reference

All IDs are **UUID strings** (e.g., `"550e8400-e29b-41d4-a716-446655440000"`).

Timestamps come in two formats:
- `createdAt`, `updatedAt` → ISO 8601 string (`"2026-03-08T10:00:00Z"`)
- `completedAt`, `releaseAt`, `hiredAt`, `startDate`, `endDate` → Unix epoch milliseconds (`1709856000000`)

Money values are **numbers with decimal precision** (e.g., `29.99`, `45230.50`).

---

## Recommended File Structure

```
src/
├── api/
│   ├── client.ts              # Axios instance + interceptors
│   ├── auth.ts                # login, register, refresh
│   ├── stores.ts              # store CRUD
│   ├── products.ts            # product CRUD
│   ├── cart.ts                # cart operations
│   ├── checkout.ts            # checkout
│   ├── orders.ts              # order management
│   ├── payments.ts            # payment flow
│   ├── staff.ts               # staff management
│   ├── subscriptions.ts       # subscription management
│   ├── billing.ts             # billing transactions
│   └── dashboard.ts           # dashboard metrics
├── stores/                    # State management
│   ├── authStore.ts
│   ├── cartStore.ts
│   └── ...
├── pages/                     # Route pages
│   ├── dashboard/             # Seller dashboard pages
│   └── [slug]/                # Storefront pages
├── components/
│   ├── common/                # Shared components
│   ├── dashboard/             # Dashboard-specific
│   └── storefront/            # Storefront-specific
├── guards/
│   └── authGuard.ts           # Route protection
├── types/
│   └── api.ts                 # TypeScript interfaces matching DTOs
└── utils/
    ├── formatters.ts          # Currency, date formatting
    └── validators.ts          # Form validation schemas
```

---

## TypeScript Interfaces (Key DTOs)

```typescript
// Auth
interface LoginRequest {
  email: string;
  password: string;
  storeId?: string;
}

interface JwtAuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

// Store
interface StoreDTO {
  id: string;
  name: string;
  slug: string;
  description?: string;
  country?: string;
  currency?: string;
  subscriptionPlan?: string;
  isActive: boolean;
  logoUrl?: string;
  bannerUrl?: string;
  websiteUrl?: string;
  businessAddress?: string;
  businessRegistrationNumber?: string;
  businessPhoneNumber?: string;
  businessEmail?: string;
  ownerId: string;
  createdAt: string;
  updatedAt: string;
}

// Product
interface ProductDTO {
  id: string;
  storeId: string;
  name: string;
  description?: string;
  price: number;
  costPrice?: number;
  stock: number;
  lowStockThreshold?: number;
  sku?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'ARCHIVED' | 'OUT_OF_STOCK';
  imageUrl?: string;
  images?: string;
  category?: string;
  attributes?: string;
}

// Cart
interface CartDTO {
  id: string;
  storeId: string;
  customerId: string;
  status: 'ACTIVE' | 'CHECKED_OUT' | 'ABANDONED';
  subtotal: number;
  total: number;
  currency: string;
  items: CartItemDTO[];
}

interface CartItemDTO {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

// Order
interface OrderDTO {
  id: string;
  storeId: string;
  orderNumber: string;
  customerId: string;
  totalAmount: number;
  shippingCost?: number;
  taxAmount?: number;
  discountAmount?: number;
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'REFUNDED';
  paymentStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'REFUNDED' | 'PARTIALLY_REFUNDED';
  deliveryAddress?: string;
  customerEmail?: string;
  customerPhoneNumber?: string;
  notes?: string;
  completedAt?: number;
  releaseAt?: number;
  createdAt: string;
}

// Checkout
interface CheckoutRequest {
  deliveryAddress: string;
  customerEmail?: string;
  customerPhoneNumber?: string;
  notes?: string;
  paymentMethod?: string;
  paymentProvider?: string;
}

interface CheckoutResponse {
  orderId: string;
  orderNumber: string;
  billingTransactionId: string;
  transactionId: string;
  totalAmount: number;
  orderStatus: string;
  paymentStatus: string;
}

// Payment
interface PaymentResponse {
  transactionId: string;
  status: string;
  paymentProvider: string;
  paymentMethod: string;
}

// Subscription
interface SubscriptionPlanDTO {
  id: string;
  name: string;
  description?: string;
  productLimit: number;
  staffLimit: number;
  paymentsEnabled: boolean;
  customDomainEnabled: boolean;
  price: number;
  active: boolean;
}

// API Wrapper
interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: {
    errorCode: string;
    errorMessage: string;
    statusCode: number;
    timestamp: string;
    path: string;
    fieldErrors?: Record<string, string>;
  };
}
```
