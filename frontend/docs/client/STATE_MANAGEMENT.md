# State Management - AntiPanel Frontend

## Overview

AntiPanel uses Angular Signals as the primary state management pattern, following Angular 21 best practices. This provides fine-grained reactivity, optimal change detection, and excellent developer experience.

## State Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        State Architecture                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                        Component Layer                           │    │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │    │
│  │  │  Dashboard   │    │    Orders    │    │    Wallet    │       │    │
│  │  │  Component   │    │  Component   │    │  Component   │       │    │
│  │  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘       │    │
│  └─────────┼───────────────────┼───────────────────┼───────────────┘    │
│            │                   │                   │                     │
│            ▼                   ▼                   ▼                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                         Service Layer                            │    │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │    │
│  │  │  AuthService │    │ OrderService │    │InvoiceService│       │    │
│  │  │  (Signals)   │    │  (RxJS)      │    │   (RxJS)     │       │    │
│  │  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘       │    │
│  └─────────┼───────────────────┼───────────────────┼───────────────┘    │
│            │                   │                   │                     │
│            ▼                   ▼                   ▼                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                        Storage Layer                             │    │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │    │
│  │  │ TokenService │    │  API Cache   │    │ LocalStorage │       │    │
│  │  │  (Signals)   │    │  (Signals)   │    │              │       │    │
│  │  └──────────────┘    └──────────────┘    └──────────────┘       │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

## Chosen Pattern: Angular Signals + Service Layer

### Why Signals?

Angular Signals (introduced in Angular 16, stable in Angular 17+) provide:

1. **Fine-grained reactivity**: Only components reading specific signals re-render
2. **No zone.js dependency**: Works with zoneless change detection
3. **Simpler mental model**: No need to manage subscriptions
4. **Built-in computed**: Derived state automatically updates
5. **Type-safe**: Full TypeScript support
6. **Official Angular API**: Future-proof, maintained by Angular team

### Pattern Comparison

| Feature | Angular Signals | BehaviorSubject | NgRx Store |
|---------|-----------------|-----------------|------------|
| **Learning Curve** | Low | Medium | High |
| **Boilerplate** | Minimal | Low | High |
| **Bundle Size** | 0 (built-in) | ~7KB (RxJS) | ~50KB+ |
| **DevTools** | Angular DevTools | - | NgRx DevTools |
| **Time Travel** | No | No | Yes |
| **Best For** | UI state, simple app state | Async streams, complex flows | Large enterprise apps |
| **Change Detection** | Automatic, fine-grained | Manual or async pipe | Store-based |
| **Subscription Management** | Not needed | Required | Required |
| **Angular 21 Recommendation** | Yes | Limited use | Complex apps only |

### When to Use Each

| Pattern | Use Case |
|---------|----------|
| **Signals** | UI state, form state, simple service state, derived values |
| **BehaviorSubject** | HTTP responses (converted to signal), complex async flows |
| **NgRx** | Large teams, complex enterprise apps, time-travel debugging needs |

## Implementation Examples

### 1. Simple State with Signals

```typescript
// src/app/core/services/token.service.ts
@Injectable({ providedIn: 'root' })
export class TokenService {
  // Private writable signals
  private readonly accessToken = signal<string | null>(null);
  private readonly refreshToken = signal<string | null>(null);
  private readonly user = signal<UserSummary | null>(null);

  // Public read-only computed signals
  readonly isAuthenticated = computed(() => !!this.accessToken());
  readonly currentUser = computed(() => this.user());

  // Methods to update state
  setTokens(access: string, refresh: string, user: UserSummary): void {
    this.accessToken.set(access);
    this.refreshToken.set(refresh);
    this.user.set(user);
  }

  clearTokens(): void {
    this.accessToken.set(null);
    this.refreshToken.set(null);
    this.user.set(null);
  }
}
```

### 2. Derived State with Computed

```typescript
// src/app/pages/orders/orders.ts
@Component({...})
export class Orders {
  // Source signals
  protected readonly orders = signal<Order[]>([]);
  protected readonly statusFilter = signal<OrderStatus | 'all'>('all');
  protected readonly searchQuery = signal('');

  // Computed derived state
  protected readonly filteredOrders = computed(() => {
    const all = this.orders();
    const status = this.statusFilter();
    const query = this.searchQuery().toLowerCase();

    return all
      .filter(o => status === 'all' || o.status === status)
      .filter(o => o.serviceName.toLowerCase().includes(query));
  });

  // Computed statistics
  protected readonly orderStats = computed(() => ({
    total: this.orders().length,
    pending: this.orders().filter(o => o.status === 'PENDING').length,
    completed: this.orders().filter(o => o.status === 'COMPLETED').length
  }));
}
```

### 3. Catalog Cache with Signals

```typescript
// src/app/services/catalog.service.ts
@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);

  // Cache signals
  private readonly categoriesCache = signal<Category[] | null>(null);
  private readonly servicesCacheMap = signal<Map<number, Service[]>>(new Map());

  getCategories(): Observable<Category[]> {
    const cached = this.categoriesCache();
    if (cached) {
      return of(cached);  // Return cached data
    }

    return this.http.get<Category[]>('/api/public/categories').pipe(
      tap(categories => this.categoriesCache.set(categories))
    );
  }

  clearCache(): void {
    this.categoriesCache.set(null);
    this.servicesCacheMap.set(new Map());
  }
}
```

### 4. Component-level State

```typescript
// src/app/pages/dashboard/dashboard.ts
@Component({...})
export class Dashboard implements OnInit {
  private readonly userService = inject(UserService);
  private readonly destroyRef = inject(DestroyRef);

  // UI state signals
  protected readonly isLoading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly stats = signal<UserStatistics | null>(null);

  // Computed for template
  protected readonly hasError = computed(() => !!this.error());
  protected readonly isEmpty = computed(() =>
    !this.isLoading() && !this.stats()
  );

  ngOnInit(): void {
    this.loadStats();
  }

  private loadStats(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.userService.getStatistics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (stats) => {
          this.stats.set(stats);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.error.set('Failed to load statistics');
          this.isLoading.set(false);
        }
      });
  }
}
```

### 5. Polling with Signals (Real-time Updates)

```typescript
// src/app/pages/cliente/sections/state-section/state-demos.ts
@Component({...})
export class StateDemos implements OnDestroy {
  private readonly stopPolling$ = new Subject<void>();

  // State signals
  protected readonly isPolling = signal(false);
  protected readonly pollingInterval = signal(2000);
  protected readonly dataItems = signal<DataItem[]>([]);
  protected readonly lastUpdated = signal<Date | null>(null);

  startPolling(): void {
    this.isPolling.set(true);

    interval(this.pollingInterval())
      .pipe(
        takeUntil(this.stopPolling$),
        switchMap(() => this.fetchData())
      )
      .subscribe(data => {
        this.dataItems.set(data);
        this.lastUpdated.set(new Date());
      });
  }

  stopPolling(): void {
    this.stopPolling$.next();
    this.isPolling.set(false);
  }
}
```

## Data Flow Patterns

### 1. Unidirectional Data Flow

```
User Action → Service Method → Signal Update → Component Re-render
     ↓              ↓               ↓                  ↓
   click()     updateData()    data.set(x)    {{ data() }}
```

### 2. HTTP Data Flow

```
Component Request → Service → HTTP Call → Response → Signal → Template

orders.ts:         orderService.        http.get()   →  orders   {{ orders() }}
loadOrders()    →  getOrders()     →      ↓        →   .set()  →
                                      JSON response
```

### 3. Authentication Flow

```
LoginForm → AuthService.login() → API → TokenService → AuthGuard
    ↓             ↓                ↓         ↓            ↓
  submit    POST /auth/login    tokens   setTokens()   isAuthenticated()
```

## Optimization Strategies

### 1. Track Expression for @for

All `@for` loops use `track` for efficient DOM updates:

```html
<!-- Good: Track by unique identifier -->
@for (order of orders(); track order.id) {
  <order-card [order]="order" />
}

<!-- Good: Track by index for static lists -->
@for (item of menuItems; track $index) {
  <menu-item [item]="item" />
}
```

### 2. OnPush Change Detection

All components use `OnPush` for optimal performance:

```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  ...
})
export class MyComponent {}
```

### 3. Signal-based Inputs (Angular 17.1+)

```typescript
// Modern signal input
export class OrderCard {
  order = input.required<Order>();
  expanded = input(false);

  // Computed from input signal
  statusClass = computed(() =>
    `status--${this.order().status.toLowerCase()}`
  );
}
```

### 4. Lazy Loading State

State is loaded on-demand, not preloaded:

```typescript
ngOnInit(): void {
  // Load data only when component mounts
  this.loadData();
}
```

### 5. Debounced Search

Search inputs use debounce to reduce API calls:

```typescript
protected readonly searchQuery = signal('');
private readonly searchSubject = new Subject<string>();

constructor() {
  this.searchSubject
    .pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed()
    )
    .subscribe(query => this.performSearch(query));
}

onSearchInput(query: string): void {
  this.searchQuery.set(query);
  this.searchSubject.next(query);
}
```

## State Categories

| Category | Storage | Pattern | Example |
|----------|---------|---------|---------|
| **Auth State** | Memory + localStorage | Signals | `TokenService` |
| **UI State** | Memory only | Signals | Loading, errors, modals |
| **Server State** | Memory (cache) | Signals + HTTP | `CatalogService` cache |
| **Form State** | Component | Reactive Forms | `RegisterForm` |
| **Route State** | URL | Router | Query params, path params |

## File Locations

| Service | Path | State Type |
|---------|------|------------|
| TokenService | `src/app/core/services/token.service.ts` | Auth tokens, user |
| AuthService | `src/app/core/services/auth.service.ts` | Auth computed signals |
| CatalogService | `src/app/services/catalog.service.ts` | Categories, services cache |
| LoadingService | `src/app/services/loading.service.ts` | Global loading state |
| ThemeService | `src/app/services/theme.service.ts` | Theme preferences |
| NotificationService | `src/app/services/notification.service.ts` | Toast notifications |
