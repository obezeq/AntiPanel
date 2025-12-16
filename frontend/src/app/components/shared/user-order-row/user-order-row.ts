import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DecimalPipe } from '@angular/common';

export type OrderStatus = 'pending' | 'processing' | 'completed' | 'cancelled' | 'partial';

export interface UserOrder {
  id: string;
  serviceName: string;
  quantity: number;
  price: string;
  status: OrderStatus;
  createdAt: Date;
}

@Component({
  selector: 'app-user-order-row',
  templateUrl: './user-order-row.html',
  styleUrl: './user-order-row.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe]
})
export class UserOrderRow {
  /** Order data */
  readonly order = input.required<UserOrder>();

  /** Whether the row is clickable */
  readonly clickable = input<boolean>(true);

  /** Click event */
  readonly orderClick = output<UserOrder>();

  protected getStatusLabel(status: OrderStatus): string {
    const labels: Record<OrderStatus, string> = {
      pending: 'Pending',
      processing: 'Processing',
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
