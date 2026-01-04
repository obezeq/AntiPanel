/**
 * Route Guards
 *
 * Functional guards for Angular 21+ route protection.
 *
 * @example
 * ```typescript
 * import { authGuard, guestGuard } from './core/guards';
 *
 * const routes: Routes = [
 *   { path: 'dashboard', canActivate: [authGuard], ... },
 *   { path: 'login', canActivate: [guestGuard], ... }
 * ];
 * ```
 */

export { authGuard, authGuardChild } from './auth.guard';
export { guestGuard } from './guest.guard';
