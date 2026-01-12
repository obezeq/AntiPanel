import { inject } from '@angular/core';
import { Router, type CanActivateFn, type CanActivateChildFn } from '@angular/router';
import { TokenService } from '../services/token.service';

/**
 * Auth guard that protects routes requiring authentication.
 *
 * Checks if user is authenticated via TokenService.
 * Redirects to login page if not authenticated, preserving the intended URL.
 *
 * Usage in routes:
 * ```typescript
 * {
 *   path: 'dashboard',
 *   loadComponent: () => import('./pages/dashboard/dashboard'),
 *   canActivate: [authGuard]
 * }
 * ```
 */
export const authGuard: CanActivateFn = (route, state) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (tokenService.isAuthenticated()) {
    return true;
  }

  // Store the attempted URL for redirecting after login
  const returnUrl = state.url;

  // Redirect to login with return URL
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl }
  });
};

/**
 * Auth guard for child routes.
 * Same logic as authGuard but for canActivateChild.
 *
 * Usage in routes:
 * ```typescript
 * {
 *   path: 'admin',
 *   canActivateChild: [authGuardChild],
 *   children: [...]
 * }
 * ```
 */
export const authGuardChild: CanActivateChildFn = (childRoute, state) => {
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
