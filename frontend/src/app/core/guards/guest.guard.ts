import { inject } from '@angular/core';
import { Router, type CanActivateFn } from '@angular/router';
import { TokenService } from '../services/token.service';

/**
 * Guest guard that protects routes for unauthenticated users only.
 *
 * Redirects authenticated users away from login/register pages.
 * Prevents logged-in users from accessing auth pages.
 *
 * Usage in routes:
 * ```typescript
 * {
 *   path: 'login',
 *   loadComponent: () => import('./pages/login/login'),
 *   canActivate: [guestGuard]
 * }
 * ```
 */
export const guestGuard: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (!tokenService.isAuthenticated()) {
    // User is not logged in, allow access to guest pages
    return true;
  }

  // User is logged in, redirect to dashboard
  return router.createUrlTree(['/dashboard']);
};
