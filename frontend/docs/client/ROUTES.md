# Route Map - AntiPanel Frontend

## Overview

This document describes the routing configuration for the AntiPanel frontend application. All routes use lazy loading for optimal bundle sizes and faster initial load times. The application follows Angular 21 best practices with functional guards, resolvers, and standalone components.

## Route Architecture Diagram

```
Root (/)
├── Public Routes
│   ├── /home ─────────────────── Home (Landing page)
│   ├── /login ────────────────── Login (guestGuard)
│   ├── /register ─────────────── Register (guestGuard, pendingChangesGuard)
│   ├── /terms ────────────────── Terms
│   └── /support ──────────────── Support
│
├── Protected Routes (authGuard)
│   ├── /dashboard ────────────── Dashboard
│   ├── /wallet ───────────────── Wallet
│   └── /orders ───────────────── OrdersLayout
│       ├── (index) ───────────── Orders (list)
│       └── /:id ──────────────── OrderDetail (orderResolver)
│
├── Development Routes
│   ├── /cliente ──────────────── ClienteLayout
│   │   ├── (index) ───────────── Cliente (main demo)
│   │   ├── /http ─────────────── HttpDemos (5.4 criteria)
│   │   └── /state ────────────── StateDemos (6.6 criteria)
│   ├── /style-guide ──────────── StyleGuide
│   └── /formarray ────────────── FormArrayDemo
│
└── Special Routes
    ├── / ─────────────────────── rootGuard (smart redirect)
    └── ** ────────────────────── NotFound (404)
```

## Complete Route Table

### Public Routes

| Route | Component | Guard | Lazy | Description |
|-------|-----------|-------|------|-------------|
| `/home` | Home | - | Yes | Landing page with hero, features, services |
| `/login` | Login | guestGuard | Yes | User authentication |
| `/register` | Register | guestGuard, pendingChangesGuard | Yes | User registration with form protection |
| `/terms` | Terms | - | Yes | Terms of service page |
| `/support` | Support | - | Yes | Support contact page |

### Protected Routes

| Route | Component | Guard | Resolver | Lazy | Description |
|-------|-----------|-------|----------|------|-------------|
| `/dashboard` | Dashboard | authGuard | - | Yes | Main dashboard with stats and order creation |
| `/wallet` | Wallet | authGuard | - | Yes | Wallet management and payment options |
| `/orders` | OrdersLayout | authGuard | - | Yes | Parent layout for orders section |
| `/orders` (child) | Orders | (inherited) | - | Yes | Order history with filtering and pagination |
| `/orders/:id` | OrderDetail | (inherited) | orderResolver | Yes | Individual order details |

### Development/Demo Routes

| Route | Component | Guard | Lazy | Description |
|-------|-----------|-------|------|-------------|
| `/cliente` | ClienteLayout | - | Yes | Parent layout for cliente demos |
| `/cliente` (child) | Cliente | - | Yes | Main client demo page |
| `/cliente/http` | HttpDemos | - | Yes | HTTP patterns demo (FormData, HttpParams, HttpHeaders) |
| `/cliente/state` | StateDemos | - | Yes | State management demo (Polling, Signals) |
| `/style-guide` | StyleGuide | - | Yes | Design system documentation |
| `/formarray` | FormArrayDemo | - | Yes | FormArray demonstration |

### Special Routes

| Route | Behavior |
|-------|----------|
| `/` | Redirects to `/dashboard` (authenticated) or `/home` (guest) via rootGuard |
| `**` | 404 Not Found page |

## Nested Routes (Child Routes)

The application uses nested routing for complex sections, enabling hierarchical navigation while sharing layout components.

### Orders Section (`/orders`)

```typescript
{
  path: 'orders',
  loadComponent: () => import('./pages/orders/orders-layout').then(m => m.OrdersLayout),
  canActivate: [authGuard],
  data: { breadcrumb: 'Orders' },
  children: [
    {
      path: '',
      loadComponent: () => import('./pages/orders/orders').then(m => m.Orders)
    },
    {
      path: ':id',
      loadComponent: () => import('./pages/order-detail/order-detail').then(m => m.OrderDetail),
      resolve: { order: orderResolver },
      data: { breadcrumb: 'Order Details' }
    }
  ]
}
```

**Layout Component Pattern:**
```typescript
// orders-layout.ts
@Component({
  selector: 'app-orders-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrdersLayout {}
```

### Cliente Demo Section (`/cliente`)

```typescript
{
  path: 'cliente',
  loadComponent: () => import('./pages/cliente/cliente-layout').then(m => m.ClienteLayout),
  data: { breadcrumb: 'Cliente Demo' },
  children: [
    {
      path: '',
      loadComponent: () => import('./pages/cliente/cliente').then(m => m.Cliente)
    },
    {
      path: 'http',
      loadComponent: () => import('./pages/cliente/sections/http-section/http-demos').then(m => m.HttpDemos),
      data: { breadcrumb: 'HTTP Demos' }
    },
    {
      path: 'state',
      loadComponent: () => import('./pages/cliente/sections/state-section/state-demos').then(m => m.StateDemos),
      data: { breadcrumb: 'State Demos' }
    }
  ]
}
```

## Guards

### authGuard (CanActivateFn)
- **Purpose:** Requires authentication
- **Behavior:** Redirects to `/login` with `returnUrl` query param if unauthenticated
- **Location:** `src/app/core/guards/auth.guard.ts`

```typescript
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url }
  });
};
```

### guestGuard (CanActivateFn)
- **Purpose:** Only allows unauthenticated users
- **Behavior:** Redirects to `/dashboard` if authenticated
- **Location:** `src/app/core/guards/guest.guard.ts`

### rootGuard (CanActivateFn)
- **Purpose:** Smart redirect based on authentication state
- **Behavior:**
  - Authenticated users → `/dashboard`
  - Guest users → `/home`
- **Location:** `src/app/core/guards/root.guard.ts`

### pendingChangesGuard (CanDeactivateFn)
- **Purpose:** Prompts before navigation if form has unsaved changes
- **Interface:** `HasUnsavedChanges`
- **Location:** `src/app/core/guards/pending-changes.guard.ts`

```typescript
export interface HasUnsavedChanges {
  hasUnsavedChanges(): boolean;
}

export const pendingChangesGuard: CanDeactivateFn<HasUnsavedChanges> = (component) => {
  if (component.hasUnsavedChanges()) {
    return confirm('You have unsaved changes. Are you sure you want to leave?');
  }
  return true;
};
```

## Resolvers

### orderResolver (ResolveFn)
- **Purpose:** Preloads order data for `/orders/:id` route
- **Behavior:** Redirects to `/orders` with error state if order not found
- **Location:** `src/app/core/resolvers/order.resolver.ts`

```typescript
export const orderResolver: ResolveFn<Order> = (route) => {
  const orderId = route.paramMap.get('id');
  const orderService = inject(OrderService);
  const router = inject(Router);

  return orderService.getOrder(orderId!).pipe(
    catchError(() => {
      router.navigate(['/orders']);
      return EMPTY;
    })
  );
};
```

## Route Configuration Features

### Lazy Loading Strategy
- All page components use `loadComponent()` for lazy loading
- Uses `PreloadAllModules` to preload lazy chunks after initial load
- Configured in `app.config.ts`:

```typescript
provideRouter(
  routes,
  withPreloading(PreloadAllModules),
  withInMemoryScrolling({ anchorScrolling: 'enabled' })
)
```

### In-Memory Scrolling
- Anchor scrolling enabled for in-page navigation
- Smooth transitions between sections

### Breadcrumb Support
- Routes define `data.breadcrumb` for breadcrumb navigation
- `BreadcrumbService` builds crumbs from activated route tree
- `BreadcrumbComponent` displays hierarchical navigation

Example breadcrumb trail for `/orders/123`:
```
Dashboard > Orders > Order Details
```

## URL Parameters

| Parameter | Route | Type | Description |
|-----------|-------|------|-------------|
| `:id` | `/orders/:id` | Path | Order ID for detail view |

## Query Parameters

| Parameter | Route | Description |
|-----------|-------|-------------|
| `returnUrl` | `/login` | URL to redirect to after login |
| `registered` | `/login` | Shows success message after registration |
| `sessionExpired` | `/login` | Shows session expired message |
| `service` | `/dashboard` | Pre-selects service for quick ordering |

## Angular 21 Best Practices

1. **Standalone Components**: All components are standalone, no NgModules required
2. **Functional Guards**: Using `CanActivateFn` and `CanDeactivateFn` (class-based deprecated in Angular 15+)
3. **Functional Resolvers**: Using `ResolveFn` (class-based deprecated)
4. **Lazy Loading**: All page components are lazy loaded via `loadComponent()`
5. **Type Safety**: Routes are typed with proper interfaces
6. **Layout Components**: Shell components with `<router-outlet />` for nested routes

## File Locations

| Component | Location |
|-----------|----------|
| Route Configuration | `src/app/app.routes.ts` |
| Guards | `src/app/core/guards/` |
| Resolvers | `src/app/core/resolvers/` |
| Layout Components | `src/app/pages/*/[name]-layout.ts` |
