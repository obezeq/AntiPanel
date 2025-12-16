import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export type OrderStatus = 'pending' | 'processing' | 'completed' | 'cancelled' | 'partial';

@Component({
  selector: 'app-admin-order-status-badge',
  template: `
    <span
      class="status-badge"
      [class.status-badge--pending]="status() === 'pending'"
      [class.status-badge--processing]="status() === 'processing'"
      [class.status-badge--completed]="status() === 'completed'"
      [class.status-badge--cancelled]="status() === 'cancelled'"
      [class.status-badge--partial]="status() === 'partial'"
      [attr.aria-label]="'Status: ' + statusLabel()"
    >
      {{ statusLabel() }}
    </span>
  `,
  styles: `
    @use '../../../../styles/01-tools/mixins' as *;

    .status-badge {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding-block: 0.125rem;
      padding-inline: 0.5rem;
      border-radius: var(--radius-sm);
      border-width: var(--border-width-thin);
      border-style: solid;
      font-family: var(--font-primary);
      font-size: var(--font-size-tiny);
      font-weight: var(--font-weight-medium);
      line-height: var(--line-height-tight);
      text-transform: uppercase;
      white-space: nowrap;
    }

    .status-badge--pending {
      color: var(--color-status-yellow);
      border-color: rgba(240, 177, 0, 0.66);
    }

    .status-badge--processing {
      color: var(--color-stats-blue);
      border-color: rgba(0, 165, 255, 0.66);
    }

    .status-badge--completed {
      color: var(--color-success);
      border-color: rgba(0, 220, 51, 0.66);
    }

    .status-badge--cancelled {
      color: var(--color-error);
      border-color: rgba(255, 68, 68, 0.66);
    }

    .status-badge--partial {
      color: var(--color-foreground);
      border-color: rgba(161, 161, 161, 0.66);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminOrderStatusBadge {
  readonly status = input.required<OrderStatus>();

  protected readonly statusLabel = computed(() => {
    const labels: Record<OrderStatus, string> = {
      pending: 'PENDING',
      processing: 'PROCESSING',
      completed: 'COMPLETED',
      cancelled: 'CANCELLED',
      partial: 'PARTIAL'
    };
    return labels[this.status()];
  });
}
