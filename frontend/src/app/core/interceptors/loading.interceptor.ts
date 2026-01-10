import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { LoadingService } from '../../services/loading.service';

/**
 * URLs que deben ser ignoradas por el interceptor de loading.
 * Por ejemplo, requests de validacion asincrona que no queremos
 * que activen el spinner global.
 */
const IGNORED_URLS: string[] = [
  '/api/check-email',
  '/api/check-username',
  // Catalog lookups during intelligent input typing - these are quick background lookups
  // that shouldn't trigger the global spinner (prevents flickering)
  '/api/v1/public/categories',
  '/types/'
];

/**
 * Verifica si una URL debe ser ignorada por el interceptor.
 */
function shouldIgnoreUrl(url: string): boolean {
  return IGNORED_URLS.some(ignoredUrl => url.includes(ignoredUrl));
}

/**
 * HTTP Interceptor para gestionar estados de carga.
 *
 * Intercepta todas las peticiones HTTP y:
 * - Muestra el estado de carga al iniciar la peticion
 * - Oculta el estado de carga al completar (exito o error)
 * - Maneja multiples peticiones concurrentes
 *
 * @example
 * ```typescript
 * // En app.config.ts
 * import { provideHttpClient, withInterceptors } from '@angular/common/http';
 * import { loadingInterceptor } from './core/interceptors/loading.interceptor';
 *
 * export const appConfig: ApplicationConfig = {
 *   providers: [
 *     provideHttpClient(withInterceptors([loadingInterceptor]))
 *   ]
 * };
 * ```
 */
export const loadingInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const loadingService = inject(LoadingService);

  // Ignorar ciertas URLs
  if (shouldIgnoreUrl(req.url)) {
    return next(req);
  }

  // Mostrar loading
  loadingService.show();

  // Ocultar loading al finalizar (exito o error)
  return next(req).pipe(
    finalize(() => loadingService.hide())
  );
};

/**
 * Crea un interceptor personalizado con URLs ignoradas adicionales.
 *
 * @param additionalIgnoredUrls - URLs adicionales a ignorar
 * @returns HttpInterceptorFn configurado
 *
 * @example
 * ```typescript
 * const customInterceptor = createLoadingInterceptor(['/api/polling']);
 * ```
 */
export function createLoadingInterceptor(
  additionalIgnoredUrls: string[] = []
): HttpInterceptorFn {
  const allIgnoredUrls = [...IGNORED_URLS, ...additionalIgnoredUrls];

  return (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
    const loadingService = inject(LoadingService);

    if (allIgnoredUrls.some(url => req.url.includes(url))) {
      return next(req);
    }

    loadingService.show();
    return next(req).pipe(
      finalize(() => loadingService.hide())
    );
  };
}
