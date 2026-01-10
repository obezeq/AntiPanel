import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { NgIcon } from '@ng-icons/core';

export type StatsCardVariant = 'default' | 'success' | 'info' | 'warning';

export interface StatsCardData {
  /** Icon name from ng-icons (e.g., 'matActivity') */
  icon: string;
  /** Card title (e.g., 'TOTAL', 'PENDING', 'STATUS') */
  title: string;
  /** Numeric value or text to display */
  value: string | number;
  /** Description below the value */
  label: string;
}

@Component({
  selector: 'app-stats-card',
  templateUrl: './stats-card.html',
  styleUrl: './stats-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon]
})
export class StatsCard {
  /** Stats data */
  readonly stats = input.required<StatsCardData>();

  /** Card visual variant - affects the value color */
  readonly variant = input<StatsCardVariant>('default');
}
