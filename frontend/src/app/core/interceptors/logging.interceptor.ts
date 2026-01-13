import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

/**
 * HTTP Logging Interceptor
 *
 * Logs HTTP requests and responses in development mode.
 * Automatically disabled in production to avoid console noise.
 *
 * Logged information:
 * - Request method and URL
 * - Response status and timing
 * - Error details (if any)
 *
 * @example Console output
 * ```
 * [HTTP] GET /api/v1/orders
 * [HTTP] GET /api/v1/orders completed in 245ms (200)
 * ```
 *
 * @remarks
 * This interceptor is registered in app.config.ts:
 * ```typescript
 * provideHttpClient(withInterceptors([
 *   authInterceptor,
 *   loadingInterceptor,
 *   loggingInterceptor
 * ]))
 * ```
 */
export const loggingInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip logging in production
  if (environment.production) {
    return next(req);
  }

  const started = Date.now();
  const requestId = Math.random().toString(36).substring(7);

  // Log request
  console.log(
    `%c[HTTP] ${req.method} ${req.url}`,
    'color: #2196F3; font-weight: bold;',
    {
      requestId,
      headers: req.headers.keys(),
      body: req.body
    }
  );

  return next(req).pipe(
    tap({
      next: (event) => {
        if (event instanceof HttpResponse) {
          const elapsed = Date.now() - started;
          const statusColor = event.status < 400 ? '#4CAF50' : '#F44336';

          console.log(
            `%c[HTTP] ${req.method} ${req.url} completed in ${elapsed}ms (${event.status})`,
            `color: ${statusColor}; font-weight: bold;`,
            {
              requestId,
              status: event.status,
              statusText: event.statusText,
              body: event.body
            }
          );
        }
      },
      error: (error) => {
        const elapsed = Date.now() - started;

        console.error(
          `%c[HTTP] ${req.method} ${req.url} failed in ${elapsed}ms`,
          'color: #F44336; font-weight: bold;',
          {
            requestId,
            status: error.status,
            message: error.message,
            error: error.error
          }
        );
      }
    })
  );
};
