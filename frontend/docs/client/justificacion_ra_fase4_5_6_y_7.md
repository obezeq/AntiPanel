# JUSTIFICACION DE RESULTADOS DE APRENDIZAJE
## Fases 4, 5, 6 y 7 - Desarrollo Frontend con Angular

---

**Proyecto:** AntiPanel Frontend
**Framework:** Angular 21 (con Signals, standalone components, functional guards/interceptors)
**Entorno:** Docker con Bun
**Fecha de evaluacion:** Enero 2026
**Repositorio:** [frontend/src/app/](../../src/app/)

---

## INDICE DE CONTENIDOS

### Navegacion Rapida por Criterio de Evaluacion

| Criterio | Descripcion | Puntos | Ir a seccion |
|:--------:|-------------|:------:|:------------:|
| **RA6.g** | Navegacion entre contenidos | 1.5/1.5 | [Ver RA6.g](#ra6g-navegacion-entre-contenidos) |
| **RA6.h** | Mecanismos historial/navegacion | 1.5/1.5 | [Ver RA6.h](#ra6h-mecanismos-de-historial-y-navegacion) |
| **RA7.a** | Comunicacion asincrona | 0.75/0.75 | [Ver RA7.a](#ra7a-mecanismos-de-comunicacion-asincrona) |
| **RA7.b** | Formatos envio/recepcion | 0.75/0.75 | [Ver RA7.b](#ra7b-formatos-para-envio-y-recepcion) |
| **RA7.c** | Librerias comunicacion | 0.75/0.75 | [Ver RA7.c](#ra7c-librerias-de-comunicacion-asincrona) |
| **RA7.d** | Modificaciones dinamicas | 0.75/0.75 | [Ver RA7.d](#ra7d-modificaciones-dinamicas-del-documento) |
| **RA7.e** | Objetos del navegador | 0.5/0.5 | [Ver RA7.e](#ra7e-objetos-proporcionados-por-el-navegador) |
| **RA7.f** | Objetos del documento | 0.5/0.5 | [Ver RA7.f](#ra7f-objetos-generados-a-partir-del-documento) |
| **RA7.h** | Objetos dinamicos | 0.5/0.5 | [Ver RA7.h](#ra7h-aplicaciones-con-objetos-dinamicos) |
| **RA7.i** | Interaccion usuario | 0.5/0.5 | [Ver RA7.i](#ra7i-interaccion-con-usuario) |
| **RA6.f** | Testing y documentacion | 1.0/1.0 | [Ver RA6.f](#ra6f-herramientas-de-depuracion-y-documentacion) |

### Indice Detallado

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [FASE 4: Sistema de Rutas](#fase-4-sistema-de-rutas)
   - [RA6.g - Navegacion entre contenidos](#ra6g-navegacion-entre-contenidos)
   - [RA6.h - Mecanismos de historial y navegacion](#ra6h-mecanismos-de-historial-y-navegacion)
3. [FASE 5: Comunicacion HTTP](#fase-5-comunicacion-http)
   - [RA7.a - Mecanismos de comunicacion asincrona](#ra7a-mecanismos-de-comunicacion-asincrona)
   - [RA7.b - Formatos para envio y recepcion](#ra7b-formatos-para-envio-y-recepcion)
   - [RA7.c - Librerias de comunicacion asincrona](#ra7c-librerias-de-comunicacion-asincrona)
   - [RA7.d - Modificaciones dinamicas del documento](#ra7d-modificaciones-dinamicas-del-documento)
4. [FASE 6: Gestion del Estado](#fase-6-gestion-del-estado)
   - [RA7.e - Objetos proporcionados por el navegador](#ra7e-objetos-proporcionados-por-el-navegador)
   - [RA7.f - Objetos generados a partir del documento](#ra7f-objetos-generados-a-partir-del-documento)
   - [RA7.h - Aplicaciones con objetos dinamicos](#ra7h-aplicaciones-con-objetos-dinamicos)
   - [RA7.i - Interaccion con usuario](#ra7i-interaccion-con-usuario)
5. [FASE 7: Testing y Calidad](#fase-7-testing-y-calidad)
   - [RA6.f - Herramientas de depuracion y documentacion](#ra6f-herramientas-de-depuracion-y-documentacion)
6. [Archivos de Referencia](#archivos-de-referencia)
7. [Decisiones Arquitectonicas Angular 21](#decisiones-arquitectonicas-angular-21)
8. [Conclusion](#conclusion)

---

## RESUMEN EJECUTIVO

### Puntuacion Total: **9.0 / 9.0 puntos (100%)**

| Fase | Criterios | Obtenido | Maximo | % |
|:----:|-----------|:--------:|:------:|:-:|
| **4** | RA6.g + RA6.h | **3.0** | 3.0 | 100% |
| **5** | RA7.a + RA7.b + RA7.c + RA7.d | **3.0** | 3.0 | 100% |
| **6** | RA7.e + RA7.f + RA7.h + RA7.i | **2.0** | 2.0 | 100% |
| **7** | RA6.f | **1.0** | 1.0 | 100% |
| | **TOTAL** | **9.0** | **9.0** | **100%** |

### Principales Fortalezas

| Aspecto | Descripcion |
|---------|-------------|
| Guards funcionales | 4 guards (authGuard, guestGuard, rootGuard, pendingChangesGuard) |
| Resolver funcional | orderResolver con ResolveFn |
| Lazy loading | 100% de rutas (13/13) |
| Interceptores | authInterceptor + loadingInterceptor (HttpInterceptorFn) |
| Retry logic | Exponential backoff en servicios HTTP |
| Signals | 15+ archivos con signal(), computed(), effect() |
| Testing | 79 tests (4 componentes + 3 servicios) |

---

## FASE 4: SISTEMA DE RUTAS

### RA6.g: NAVEGACION ENTRE CONTENIDOS

#### Puntuacion: **1.5/1.5**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa sistema de rutas | - |
| 0.5 | Rutas basicas sin parametros | - |
| 1.0 | Rutas con parametros + guards basicos | - |
| **1.5** | **Rutas con parametros + guards funcionales + resolver + lazy loading + breadcrumbs** | ✅ |

#### Evidencia: Rutas con Parametros

**Archivo:** [`src/app/app.routes.ts`](../../src/app/app.routes.ts) (lineas 31-38)

```typescript
{
  path: 'orders/:id',
  loadComponent: () =>
    import('./pages/order-detail/order-detail').then(m => m.OrderDetail),
  canActivate: [authGuard],
  resolve: { order: orderResolver },
  data: { breadcrumb: 'Order Details' }
}
```

#### Evidencia: Guards Funcionales (CanActivateFn)

**Archivo:** [`src/app/core/guards/auth.guard.ts`](../../src/app/core/guards/auth.guard.ts)

```typescript
// CanActivateFn - Angular 21 funcional
export const authGuard: CanActivateFn = (route, state) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (tokenService.isAuthenticated()) {
    return true;
  }

  const returnUrl = state.url;
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl }
  });
};
```

| Guard | Archivo | Tipo | Funcion |
|-------|---------|------|---------|
| `authGuard` | auth.guard.ts | CanActivateFn | Protege rutas autenticadas |
| `guestGuard` | guest.guard.ts | CanActivateFn | Protege /login y /register |
| `rootGuard` | root.guard.ts | CanActivateFn | Smart redirect en `/` |
| `pendingChangesGuard` | pending-changes.guard.ts | CanDeactivateFn | Protege formularios |

#### Evidencia: Resolver Funcional (ResolveFn)

**Archivo:** [`src/app/core/resolvers/order.resolver.ts`](../../src/app/core/resolvers/order.resolver.ts) (lineas 26-44)

```typescript
export const orderResolver: ResolveFn<OrderResponse | null> = (route) => {
  const orderService = inject(OrderService);
  const router = inject(Router);
  const id = route.paramMap.get('id');

  if (!id) {
    router.navigate(['/orders']);
    return of(null);
  }

  return orderService.getOrderById(Number(id)).pipe(
    catchError(() => {
      router.navigate(['/orders'], {
        state: { error: `Order #${id} not found` }
      });
      return of(null);
    })
  );
};
```

#### Evidencia: Lazy Loading (100%)

**Archivo:** [`src/app/app.routes.ts`](../../src/app/app.routes.ts)

| # | Ruta | Linea | loadComponent |
|:-:|------|:-----:|:-------------:|
| 1 | /home | 7-10 | ✅ |
| 2 | /dashboard | 11-16 | ✅ |
| 3 | /wallet | 17-23 | ✅ |
| 4 | /orders | 24-30 | ✅ |
| 5 | /orders/:id | 31-38 | ✅ |
| 6 | /cliente | 39-43 | ✅ |
| 7 | /style-guide | 44-48 | ✅ |
| 8 | /formarray | 49-53 | ✅ |
| 9 | /login | 54-59 | ✅ |
| 10 | /register | 60-66 | ✅ |
| 11 | /terms | 67-71 | ✅ |
| 12 | /support | 72-76 | ✅ |
| 13 | /** (404) | 83-87 | ✅ |

#### Evidencia: Breadcrumbs Dinamicos

**Archivo:** [`src/app/core/services/breadcrumb.service.ts`](../../src/app/core/services/breadcrumb.service.ts)

```typescript
@Injectable({ providedIn: 'root' })
export class BreadcrumbService {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  private readonly _breadcrumbs = signal<Breadcrumb[]>([]);
  readonly breadcrumbs = this._breadcrumbs.asReadonly();

  constructor() {
    this._breadcrumbs.set(this.buildBreadcrumbs(this.activatedRoute.root));
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this._breadcrumbs.set(this.buildBreadcrumbs(this.activatedRoute.root));
    });
  }
}
```

**Rutas con data.breadcrumb:**

| Ruta | Breadcrumb | Linea |
|------|------------|:-----:|
| /wallet | 'Wallet' | 22 |
| /orders | 'Orders' | 29 |
| /orders/:id | 'Order Details' | 37 |

#### Justificacion de la Puntuacion

**1.5/1.5** porque:
- ✅ Ruta con parametros `/orders/:id`
- ✅ 4 guards funcionales (CanActivateFn, CanDeactivateFn)
- ✅ Resolver funcional (ResolveFn)
- ✅ Lazy loading 100% (13/13 rutas)
- ✅ BreadcrumbService con signals
- ✅ Documentacion en ROUTES.md

---

### RA6.h: MECANISMOS DE HISTORIAL Y NAVEGACION

#### Puntuacion: **1.5/1.5**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa mecanismos de navegacion | - |
| 0.5 | Navegacion basica con routerLink | - |
| 1.0 | Router.navigate() + query params | - |
| **1.5** | **CanDeactivate + PreloadAllModules + query params avanzados** | ✅ |

#### Evidencia: Guard CanDeactivate

**Archivo:** [`src/app/core/guards/pending-changes.guard.ts`](../../src/app/core/guards/pending-changes.guard.ts) (lineas 18-48)

```typescript
export interface HasUnsavedChanges {
  hasUnsavedChanges(): boolean;
}

export const pendingChangesGuard: CanDeactivateFn<HasUnsavedChanges> = (component) => {
  if (component.hasUnsavedChanges?.()) {
    return confirm('You have unsaved changes. Are you sure you want to leave?');
  }
  return true;
};
```

**Uso en ruta:** [`src/app/app.routes.ts`](../../src/app/app.routes.ts) (lineas 60-66)

```typescript
{
  path: 'register',
  loadComponent: () =>
    import('./pages/register/register').then(m => m.Register),
  canActivate: [guestGuard],
  canDeactivate: [pendingChangesGuard]  // ← CanDeactivate aplicado
}
```

#### Evidencia: PreloadAllModules

**Archivo:** [`src/app/app.config.ts`](../../src/app/app.config.ts) (lineas 71-75)

```typescript
provideRouter(
  routes,
  withInMemoryScrolling({ anchorScrolling: 'enabled' }),
  withPreloading(PreloadAllModules)  // ← Preload strategy
),
```

#### Evidencia: Query Parameters Avanzados

| Param | Ruta | Archivo | Uso |
|-------|------|---------|-----|
| `returnUrl` | /login | login.ts:57-60 | Redirige tras login |
| `registered` | /login | login.ts:63-66 | Mensaje de exito |
| `sessionExpired` | /login | login.ts:69-72 | Mensaje de sesion |
| `service` | /dashboard | orders.ts:302-304 | Preselecciona servicio |

**Archivo:** [`src/app/pages/login/login.ts`](../../src/app/pages/login/login.ts)

```typescript
// Lineas 57-72 - Lectura de query params
const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
if (returnUrl) {
  this.returnUrl = returnUrl;
}

const registered = this.route.snapshot.queryParamMap.get('registered');
if (registered === 'true') {
  this.successMessage.set('Account created successfully! Please login.');
}

const sessionExpired = this.route.snapshot.queryParamMap.get('sessionExpired');
if (sessionExpired === 'true') {
  this.infoMessage.set('Your session has expired. Please login again.');
}
```

#### Evidencia: Navegacion Programatica

**Router.navigate() con queryParams:**

```typescript
// register.ts:73-75
this.router.navigate(['/login'], {
  queryParams: { registered: 'true' }
});

// orders.ts:302-304
this.router.navigate(['/dashboard'], {
  queryParams: { service: order.serviceName }
});
```

**Router.navigateByUrl():**

```typescript
// login.ts:101
this.router.navigateByUrl(this.returnUrl);
```

#### Justificacion de la Puntuacion

**1.5/1.5** porque:
- ✅ `pendingChangesGuard` (CanDeactivateFn) implementado
- ✅ `PreloadAllModules` configurado
- ✅ `withInMemoryScrolling` configurado
- ✅ Query params: returnUrl, registered, sessionExpired, service
- ✅ Router.navigate() y Router.navigateByUrl() usados correctamente
- ✅ routerLink en templates (header, breadcrumb, footer)

---

## FASE 5: COMUNICACION HTTP

### RA7.a: MECANISMOS DE COMUNICACION ASINCRONA

#### Puntuacion: **0.75/0.75**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No usa HttpClient | - |
| 0.25 | HttpClient basico sin interceptores | - |
| 0.5 | HttpClient + interceptores basicos | - |
| **0.75** | **HttpClient + interceptores funcionales + retry logic** | ✅ |

#### Evidencia: provideHttpClient con Interceptores

**Archivo:** [`src/app/app.config.ts`](../../src/app/app.config.ts) (linea 76)

```typescript
provideHttpClient(withInterceptors([authInterceptor, loadingInterceptor])),
```

#### Evidencia: authInterceptor (HttpInterceptorFn)

**Archivo:** [`src/app/core/interceptors/auth.interceptor.ts`](../../src/app/core/interceptors/auth.interceptor.ts)

```typescript
// HttpInterceptorFn - Angular 21 funcional
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const authService = inject(AuthService);
  const router = inject(Router);

  // Skip auth for public endpoints
  if (SKIP_AUTH_URLS.some(url => req.url.includes(url))) {
    return next(req);
  }

  const token = tokenService.getAccessToken();
  if (!token) {
    return next(req);
  }

  return next(addAuthHeader(req, token)).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Handle token refresh...
      }
      return throwError(() => error);
    })
  );
};
```

#### Evidencia: Retry Logic con Exponential Backoff

**Archivo:** [`src/app/core/services/order.service.ts`](../../src/app/core/services/order.service.ts) (lineas 127-138)

```typescript
/** Retry configuration for GET requests */
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

// Uso en metodos (lineas 176-180)
getOrders(page = 0, size = 20): Observable<PageResponse<OrderResponse>> {
  return this.http.get<PageResponse<OrderResponse>>(this.baseUrl, {
    params: { page: page.toString(), size: size.toString() }
  }).pipe(retry(this.retryConfig));
}
```

#### Justificacion de la Puntuacion

**0.75/0.75** porque:
- ✅ `provideHttpClient()` configurado (no HttpClientModule deprecated)
- ✅ `withInterceptors()` con interceptores funcionales
- ✅ `authInterceptor` con Bearer token y refresh automatico
- ✅ `loadingInterceptor` con gestion de loading states
- ✅ Retry logic con exponential backoff en OrderService y UserService
- ✅ `catchError` y `throwError` en todos los servicios

---

### RA7.b: FORMATOS PARA ENVIO Y RECEPCION

#### Puntuacion: **0.75/0.75**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No define formatos | - |
| 0.25 | JSON sin tipado | - |
| 0.5 | JSON con algunos tipos | - |
| **0.75** | **JSON + interfaces TypeScript exhaustivas** | ✅ |

#### Evidencia: Interfaces TypeScript

**Archivo:** [`src/app/core/services/order.service.ts`](../../src/app/core/services/order.service.ts) (lineas 14-92)

```typescript
export type OrderStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'PARTIAL'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'REFUNDED'
  | 'FAILED';

export interface OrderCreateRequest {
  serviceId: number;
  target: string;
  quantity: number;
  idempotencyKey?: string;
}

export interface OrderResponse {
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

export interface PageResponse<T> {
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

#### Interfaces Principales por Servicio

| Servicio | Interfaces | Archivo |
|----------|------------|---------|
| AuthService | LoginRequest, RegisterRequest, AuthResponse, UserResponse, ApiError | auth.service.ts |
| OrderService | OrderCreateRequest, OrderResponse, PageResponse<T>, OrderStatus | order.service.ts |
| InvoiceService | InvoiceCreateRequest, InvoiceResponse, PaymentProcessor | invoice.service.ts |
| UserService | UserStatisticsResponse | user.service.ts |
| TokenService | UserSummary | token.service.ts |

#### Justificacion de la Puntuacion

**0.75/0.75** porque:
- ✅ JSON format automatico (Angular HttpClient)
- ✅ 15+ interfaces TypeScript definidas
- ✅ Generic types: `PageResponse<T>`
- ✅ Discriminated unions: `OrderStatus`, `InvoiceStatus`
- ✅ Optional fields correctamente tipados (`idempotencyKey?`)
- ✅ Nullable types (`startCount: number | null`)

---

### RA7.c: LIBRERIAS DE COMUNICACION ASINCRONA

#### Puntuacion: **0.75/0.75**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No usa librerias | - |
| 0.25 | Promises basicas | - |
| 0.5 | RxJS basico | - |
| **0.75** | **RxJS avanzado con multiples operadores** | ✅ |

#### Operadores RxJS Implementados

| Operador | Ubicacion | Proposito |
|----------|-----------|-----------|
| `pipe()` | Todos los servicios | Encadenar operadores |
| `map()` | auth.service.ts | Transformar respuestas |
| `tap()` | auth.service.ts | Side effects (guardar tokens) |
| `catchError()` | Todos los servicios | Manejo de errores |
| `throwError()` | Todos los servicios | Lanzar errores tipados |
| `retry()` | order.service.ts, user.service.ts | Reintentos con backoff |
| `switchMap()` | auth.interceptor.ts | Token refresh |
| `filter()` | auth.interceptor.ts, breadcrumb.service.ts | Filtrar eventos |
| `take(1)` | auth.interceptor.ts | Limitar emision |
| `timer()` | order.service.ts | Delay en retry |
| `forkJoin()` | wallet.ts | Combinar requests |
| `fromEvent()` | wallet.ts | Eventos del DOM |
| `debounceTime()` | order-filters.ts | Debounce busqueda |
| `distinctUntilChanged()` | order-filters.ts | Evitar duplicados |
| `takeUntilDestroyed()` | Todos los componentes | Cleanup automatico |

#### Evidencia: switchMap en Token Refresh

**Archivo:** [`src/app/core/interceptors/auth.interceptor.ts`](../../src/app/core/interceptors/auth.interceptor.ts)

```typescript
return authService.refreshToken(refreshToken).pipe(
  switchMap((response) => {
    tokenService.setTokens(
      response.accessToken,
      response.refreshToken,
      response.expiresIn,
      response.user
    );
    refreshTokenSubject.next(response.accessToken);
    return next(addAuthHeader(req, response.accessToken));
  }),
  catchError((refreshError) => {
    return handleLogout(router, tokenService, refreshError);
  })
);
```

#### Justificacion de la Puntuacion

**0.75/0.75** porque:
- ✅ 15+ operadores RxJS implementados
- ✅ `switchMap` para token refresh (cancela requests anteriores)
- ✅ `BehaviorSubject` para queue de requests durante refresh
- ✅ `takeUntilDestroyed()` para cleanup automatico
- ✅ Combinacion de operadores en pipelines complejos

---

### RA7.d: MODIFICACIONES DINAMICAS DEL DOCUMENTO

#### Puntuacion: **0.75/0.75**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No hay modificaciones dinamicas | - |
| 0.25 | Actualizacion basica | - |
| 0.5 | Loading states basicos | - |
| **0.75** | **Signals + loading states reactivos** | ✅ |

#### Evidencia: LoadingService con Signals

**Archivo:** [`src/app/services/loading.service.ts`](../../src/app/services/loading.service.ts)

```typescript
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly _activeRequests = signal(0);

  readonly isLoading = computed(() => this._activeRequests() > 0);
  readonly activeRequests = this._activeRequests.asReadonly();

  show(): void {
    this._activeRequests.update(count => count + 1);
  }

  hide(): void {
    this._activeRequests.update(count => Math.max(0, count - 1));
  }

  reset(): void {
    this._activeRequests.set(0);
  }
}
```

#### Evidencia: loadingInterceptor con finalize()

**Archivo:** [`src/app/core/interceptors/loading.interceptor.ts`](../../src/app/core/interceptors/loading.interceptor.ts)

```typescript
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);

  // Skip loading for certain URLs
  if (IGNORED_URLS.some(url => req.url.includes(url))) {
    return next(req);
  }

  loadingService.show();
  return next(req).pipe(
    finalize(() => loadingService.hide())
  );
};
```

#### Justificacion de la Puntuacion

**0.75/0.75** porque:
- ✅ `signal<number>` para contador de requests activos
- ✅ `computed()` para derivar `isLoading`
- ✅ `finalize()` en interceptor para cleanup
- ✅ Manejo de multiples requests concurrentes
- ✅ URLs ignoradas configurables

---

## FASE 6: GESTION DEL ESTADO

### RA7.e: OBJETOS PROPORCIONADOS POR EL NAVEGADOR

#### Puntuacion: **0.5/0.5**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No usa APIs del navegador | - |
| 0.25 | Uso basico de localStorage | - |
| **0.5** | **localStorage + window APIs + sync cross-tab** | ✅ |

#### APIs del Navegador Implementadas

| API | Uso | Archivo |
|-----|-----|---------|
| `localStorage.setItem()` | Tokens + tema | token.service.ts, theme.service.ts |
| `localStorage.getItem()` | Recuperar datos | token.service.ts, theme.service.ts |
| `localStorage.removeItem()` | Logout | token.service.ts |
| `window.addEventListener('storage')` | Sync cross-tab | theme.service.ts |
| `window.matchMedia()` | Dark mode detection | theme.service.ts |
| `window.open()` | Payment URLs | wallet.ts |
| `document.visibilityState` | Polling cuando visible | wallet.ts |
| `navigator.clipboard` | Copiar al portapapeles | style-guide.ts |

#### Evidencia: localStorage para Tokens

**Archivo:** [`src/app/core/services/token.service.ts`](../../src/app/core/services/token.service.ts)

```typescript
const ACCESS_TOKEN_KEY = 'antipanel_access_token';
const REFRESH_TOKEN_KEY = 'antipanel_refresh_token';
const USER_KEY = 'antipanel_user';
const TOKEN_EXPIRY_KEY = 'antipanel_token_expiry';

setTokens(accessToken: string, refreshToken: string, expiresIn: number, user?: UserSummary): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  localStorage.setItem(TOKEN_EXPIRY_KEY, expiresAt.toString());
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }
}

clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  localStorage.removeItem(TOKEN_EXPIRY_KEY);
}
```

#### Evidencia: Cross-tab Sync

**Archivo:** [`src/app/services/theme.service.ts`](../../src/app/services/theme.service.ts)

```typescript
private setupStorageListener(): void {
  window.addEventListener('storage', (event) => {
    if (event.key === 'antipanel-theme' && event.newValue) {
      const newTheme = event.newValue as Theme;
      if (['light', 'dark', 'system'].includes(newTheme)) {
        this.selectedTheme.set(newTheme);
      }
    }
  });
}
```

#### Justificacion de la Puntuacion

**0.5/0.5** porque:
- ✅ localStorage para tokens (4 keys)
- ✅ localStorage para tema
- ✅ `window.addEventListener('storage')` para sync cross-tab
- ✅ `window.matchMedia()` para preferencia del sistema
- ✅ `document.visibilityState` para polling inteligente
- ✅ `navigator.clipboard` para copiar

---

### RA7.f: OBJETOS GENERADOS A PARTIR DEL DOCUMENTO

#### Puntuacion: **0.5/0.5**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No usa formularios reactivos | - |
| 0.25 | Formularios basicos | - |
| **0.5** | **Reactive Forms + FormArray + validadores custom** | ✅ |

#### Evidencia: FormGroup con Validadores

**Archivo:** [`src/app/components/shared/auth-form/auth-form.ts`](../../src/app/components/shared/auth-form/auth-form.ts)

```typescript
protected readonly form = this.fb.group({
  email: ['', {
    validators: [Validators.required, Validators.email],
    updateOn: 'blur' as const
  }],
  password: ['', [
    Validators.required,
    Validators.minLength(8)
  ]],
  confirmPassword: [''],
  termsAccepted: [false]
});

// Validadores aplicados dinamicamente
effect(() => {
  const isRegister = this.isRegisterMode();
  if (isRegister) {
    emailControl.setAsyncValidators([emailUniqueValidator(500)]);
    passwordControl.setValidators([
      Validators.required,
      Validators.minLength(8),
      passwordStrengthValidator()
    ]);
    this.form.setValidators([passwordMatchValidator('password', 'confirmPassword')]);
  }
});
```

#### Evidencia: FormArray Dinamico

**Archivo:** [`src/app/pages/formarray-demo/formarray-demo.ts`](../../src/app/pages/formarray-demo/formarray-demo.ts)

```typescript
protected readonly form = this.fb.group({
  customerName: ['', [Validators.required, Validators.minLength(2)]],
  items: this.fb.array([this.createItemGroup()])
});

get items(): FormArray {
  return this.form.get('items') as FormArray;
}

private createItemGroup(): FormGroup {
  return this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    quantity: [1, [Validators.required, Validators.min(1), Validators.max(100)]],
    price: [0, [Validators.required, Validators.min(0)]]
  });
}

protected addItem(): void {
  this.items.push(this.createItemGroup());
}

protected removeItem(index: number): void {
  if (this.items.length > 1) {
    this.items.removeAt(index);
  }
}
```

#### Validadores Implementados

| Tipo | Validador | Archivo |
|------|-----------|---------|
| Built-in | required, email, minLength, min, max | auth-form.ts, formarray-demo.ts |
| Custom sync | passwordStrengthValidator | password-strength.validator.ts |
| Custom sync | passwordMatchValidator | password-match.validator.ts |
| Custom sync | nifValidator, phoneValidator | custom-pattern.validator.ts |
| Custom async | emailUniqueValidator | email-unique.validator.ts |
| Custom async | usernameAvailableValidator | username-available.validator.ts |

#### Justificacion de la Puntuacion

**0.5/0.5** porque:
- ✅ FormGroup con multiples controles
- ✅ FormArray dinamico con add/remove
- ✅ Validators built-in (required, email, minLength, min, max)
- ✅ Validadores sincronos custom
- ✅ Validadores asincronos con debounce
- ✅ Cross-field validation (passwordMatchValidator)
- ✅ `updateOn: 'blur'` para mejor UX

---

### RA7.h: APLICACIONES CON OBJETOS DINAMICOS

#### Puntuacion: **0.5/0.5**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No hay objetos dinamicos | - |
| 0.25 | Estado basico | - |
| **0.5** | **Signals + computed + effect + paginacion + filtros** | ✅ |

#### Uso de Signals en el Proyecto

| Archivo | Signals | Computed | Effect |
|---------|:-------:|:--------:|:------:|
| token.service.ts | 2 | 3 | - |
| theme.service.ts | 2 | 2 | 2 |
| notification.service.ts | 1 | 2 | - |
| loading.service.ts | 1 | 1 | - |
| orders.ts | 9 | 3 | - |
| dashboard.ts | 6 | 2 | 1 |
| wallet.ts | 5 | - | - |
| auth-form.ts | 5 | 8 | 1 |
| **Total** | **30+** | **20+** | **4+** |

#### Evidencia: Filtrado Reactivo con computed()

**Archivo:** [`src/app/pages/orders/orders.ts`](../../src/app/pages/orders/orders.ts)

```typescript
protected readonly filteredOrders = computed(() => {
  let result = this.orders();

  // Filtro por categoria
  const category = this.selectedCategory();
  if (category !== 'ALL') {
    const statusMap: Record<FilterCategory, CardStatus[]> = {
      'ALL': [],
      'PENDING': ['pending'],
      'PROCESSING': ['processing'],
      'COMPLETED': ['completed', 'partial']
    };
    const statuses = statusMap[category];
    result = result.filter(order => statuses.includes(order.status));
  }

  // Filtro por busqueda
  const query = this.searchQuery().toLowerCase().trim();
  if (query) {
    result = result.filter(order =>
      order.serviceName.toLowerCase().includes(query) ||
      order.id.includes(query)
    );
  }

  // Ordenamiento
  const sortOrder = this.sortOrder();
  result = [...result].sort((a, b) => {
    const dateA = a.createdAt.getTime();
    const dateB = b.createdAt.getTime();
    return sortOrder === 'latest' ? dateB - dateA : dateA - dateB;
  });

  return result;
});
```

#### Evidencia: Paginacion

**Archivo:** [`src/app/pages/orders/orders.ts`](../../src/app/pages/orders/orders.ts)

```typescript
protected readonly currentPage = signal(1);
protected readonly totalPages = signal(1);
protected readonly pageSize = signal(10);
protected readonly totalElements = signal(0);

protected loadOrders(): void {
  const page = this.currentPage() - 1; // API es 0-indexed
  const size = this.pageSize();

  this.orderService.getOrders(page, size)
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: response => {
        this.orders.set(this.mapOrders(response.content));
        this.totalPages.set(response.totalPages || 1);
        this.totalElements.set(response.totalElements);
      }
    });
}
```

#### Justificacion de la Puntuacion

**0.5/0.5** porque:
- ✅ `signal()` en 15+ archivos
- ✅ `computed()` para derivados (filteredOrders, balance, isEmpty)
- ✅ `effect()` para persistencia y DOM (ThemeService)
- ✅ Paginacion completa con PageResponse
- ✅ Filtros dinamicos (categoria, busqueda, ordenamiento)

---

### RA7.i: INTERACCION CON USUARIO

#### Puntuacion: **0.5/0.5**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No hay interaccion | - |
| 0.25 | Eventos basicos | - |
| **0.5** | **Debounce + Subject + eventos complejos + modales** | ✅ |

#### Evidencia: Debounce en Busqueda

**Archivo:** [`src/app/components/shared/order-filters/order-filters.ts`](../../src/app/components/shared/order-filters/order-filters.ts) (lineas 44-82)

```typescript
/** Subject for debounced search input */
private readonly searchSubject = new Subject<string>();

constructor() {
  // Debounce search input to avoid excessive filtering
  this.searchSubject.pipe(
    debounceTime(300),
    distinctUntilChanged(),
    takeUntilDestroyed(this.destroyRef)
  ).subscribe(value => {
    this.searchChange.emit(value);
  });
}

/** Handle search input with debounce */
protected onSearchInput(event: Event): void {
  const target = event.target as HTMLInputElement;
  this.searchSubject.next(target.value);
}
```

#### Evidencia: BehaviorSubject en Token Refresh

**Archivo:** [`src/app/core/interceptors/auth.interceptor.ts`](../../src/app/core/interceptors/auth.interceptor.ts)

```typescript
let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

// Queue requests during refresh
if (isRefreshing) {
  return refreshTokenSubject.pipe(
    filter(token => token !== null),
    take(1),
    switchMap(token => next(addAuthHeader(req, token!)))
  );
}
```

#### Evidencia: Modal con Focus Trap

**Archivo:** [`src/app/components/shared/modal/modal.ts`](../../src/app/components/shared/modal/modal.ts)

```typescript
protected onKeydown(event: KeyboardEvent): void {
  if (event.key === 'Escape' && this.closeOnEsc()) {
    event.preventDefault();
    this.closeRequest.emit();
    return;
  }

  // Focus trap
  if (event.key === 'Tab') {
    if (event.shiftKey) {
      if (this.document.activeElement === firstElement) {
        event.preventDefault();
        lastElement.focus();
      }
    } else {
      if (this.document.activeElement === lastElement) {
        event.preventDefault();
        firstElement.focus();
      }
    }
  }
}
```

#### Justificacion de la Puntuacion

**0.5/0.5** porque:
- ✅ `debounceTime(300)` en busqueda
- ✅ `distinctUntilChanged()` para evitar duplicados
- ✅ `Subject` para searchSubject
- ✅ `BehaviorSubject` para token refresh queue
- ✅ Modal con focus trap completo
- ✅ `throttleTime(5000)` en wallet polling
- ✅ Eventos complejos (click, input, blur, keydown, mousemove)

---

## FASE 7: TESTING Y CALIDAD

### RA6.f: HERRAMIENTAS DE DEPURACION Y DOCUMENTACION

#### Puntuacion: **1.0/1.0**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | Sin tests ni documentacion | - |
| 0.5 | Pocos tests + poca documentacion | - |
| 0.75 | Tests parciales + algo de documentacion | - |
| **1.0** | **Tests componentes (3+) + tests servicios (3+) + coverage + docs** | ✅ |

#### Tests de Componentes (4 archivos, 25 tests)

| Archivo | Tests | Patrones |
|---------|:-----:|----------|
| [`app.spec.ts`](../../src/app/app.spec.ts) | 2 | TestBed, window.matchMedia mock |
| [`dashboard.spec.ts`](../../src/app/pages/dashboard/dashboard.spec.ts) | 6 | vi.fn(), provideRouter, signals |
| [`login.spec.ts`](../../src/app/pages/login/login.spec.ts) | 8 | ActivatedRoute mock, query params |
| [`orders.spec.ts`](../../src/app/pages/orders/orders.spec.ts) | 9 | OrderService mock, filtrado |

#### Tests de Servicios (3 archivos, 54 tests)

| Archivo | Tests | Patrones |
|---------|:-----:|----------|
| [`order.service.spec.ts`](../../src/app/core/services/order.service.spec.ts) | 12 | HttpTestingController, expectOne, flush |
| [`auth.service.spec.ts`](../../src/app/core/services/auth.service.spec.ts) | 18 | HttpTestingController, TokenService mock |
| [`token.service.spec.ts`](../../src/app/core/services/token.service.spec.ts) | 24 | localStorage mock, vi.setSystemTime |

**Total: 79 tests**

#### Evidencia: Test con HttpTestingController

**Archivo:** [`src/app/core/services/order.service.spec.ts`](../../src/app/core/services/order.service.spec.ts)

```typescript
describe('OrderService', () => {
  let service: OrderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        OrderService
      ]
    });
    service = TestBed.inject(OrderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch paginated orders', () => {
    service.getOrders(0, 20).subscribe(response => {
      expect(response.content.length).toBe(1);
      expect(response.totalElements).toBe(1);
    });

    const req = httpMock.expectOne('/api/v1/orders?page=0&size=20');
    expect(req.request.method).toBe('GET');
    req.flush(mockPageResponse);
  });
});
```

#### Evidencia: window.matchMedia Mock

**Archivo:** [`src/app/pages/dashboard/dashboard.spec.ts`](../../src/app/pages/dashboard/dashboard.spec.ts)

```typescript
describe('Dashboard', () => {
  beforeAll(() => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation((query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    });
  });
});
```

#### Configuracion de Coverage

**Archivo:** [`package.json`](../../package.json)

```json
{
  "scripts": {
    "test": "ng test",
    "test:coverage": "ng test --coverage"
  }
}
```

**Archivo:** [`angular.json`](../../angular.json)

```json
{
  "test": {
    "builder": "@angular/build:unit-test",
    "options": {
      "coverage": {
        "enabled": false,
        "thresholds": {
          "statements": 50,
          "branches": 50,
          "functions": 50,
          "lines": 50
        }
      }
    }
  }
}
```

#### Documentacion

| Archivo | Lineas | Contenido |
|---------|:------:|-----------|
| [`ROUTES.md`](./ROUTES.md) | 121 | Mapa completo de rutas, guards, resolvers |
| [`DOCUMENTACION.md`](./DOCUMENTACION.md) | 1246+ | Fases 1-3 detalladas |
| [`justificacion_ra_fase1_2_y_3.md`](./justificacion_ra_fase1_2_y_3.md) | 1520+ | Evidencia por criterio |

#### Justificacion de la Puntuacion

**1.0/1.0** porque:
- ✅ Tests de componentes: 4 archivos con 25 tests
- ✅ Tests de servicios: 3 archivos con 54 tests
- ✅ HttpTestingController en OrderService y AuthService
- ✅ Mocks correctos: vi.fn(), useValue, localStorage
- ✅ window.matchMedia mock en 4 archivos
- ✅ Script test:coverage en package.json
- ✅ Coverage thresholds 50% en angular.json
- ✅ ROUTES.md con documentacion completa

---

## ARCHIVOS DE REFERENCIA

### Fase 4 - Sistema de Rutas

| Archivo | Descripcion |
|---------|-------------|
| [`src/app/app.routes.ts`](../../src/app/app.routes.ts) | 88 lineas, 13 rutas lazy |
| [`src/app/app.config.ts`](../../src/app/app.config.ts) | provideRouter con PreloadAllModules |
| [`src/app/core/guards/auth.guard.ts`](../../src/app/core/guards/auth.guard.ts) | authGuard, authGuardChild |
| [`src/app/core/guards/guest.guard.ts`](../../src/app/core/guards/guest.guard.ts) | guestGuard |
| [`src/app/core/guards/root.guard.ts`](../../src/app/core/guards/root.guard.ts) | rootGuard |
| [`src/app/core/guards/pending-changes.guard.ts`](../../src/app/core/guards/pending-changes.guard.ts) | pendingChangesGuard |
| [`src/app/core/resolvers/order.resolver.ts`](../../src/app/core/resolvers/order.resolver.ts) | orderResolver |
| [`src/app/core/services/breadcrumb.service.ts`](../../src/app/core/services/breadcrumb.service.ts) | BreadcrumbService |
| [`src/app/components/shared/breadcrumb/`](../../src/app/components/shared/breadcrumb/) | Componente breadcrumb |

### Fase 5 - Comunicacion HTTP

| Archivo | Descripcion |
|---------|-------------|
| [`src/app/core/interceptors/auth.interceptor.ts`](../../src/app/core/interceptors/auth.interceptor.ts) | Token + refresh automatico |
| [`src/app/core/interceptors/loading.interceptor.ts`](../../src/app/core/interceptors/loading.interceptor.ts) | Loading states |
| [`src/app/core/services/auth.service.ts`](../../src/app/core/services/auth.service.ts) | Login, register, logout |
| [`src/app/core/services/order.service.ts`](../../src/app/core/services/order.service.ts) | CRUD + retry logic |
| [`src/app/core/services/user.service.ts`](../../src/app/core/services/user.service.ts) | Estadisticas + retry |
| [`src/app/core/services/invoice.service.ts`](../../src/app/core/services/invoice.service.ts) | Pagos |

### Fase 6 - Gestion del Estado

| Archivo | Descripcion |
|---------|-------------|
| [`src/app/core/services/token.service.ts`](../../src/app/core/services/token.service.ts) | localStorage tokens + signals |
| [`src/app/services/theme.service.ts`](../../src/app/services/theme.service.ts) | localStorage tema + signals + cross-tab |
| [`src/app/components/shared/auth-form/`](../../src/app/components/shared/auth-form/) | Reactive Forms |
| [`src/app/pages/formarray-demo/`](../../src/app/pages/formarray-demo/) | FormArray dinamico |
| [`src/app/components/shared/order-filters/`](../../src/app/components/shared/order-filters/) | Debounce search |
| [`src/app/pages/orders/`](../../src/app/pages/orders/) | Filtros + paginacion |

### Fase 7 - Testing

| Archivo | Descripcion |
|---------|-------------|
| [`src/app/app.spec.ts`](../../src/app/app.spec.ts) | 2 tests |
| [`src/app/pages/dashboard/dashboard.spec.ts`](../../src/app/pages/dashboard/dashboard.spec.ts) | 6 tests |
| [`src/app/pages/login/login.spec.ts`](../../src/app/pages/login/login.spec.ts) | 8 tests |
| [`src/app/pages/orders/orders.spec.ts`](../../src/app/pages/orders/orders.spec.ts) | 9 tests |
| [`src/app/core/services/order.service.spec.ts`](../../src/app/core/services/order.service.spec.ts) | 12 tests |
| [`src/app/core/services/auth.service.spec.ts`](../../src/app/core/services/auth.service.spec.ts) | 18 tests |
| [`src/app/core/services/token.service.spec.ts`](../../src/app/core/services/token.service.spec.ts) | 24 tests |
| [`docs/client/ROUTES.md`](./ROUTES.md) | Documentacion de rutas |

---

## DECISIONES ARQUITECTONICAS ANGULAR 21

### Por que Guards Funcionales (CanActivateFn)

**Decision:** Usar CanActivateFn en lugar de class-based guards

**Justificacion tecnica:**

| Aspecto | Class-Based (Deprecated) | Functional (Angular 21) |
|---------|--------------------------|-------------------------|
| Sintaxis | @Injectable + implements | export const |
| Tree-shaking | No | Si |
| Bundle size | Mayor | Menor |
| Testabilidad | Requiere DI setup | Mas simple |
| Documentacion | Legacy | Recomendado |

```typescript
// ❌ Deprecated (Angular 15.2+)
@Injectable()
export class AuthGuard implements CanActivate {
  canActivate(route, state) { ... }
}

// ✅ Angular 21 - Funcional
export const authGuard: CanActivateFn = (route, state) => {
  const tokenService = inject(TokenService);
  return tokenService.isAuthenticated();
};
```

### Por que Interceptores Funcionales (HttpInterceptorFn)

**Decision:** Usar HttpInterceptorFn en lugar de HTTP_INTERCEPTORS

**Justificacion tecnica:**

```typescript
// ❌ Deprecated
providers: [
  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
]

// ✅ Angular 21 - Funcional
provideHttpClient(withInterceptors([authInterceptor, loadingInterceptor]))
```

### Por que provideHttpClient (no HttpClientModule)

**Decision:** Usar provideHttpClient() en standalone app

**Referencia Angular 21:**
- HttpClientModule esta deprecated para apps standalone
- provideHttpClient() es la API recomendada
- Mejor tree-shaking y configuracion mas limpia

---

## CONCLUSION

### Resumen de Puntuaciones

| RA | Descripcion | Puntos |
|:--:|-------------|:------:|
| RA6.g | Navegacion entre contenidos | **1.5/1.5** |
| RA6.h | Mecanismos historial/navegacion | **1.5/1.5** |
| RA7.a | Comunicacion asincrona | **0.75/0.75** |
| RA7.b | Formatos envio/recepcion | **0.75/0.75** |
| RA7.c | Librerias comunicacion | **0.75/0.75** |
| RA7.d | Modificaciones dinamicas | **0.75/0.75** |
| RA7.e | Objetos del navegador | **0.5/0.5** |
| RA7.f | Objetos del documento | **0.5/0.5** |
| RA7.h | Objetos dinamicos | **0.5/0.5** |
| RA7.i | Interaccion usuario | **0.5/0.5** |
| RA6.f | Testing y documentacion | **1.0/1.0** |
| **TOTAL** | | **9.0/9.0** |

### Porcentaje Final: **100%**

### Principales Fortalezas

1. **Sistema de rutas completo** - Guards funcionales, resolver, lazy loading 100%, breadcrumbs
2. **Interceptores funcionales** - authInterceptor con token refresh, loadingInterceptor
3. **Retry logic** - Exponential backoff en servicios HTTP
4. **Signals everywhere** - 30+ signals, 20+ computed, 4+ effects
5. **Reactive Forms avanzados** - FormArray dinamico, validadores async
6. **Testing exhaustivo** - 79 tests con HttpTestingController y mocks
7. **Debounce implementado** - 300ms en busqueda de ordenes
8. **Cross-tab sync** - Sincronizacion de tema entre pestanas
9. **Documentacion completa** - ROUTES.md con mapa de rutas

### Patrones Angular 21 Implementados

| Patron | Ubicacion |
|--------|-----------|
| Standalone components | Todos los componentes |
| Signals (signal, computed, effect) | 15+ archivos |
| Functional guards (CanActivateFn) | 4 guards |
| Functional interceptors (HttpInterceptorFn) | 2 interceptores |
| Functional resolvers (ResolveFn) | 1 resolver |
| provideHttpClient() | app.config.ts |
| provideRouter() | app.config.ts |
| takeUntilDestroyed() | Todos los componentes |
| viewChild() signal API | 5+ componentes |
| input() signal API | Todos los componentes |
| output() signal API | Todos los componentes |

---

**Documento generado:** Enero 2026
**Framework:** Angular 21
**Autor:** Estudiante
