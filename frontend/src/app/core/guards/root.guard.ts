import { inject } from '@angular/core';
import { Router, type CanActivateFn } from '@angular/router';
import { TokenService } from '../services/token.service';

/**
 * Root path redirect guard.
 *
 * Redirects users based on authentication state:
 * - Authenticated users → /dashboard
 * - Unauthenticated users → /home
 *
 * Usage in routes:
 * ```typescript
 * {
 *   path: '',
 *   canActivate: [rootGuard],
 *   children: []
 * }
 * ```
 */
export const rootGuard: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (tokenService.isAuthenticated()) {
    return router.createUrlTree(['/dashboard']);
  }

  return router.createUrlTree(['/home']);
};
