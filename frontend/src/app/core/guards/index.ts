/**
 * Route Guards
 *
 * Functional guards for Angular 21+ route protection.
 *
 * @example
 * ```typescript
 * import { authGuard, guestGuard, pendingChangesGuard } from './core/guards';
 *
 * const routes: Routes = [
 *   { path: 'dashboard', canActivate: [authGuard], ... },
 *   { path: 'login', canActivate: [guestGuard], ... },
 *   { path: 'register', canDeactivate: [pendingChangesGuard], ... }
 * ];
 * ```
 */

export { authGuard, authGuardChild } from './auth.guard';
export { guestGuard } from './guest.guard';
export { rootGuard } from './root.guard';
export { pendingChangesGuard, type HasUnsavedChanges } from './pending-changes.guard';
