/**
 * HTTP Interceptors
 *
 * Functional interceptors for Angular 21+ HTTP client.
 *
 * @example
 * ```typescript
 * // In app.config.ts
 * import { provideHttpClient, withInterceptors } from '@angular/common/http';
 * import { authInterceptor, loadingInterceptor } from './core/interceptors';
 *
 * export const appConfig: ApplicationConfig = {
 *   providers: [
 *     provideHttpClient(withInterceptors([authInterceptor, loadingInterceptor]))
 *   ]
 * };
 * ```
 */

export { authInterceptor } from './auth.interceptor';
export { loadingInterceptor, createLoadingInterceptor } from './loading.interceptor';
