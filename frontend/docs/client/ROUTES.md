# Route Map - AntiPanel Frontend

## Overview

This document describes the routing configuration for the AntiPanel frontend application. All routes use lazy loading for optimal bundle sizes and faster initial load times.

## Public Routes

| Route | Component | Guard | Lazy | Description |
|-------|-----------|-------|------|-------------|
| `/home` | Home | - | Yes | Landing page with hero, features, services |
| `/login` | Login | guestGuard | Yes | User authentication |
| `/register` | Register | guestGuard, pendingChangesGuard | Yes | User registration with form protection |
| `/terms` | Terms | - | Yes | Terms of service page |
| `/support` | Support | - | Yes | Support contact page |

## Protected Routes

| Route | Component | Guard | Resolver | Lazy | Description |
|-------|-----------|-------|----------|------|-------------|
| `/dashboard` | Dashboard | authGuard | - | Yes | Main dashboard with stats and order creation |
| `/wallet` | Wallet | authGuard | - | Yes | Wallet management and payment options |
| `/orders` | Orders | authGuard | - | Yes | Order history with filtering and pagination |
| `/orders/:id` | OrderDetail | authGuard | orderResolver | Yes | Individual order details |

## Special Routes

| Route | Behavior |
|-------|----------|
| `/` | Redirects to `/dashboard` (authenticated) or `/home` (guest) via rootGuard |
| `**` | 404 Not Found page |

## Development Routes

| Route | Component | Description |
|-------|-----------|-------------|
| `/cliente` | Cliente | Client demo page |
| `/style-guide` | StyleGuide | Design system documentation |
| `/formarray` | FormArrayDemo | FormArray demonstration |

## Guards

### authGuard (CanActivateFn)
- Requires authentication
- Redirects to `/login` with `returnUrl` query param if unauthenticated
- Location: `src/app/core/guards/auth.guard.ts`

### guestGuard (CanActivateFn)
- Only allows unauthenticated users
- Redirects to `/dashboard` if authenticated
- Location: `src/app/core/guards/guest.guard.ts`

### rootGuard (CanActivateFn)
- Smart redirect based on authentication state
- Authenticated users → `/dashboard`
- Guest users → `/home`
- Location: `src/app/core/guards/root.guard.ts`

### pendingChangesGuard (CanDeactivateFn)
- Prompts before navigation if form has unsaved changes
- Implements `HasUnsavedChanges` interface
- Location: `src/app/core/guards/pending-changes.guard.ts`

## Resolvers

### orderResolver (ResolveFn)
- Preloads order data for `/orders/:id` route
- Redirects to `/orders` with error state if order not found
- Location: `src/app/core/resolvers/order.resolver.ts`

## Route Configuration Features

### Preloading Strategy
- Uses `PreloadAllModules` to preload lazy chunks after initial load
- Configured in `app.config.ts`

### In-Memory Scrolling
- Anchor scrolling enabled for in-page navigation
- Configured via `withInMemoryScrolling({ anchorScrolling: 'enabled' })`

### Breadcrumb Support
- Routes define `data.breadcrumb` for breadcrumb navigation
- BreadcrumbService builds crumbs from activated route tree
- BreadcrumbComponent displays hierarchical navigation

## Route Data

```typescript
// Example route with full configuration
{
  path: 'orders/:id',
  loadComponent: () => import('./pages/order-detail/order-detail').then(m => m.OrderDetail),
  canActivate: [authGuard],
  resolve: { order: orderResolver },
  data: { breadcrumb: 'Order Details' }
}
```

## URL Parameters

| Parameter | Route | Description |
|-----------|-------|-------------|
| `:id` | `/orders/:id` | Order ID for detail view |

## Query Parameters

| Parameter | Route | Description |
|-----------|-------|-------------|
| `returnUrl` | `/login` | URL to redirect to after login |
| `registered` | `/login` | Shows success message after registration |
| `sessionExpired` | `/login` | Shows session expired message |
| `service` | `/dashboard` | Pre-selects service for quick ordering |

## Architecture Notes

1. **Standalone Components**: All components are standalone, no NgModules
2. **Functional Guards**: Using CanActivateFn and CanDeactivateFn (class-based deprecated)
3. **Functional Resolvers**: Using ResolveFn (class-based deprecated)
4. **Lazy Loading**: All page components are lazy loaded
5. **Type Safety**: Routes are typed with proper interfaces
