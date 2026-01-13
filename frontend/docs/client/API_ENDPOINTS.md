# API Endpoints Catalog - AntiPanel Frontend

## Overview

This document catalogs all API endpoints consumed by the AntiPanel frontend application. The API follows RESTful conventions and returns JSON responses.

**Base URL:** `${environment.apiUrl}` (configured per environment)

## HTTP Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          HTTP Request Flow                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Component/Service                                                       │
│       │                                                                  │
│       ▼                                                                  │
│  ┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐       │
│  │ HttpClient  │────▶│  Interceptors    │────▶│  Backend API    │       │
│  └─────────────┘     │  1. auth         │     └─────────────────┘       │
│                      │  2. loading      │              │                 │
│                      │  3. logging      │              │                 │
│                      └──────────────────┘              │                 │
│                             │                          │                 │
│       ┌─────────────────────┘                          │                 │
│       │                                                │                 │
│       ▼                                                ▼                 │
│  ┌─────────────┐                              ┌─────────────────┐       │
│  │   Request   │                              │    Response     │       │
│  │  + Bearer   │                              │   JSON/Error    │       │
│  │    Token    │                              └─────────────────┘       │
│  └─────────────┘                                       │                 │
│                                                        │                 │
│                      ┌────────────────────────────────┘                 │
│                      │                                                   │
│                      ▼                                                   │
│              ┌───────────────┐                                          │
│              │ Error Handler │──▶ retry(5xx) or throwError(4xx)         │
│              └───────────────┘                                          │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

## Authentication Endpoints

**Service:** `AuthService` (`src/app/core/services/auth.service.ts`)
**Base Path:** `/api/auth`

| Method | Endpoint | Description | Request Body | Response | Auth |
|--------|----------|-------------|--------------|----------|------|
| POST | `/auth/login` | User login | `LoginRequest` | `AuthResponse` | No |
| POST | `/auth/register` | User registration | `RegisterRequest` | `UserResponse` | No |
| POST | `/auth/refresh` | Refresh access token | `{ refreshToken }` | `AuthResponse` | No |
| POST | `/auth/logout` | User logout | - | `void` | Yes |

### TypeScript Interfaces

```typescript
// Request Types
interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  email: string;
  password: string;
  role: 'USER' | 'ADMIN' | 'SUPPORT';
}

// Response Types
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

interface UserSummary {
  id: number;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPPORT';
  balance: number;
}

interface UserResponse {
  id: number;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPPORT';
  department: string | null;
  balance: number;
  isBanned: boolean;
  bannedReason: string | null;
  lastLoginAt: string | null;
  loginCount: number;
  createdAt: string;
  updatedAt: string;
}
```

## User Endpoints

**Service:** `UserService` (`src/app/core/services/user.service.ts`)
**Base Path:** `/api/users/me`

| Method | Endpoint | Description | Query Params | Response | Auth |
|--------|----------|-------------|--------------|----------|------|
| GET | `/users/me/statistics` | Dashboard stats | - | `UserStatisticsResponse` | Yes |

### TypeScript Interfaces

```typescript
interface UserStatisticsResponse {
  totalOrders: number;
  pendingOrders: number;
  completedOrders: number;
  ordersThisMonth: number;
  balance: number;
}
```

## Order Endpoints

**Service:** `OrderService` (`src/app/core/services/order.service.ts`)
**Base Path:** `/api/orders`

| Method | Endpoint | Description | Query Params | Request Body | Response | Auth |
|--------|----------|-------------|--------------|--------------|----------|------|
| POST | `/orders` | Create order | - | `OrderCreateRequest` | `OrderResponse` | Yes |
| GET | `/orders` | List orders (paginated) | `page`, `size` | - | `PageResponse<OrderResponse>` | Yes |
| GET | `/orders/{id}` | Get order by ID | - | - | `OrderResponse` | Yes |
| GET | `/orders/active` | Get active orders | - | - | `OrderResponse[]` | Yes |
| GET | `/orders/refillable` | Get refillable orders | - | - | `OrderResponse[]` | Yes |

### TypeScript Interfaces

```typescript
// Request Types
interface OrderCreateRequest {
  serviceId: number;
  target: string;
  quantity: number;
  idempotencyKey?: string;  // Auto-generated if not provided
}

// Response Types
type OrderStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'IN_PROGRESS'
  | 'PARTIAL'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'REFUNDED'
  | 'FAILED';

interface OrderResponse {
  id: number;
  user: OrderUserSummary;
  serviceId: number;
  serviceName: string;
  target: string;
  quantity: number;
  startCount: number | null;
  remains: number;
  status: OrderStatus;
  progress: number;
  totalCharge: number;
  isRefillable: boolean;
  refillDays: number;
  refillDeadline: string | null;
  canRequestRefill: boolean;
  createdAt: string;
  completedAt: string | null;
  updatedAt: string;
}

// Generic Pagination
interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
```

## Invoice/Payment Endpoints

**Service:** `InvoiceService` (`src/app/core/services/invoice.service.ts`)
**Base Path:** `/api/invoices`

| Method | Endpoint | Description | Query Params | Request Body | Response | Auth |
|--------|----------|-------------|--------------|--------------|----------|------|
| POST | `/invoices` | Create invoice | - | `InvoiceCreateRequest` | `InvoiceResponse` | Yes |
| GET | `/invoices` | List invoices (paginated) | `page`, `size` | - | `PageResponse<InvoiceResponse>` | Yes |
| GET | `/invoices/{id}` | Get invoice by ID | - | - | `InvoiceResponse` | Yes |
| POST | `/invoices/{id}/check-status` | Check payment status | - | - | `InvoiceResponse` | Yes |
| GET | `/invoices/pending` | Get pending invoices | - | - | `InvoiceResponse[]` | Yes |

### TypeScript Interfaces

```typescript
// Request Types
interface InvoiceCreateRequest {
  processorId: number;
  amount: number;
  currency?: string;  // Default: 'USD'
}

// Response Types
type InvoiceStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'FAILED'
  | 'EXPIRED'
  | 'CANCELLED';

interface InvoiceResponse {
  id: number;
  user: InvoiceUserSummary;
  processor: PaymentProcessorSummary;
  processorInvoiceId: string | null;
  amount: number;
  fee: number;
  netAmount: number;
  currency: string;
  status: InvoiceStatus;
  paymentUrl: string | null;
  paidAt: string | null;
  createdAt: string;
  updatedAt: string;
}

interface PaymentProcessorSummary {
  id: number;
  name: string;
  code: string;
  minAmount: number;
  maxAmount: number | null;
  feePercentage: number;
  feeFixed: number;
}
```

## Public/Catalog Endpoints

**Service:** `CatalogService` (`src/app/services/catalog.service.ts`)
**Base Path:** `/api/public`

| Method | Endpoint | Description | Query Params | Response | Auth |
|--------|----------|-------------|--------------|----------|------|
| GET | `/public/categories/with-counts` | List categories | - | `CategoryWithCount[]` | No |
| GET | `/public/categories/{id}/services` | Services by category | - | `Service[]` | No |
| GET | `/public/categories/{id}/service-types` | Service types by category | - | `ServiceType[]` | No |
| GET | `/public/categories/{id}/types/{typeId}/services` | Services by type | - | `Service[]` | No |
| GET | `/public/services/{id}` | Get service by ID | - | `Service` | No |
| GET | `/public/services/search` | Search services | `categoryId`, `serviceTypeId`, `quality`, `speed`, `search` | `{ content: Service[] }` | No |
| GET | `/public/payment-processors` | List payment processors | - | `PaymentProcessor[]` | No |

### TypeScript Interfaces

```typescript
interface CategoryWithCount {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  iconUrl: string | null;
  sortOrder: number;
  isActive: boolean;
  serviceCount: number;
}

interface ServiceType {
  id: number;
  categoryId: number;
  name: string;
  slug: string;
  sortOrder: number;
  isActive: boolean;
}

interface Service {
  id: number;
  categoryId: number;
  serviceTypeId: number;
  name: string;
  description: string | null;
  rate: number;
  minQuantity: number;
  maxQuantity: number;
  isRefillable: boolean;
  refillDays: number;
  quality: 'PREMIUM' | 'STANDARD' | 'ECONOMY';
  speed: 'INSTANT' | 'FAST' | 'SLOW';
  isActive: boolean;
}

interface PaymentProcessor {
  id: number;
  name: string;
  code: string;
  website: string;
  minAmount: number;
  maxAmount: number | null;
  feePercentage: number;
  feeFixed: number;
  isActive: boolean;
  sortOrder: number;
}
```

## Error Handling

### Standard Error Response

```typescript
interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: FieldError[];
}

interface FieldError {
  field: string;
  message: string;
  rejectedValue: unknown;
}
```

### HTTP Status Codes

| Code | Meaning | Typical Cause |
|------|---------|---------------|
| 200 | OK | Successful request |
| 201 | Created | Resource created |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Invalid/expired token |
| 402 | Payment Required | Insufficient balance |
| 403 | Forbidden | Access denied |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate (email/order) |
| 500 | Server Error | Backend error |
| 503 | Service Unavailable | Provider unavailable |

### Retry Strategy

```typescript
// Implemented in services using RxJS retry()
private readonly retryConfig = {
  count: 2,
  delay: (error: HttpErrorResponse, retryCount: number) => {
    // Only retry on 5xx errors (server errors) or network errors
    if (error.status >= 500 || error.status === 0) {
      return timer(1000 * retryCount); // Exponential backoff: 1s, 2s
    }
    // Don't retry on client errors (4xx)
    throw error;
  }
};
```

## Interceptors

The application uses three HTTP interceptors (configured in `app.config.ts`):

### 1. Auth Interceptor
**File:** `src/app/core/interceptors/auth.interceptor.ts`

- Adds `Authorization: Bearer <token>` header to authenticated requests
- Skips public endpoints (`/api/public/*`, `/api/auth/login`, `/api/auth/register`)
- Handles 401 responses with automatic token refresh

### 2. Loading Interceptor
**File:** `src/app/core/interceptors/loading.interceptor.ts`

- Triggers loading state via `LoadingService`
- Shows/hides loading indicator during HTTP requests
- Supports debouncing to prevent flicker

### 3. Logging Interceptor
**File:** `src/app/core/interceptors/logging.interceptor.ts`

- Logs all HTTP requests in development mode
- Includes request method, URL, headers, timing
- Logs response status and errors

## Service Locations

| Service | File Path | Purpose |
|---------|-----------|---------|
| AuthService | `src/app/core/services/auth.service.ts` | Authentication |
| UserService | `src/app/core/services/user.service.ts` | User statistics |
| OrderService | `src/app/core/services/order.service.ts` | Order management |
| InvoiceService | `src/app/core/services/invoice.service.ts` | Payments |
| CatalogService | `src/app/services/catalog.service.ts` | Service catalog |
| TokenService | `src/app/core/services/token.service.ts` | Token storage |
| TokenRefreshService | `src/app/core/services/token-refresh.service.ts` | Token refresh |
