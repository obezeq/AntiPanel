import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BreadcrumbService } from '../../../core/services/breadcrumb.service';

/**
 * Breadcrumb Navigation Component
 *
 * Displays hierarchical navigation trail based on current route.
 * Uses semantic HTML with nav, ol, and li elements for accessibility.
 *
 * @example
 * ```html
 * <app-breadcrumb />
 * ```
 *
 * Output: Dashboard / Orders / Order Details
 */
@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.html',
  styleUrl: './breadcrumb.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink]
})
export class Breadcrumb {
  private readonly breadcrumbService = inject(BreadcrumbService);

  /** Breadcrumb items from service */
  protected readonly breadcrumbs = this.breadcrumbService.breadcrumbs;
}
