import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { StatsCard, type StatsCardData, type StatsCardVariant } from '../../../../components/shared/stats-card/stats-card';
import type { UserStatisticsResponse } from '../../../../core/services/user.service';

/**
 * Dashboard overview section component.
 * Displays the branded header with balance and stats cards.
 *
 * @example
 * <app-dashboard-overview
 *   [stats]="userStats()"
 *   [balance]="formattedBalance()"
 *   (addFunds)="goToWallet()"
 * />
 */
@Component({
  selector: 'app-dashboard-overview',
  templateUrl: './dashboard-overview.html',
  styleUrl: './dashboard-overview.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [StatsCard]
})
export class DashboardOverview {
  /** User statistics from API */
  readonly stats = input.required<UserStatisticsResponse>();

  /** Formatted balance string (e.g., "$150.50") */
  readonly balance = input.required<string>();

  /** Emits when "Add Funds" button is clicked */
  readonly addFunds = output<void>();

  /** Stats cards data computed from user statistics */
  protected readonly statsCards = computed<Array<{ data: StatsCardData; variant: StatsCardVariant }>>(() => {
    const stats = this.stats();
    return [
      {
        data: {
          icon: 'matShowChart',
          title: 'TOTAL',
          value: stats.totalOrders,
          label: 'All time orders'
        },
        variant: 'default' as StatsCardVariant
      },
      {
        data: {
          icon: 'matSchedule',
          title: 'PENDING',
          value: stats.pendingOrders,
          label: 'In progress'
        },
        variant: 'warning' as StatsCardVariant
      },
      {
        data: {
          icon: 'matCheckCircle',
          title: 'STATUS',
          value: stats.completedOrders,
          label: 'Completed'
        },
        variant: 'success' as StatsCardVariant
      },
      {
        data: {
          icon: 'matQueryStats',
          title: 'STATUS',
          value: stats.ordersThisMonth,
          label: 'This month'
        },
        variant: 'info' as StatsCardVariant
      }
    ];
  });

  /**
   * Handle "Add Funds" button click.
   * Emits event for parent to navigate to wallet.
   */
  protected onAddFundsClick(): void {
    this.addFunds.emit();
  }
}
