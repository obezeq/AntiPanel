import { CanDeactivateFn } from '@angular/router';

/**
 * Interface for components with unsaved form changes.
 *
 * Implement this interface in components that have forms which
 * should prompt users before navigating away with unsaved changes.
 *
 * @example
 * ```typescript
 * export class Register implements HasUnsavedChanges {
 *   hasUnsavedChanges(): boolean {
 *     return this.authForm()?.form.dirty ?? false;
 *   }
 * }
 * ```
 */
export interface HasUnsavedChanges {
  hasUnsavedChanges(): boolean;
}

/**
 * CanDeactivate Guard for Unsaved Form Changes
 *
 * Functional guard that prompts users before leaving a page with unsaved changes.
 * Uses the native browser confirm dialog for consistency across browsers.
 *
 * @example
 * ```typescript
 * // In app.routes.ts
 * {
 *   path: 'register',
 *   loadComponent: () => import('./pages/register/register').then(m => m.Register),
 *   canDeactivate: [pendingChangesGuard]
 * }
 * ```
 *
 * @param component - Component implementing HasUnsavedChanges interface
 * @returns true if navigation allowed, false to block
 */
export const pendingChangesGuard: CanDeactivateFn<HasUnsavedChanges> = (component) => {
  // If component implements hasUnsavedChanges and has unsaved changes, prompt
  if (component.hasUnsavedChanges?.()) {
    return confirm('You have unsaved changes. Are you sure you want to leave?');
  }

  return true;
};
