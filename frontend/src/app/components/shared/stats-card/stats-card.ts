import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export type StatsCardVariant = 'default' | 'success' | 'info' | 'warning' | 'error';

export interface StatsCardData {
  value: string | number;
  label: string;
  change?: number;
  changeLabel?: string;
}

@Component({
  selector: 'app-stats-card',
  templateUrl: './stats-card.html',
  styleUrl: './stats-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatsCard {
  /** Stats data */
  readonly stats = input.required<StatsCardData>();

  /** Card visual variant */
  readonly variant = input<StatsCardVariant>('default');

  /** Whether to show the change indicator */
  readonly showChange = input<boolean>(true);

  /** Computed: whether change is positive */
  protected readonly isPositiveChange = computed(() => {
    const change = this.stats().change;
    return change !== undefined && change > 0;
  });

  /** Computed: whether change is negative */
  protected readonly isNegativeChange = computed(() => {
    const change = this.stats().change;
    return change !== undefined && change < 0;
  });

  /** Computed: formatted change value */
  protected readonly formattedChange = computed(() => {
    const change = this.stats().change;
    if (change === undefined) return '';
    const sign = change > 0 ? '+' : '';
    return `${sign}${change}%`;
  });
}
