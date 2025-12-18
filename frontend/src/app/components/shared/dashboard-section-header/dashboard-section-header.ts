import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Dashboard section header component
 * Displays a large title with an optional subtitle in monospace font.
 * Used for section headers in dashboard-style pages.
 *
 * @example
 * <app-dashboard-section-header
 *   title="RECENT"
 *   subtitle="Your latest orders and their status"
 * />
 */
@Component({
  selector: 'app-dashboard-section-header',
  templateUrl: './dashboard-section-header.html',
  styleUrl: './dashboard-section-header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardSectionHeader {
  /** Main title text (displayed in large bold font) */
  readonly title = input.required<string>();

  /** Optional subtitle text (displayed in monospace font) */
  readonly subtitle = input<string>();

  /** Text alignment */
  readonly align = input<'left' | 'center' | 'right'>('center');
}
