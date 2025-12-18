import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

/**
 * Order status types with associated colors
 */
export type OrderStatus = 'pending' | 'processing' | 'completed' | 'cancelled' | 'partial';

/**
 * Recent order data interface for dashboard display
 */
export interface RecentOrderData {
  /** Unique order identifier */
  id: string;
  /** Service name */
  serviceName: string;
  /** Order quantity */
  quantity: number;
  /** Order price (formatted with currency) */
  price: string;
  /** Current order status */
  status: OrderStatus;
}

/**
 * Recent order card component for dashboard
 * Displays a simplified order card with service name, order info, price, and status.
 * Based on Figma design: "Recent Order" component
 *
 * @example
 * <app-recent-order-card
 *   [order]="order"
 *   (orderClick)="handleClick($event)"
 * />
 */
@Component({
  selector: 'app-recent-order-card',
  templateUrl: './recent-order-card.html',
  styleUrl: './recent-order-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecentOrderCard {
  /** Order data to display */
  readonly order = input.required<RecentOrderData>();

  /** Whether the card is interactive/clickable */
  readonly interactive = input<boolean>(true);

  /** Emitted when the card is clicked */
  readonly orderClick = output<RecentOrderData>();

  /** Emitted when "Order Again" button is clicked */
  readonly orderAgain = output<RecentOrderData>();

  /** Computed status label for display */
  protected readonly statusLabel = computed(() => {
    const status = this.order().status;
    return status.toUpperCase();
  });

  /** Computed CSS class for status color */
  protected readonly statusClass = computed(() => {
    return `recent-order-card__status--${this.order().status}`;
  });

  /** Format quantity with thousands separator */
  protected readonly formattedQuantity = computed(() => {
    return this.order().quantity.toLocaleString();
  });

  /**
   * Handle card click
   */
  protected onClick(): void {
    if (this.interactive()) {
      this.orderClick.emit(this.order());
    }
  }

  /**
   * Handle keyboard interaction
   */
  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.onClick();
    }
  }

  /**
   * Handle order again button click
   */
  protected onOrderAgainClick(event: MouseEvent): void {
    event.stopPropagation(); // Prevent card click
    this.orderAgain.emit(this.order());
  }
}
