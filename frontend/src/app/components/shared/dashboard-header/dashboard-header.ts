import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-dashboard-header',
  templateUrl: './dashboard-header.html',
  styleUrl: './dashboard-header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardHeader {
  /** Main title */
  readonly title = input.required<string>();

  /** Subtitle / description */
  readonly subtitle = input<string>('');

  /** Whether to center the content */
  readonly centered = input<boolean>(true);
}
