import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take, Observable } from 'rxjs';
import { TokenService } from '../services/token.service';
import { AuthService } from '../services/auth.service';

// ============================================================================
// Token Refresh State
// ============================================================================

/** Whether a token refresh is in progress */
let isRefreshing = false;

/** Subject to queue requests while refreshing */
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

// ============================================================================
// URLs to Skip
// ============================================================================

/**
 * URLs that should not have the Authorization header added.
 * Includes auth endpoints that don't require tokens.
 */
const SKIP_AUTH_URLS = [
  '/auth/login',
  '/auth/register',
  '/auth/refresh',
  '/auth/check-email'
];

/**
 * Checks if a URL should skip auth header.
 */
function shouldSkipAuth(url: string): boolean {
  return SKIP_AUTH_URLS.some(skipUrl => url.includes(skipUrl));
}

// ============================================================================
// Auth Interceptor
// ============================================================================

/**
 * HTTP Interceptor for authentication.
 *
 * Features:
 * - Adds Authorization header to requests
 * - Handles 401 responses with automatic token refresh
 * - Queues requests during token refresh
 * - Redirects to login on refresh failure
 *
 * @example
 * ```typescript
 * // In app.config.ts
 * import { provideHttpClient, withInterceptors } from '@angular/common/http';
 * import { authInterceptor } from './core/interceptors/auth.interceptor';
 *
 * export const appConfig: ApplicationConfig = {
 *   providers: [
 *     provideHttpClient(withInterceptors([authInterceptor]))
 *   ]
 * };
 * ```
 */
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const tokenService = inject(TokenService);
  const authService = inject(AuthService);
  const router = inject(Router);

  // Skip auth header for certain URLs
  if (shouldSkipAuth(req.url)) {
    return next(req);
  }

  // Add auth header if token exists
  const accessToken = tokenService.getAccessToken();
  let authReq = req;

  if (accessToken) {
    authReq = addAuthHeader(req, accessToken);
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Handle 401 Unauthorized
      if (error.status === 401 && !shouldSkipAuth(req.url)) {
        return handle401Error(req, next, tokenService, authService, router);
      }

      return throwError(() => error);
    })
  );
};

// ============================================================================
// Helper Functions
// ============================================================================

/**
 * Adds Authorization header to request.
 */
function addAuthHeader(
  req: HttpRequest<unknown>,
  token: string
): HttpRequest<unknown> {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}

/**
 * Handles 401 errors by attempting token refresh.
 */
function handle401Error(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  tokenService: TokenService,
  authService: AuthService,
  router: Router
): Observable<HttpEvent<unknown>> {
  // Check if we can refresh
  const refreshToken = tokenService.getRefreshToken();

  if (!refreshToken) {
    // No refresh token, redirect to login
    return handleLogout(tokenService, router);
  }

  if (!isRefreshing) {
    // Start refresh process
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshToken(refreshToken).pipe(
      switchMap((response) => {
        isRefreshing = false;

        // Store new tokens
        tokenService.setTokens(
          response.accessToken,
          response.refreshToken,
          response.expiresIn,
          response.user
        );

        // Notify waiting requests
        refreshTokenSubject.next(response.accessToken);

        // Retry original request with new token
        return next(addAuthHeader(req, response.accessToken));
      }),
      catchError((refreshError) => {
        isRefreshing = false;
        refreshTokenSubject.next(null);

        // Refresh failed, logout and redirect
        return handleLogout(tokenService, router);
      })
    );
  }

  // Refresh already in progress, wait for it
  return refreshTokenSubject.pipe(
    filter((token): token is string => token !== null),
    take(1),
    switchMap((token) => next(addAuthHeader(req, token)))
  );
}

/**
 * Handles logout by clearing tokens and redirecting.
 */
function handleLogout(
  tokenService: TokenService,
  router: Router
): Observable<never> {
  tokenService.clearTokens();

  // Get current URL for return after login
  const returnUrl = router.url;

  router.navigate(['/login'], {
    queryParams: { returnUrl, sessionExpired: 'true' }
  });

  return throwError(() => new Error('Session expired'));
}
