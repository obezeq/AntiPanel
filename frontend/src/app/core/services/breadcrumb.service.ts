import { Injectable, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

/**
 * Breadcrumb item representing a navigation path segment
 */
export interface Breadcrumb {
  /** Display label for the breadcrumb */
  label: string;
  /** Route URL for navigation */
  url: string;
}

/**
 * Breadcrumb Service
 *
 * Automatically generates breadcrumb navigation from route configuration.
 * Reads `data.breadcrumb` from route definitions and builds the path.
 *
 * @example
 * ```typescript
 * // In app.routes.ts
 * {
 *   path: 'orders',
 *   data: { breadcrumb: 'Orders' }
 * },
 * {
 *   path: 'orders/:id',
 *   data: { breadcrumb: 'Order Details' }
 * }
 *
 * // In component
 * readonly breadcrumbs = inject(BreadcrumbService).breadcrumbs;
 * ```
 */
@Injectable({ providedIn: 'root' })
export class BreadcrumbService {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  /** Current breadcrumb trail as readonly signal */
  private readonly _breadcrumbs = signal<Breadcrumb[]>([]);
  readonly breadcrumbs = this._breadcrumbs.asReadonly();

  constructor() {
    // Build initial breadcrumbs
    this._breadcrumbs.set(this.buildBreadcrumbs(this.activatedRoute.root));

    // Update breadcrumbs on navigation
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this._breadcrumbs.set(this.buildBreadcrumbs(this.activatedRoute.root));
    });
  }

  /**
   * Recursively build breadcrumbs from route tree
   */
  private buildBreadcrumbs(
    route: ActivatedRoute,
    url = '',
    breadcrumbs: Breadcrumb[] = []
  ): Breadcrumb[] {
    const children = route.children;

    if (children.length === 0) {
      return breadcrumbs;
    }

    for (const child of children) {
      const routeUrl = child.snapshot.url.map(segment => segment.path).join('/');

      if (routeUrl) {
        url += `/${routeUrl}`;
      }

      const label = child.snapshot.data['breadcrumb'];

      if (label) {
        breadcrumbs.push({ label, url });
      }

      return this.buildBreadcrumbs(child, url, breadcrumbs);
    }

    return breadcrumbs;
  }
}
