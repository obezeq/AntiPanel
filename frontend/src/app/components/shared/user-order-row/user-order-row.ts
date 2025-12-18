import { ChangeDetectionStrategy, Component, input, output, computed } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';

export type OrderStatus = 'pending' | 'processing' | 'completed' | 'cancelled' | 'partial';

/**
 * Order row variant:
 * - user: Simple view with service name, quantity, price, status
 * - admin: Extended view with user info, start count, remains, actions
 */
export type OrderRowVariant = 'user' | 'admin';

export interface UserOrder {
  id: string;
  serviceName: string;
  quantity: number;
  price: string;
  status: OrderStatus;
  createdAt: Date;
  /** For admin variant: username who placed the order */
  username?: string;
  /** For admin variant: link/target URL */
  link?: string;
  /** For admin variant: start count */
  startCount?: number;
  /** For admin variant: remaining quantity */
  remains?: number;
}

@Component({
  selector: 'app-user-order-row',
  templateUrl: './user-order-row.html',
  styleUrl: './user-order-row.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe, DatePipe]
})
export class UserOrderRow {
  /** Order data */
  readonly order = input.required<UserOrder>();

  /** Row variant: 'user' or 'admin' */
  readonly variant = input<OrderRowVariant>('user');

  /** Whether the row is clickable */
  readonly clickable = input<boolean>(true);

  /** Click event */
  readonly orderClick = output<UserOrder>();

  /** Whether to show admin columns */
  protected readonly isAdminVariant = computed(() => this.variant() === 'admin');

  protected getStatusLabel(status: OrderStatus): string {
    const labels: Record<OrderStatus, string> = {
      pending: 'Pending',
      processing: 'In Progress',
      completed: 'Completed',
      cancelled: 'Cancelled',
      partial: 'Partial'
    };
    return labels[status];
  }

  protected onClick(): void {
    if (this.clickable()) {
      this.orderClick.emit(this.order());
    }
  }
}
