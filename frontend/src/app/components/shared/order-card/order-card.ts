import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

/**
 * Order status types with associated styling
 */
export type OrderStatus = 'pending' | 'processing' | 'completed' | 'cancelled' | 'partial';

/**
 * Full order data interface for Orders page display
 */
export interface OrderCardData {
  /** Unique order identifier */
  id: string;
  /** Service name and description */
  serviceName: string;
  /** Target URL for the service */
  targetUrl?: string;
  /** Order quantity */
  quantity: number;
  /** Remaining quantity */
  remains: number;
  /** Order price (as number for formatting) */
  price: number;
  /** Current order status */
  status: OrderStatus;
  /** Order creation date */
  createdAt: Date;
}

/**
 * Order card component for Orders page (user view)
 * Displays complete order information with status, details, and action buttons.
 * Based on Figma design: "Order" component
 *
 * @example
 * <app-order-card
 *   [order]="order"
 *   [showActions]="true"
 *   (orderAgain)="handleOrderAgain($event)"
 *   (refill)="handleRefill($event)"
 * />
 */
@Component({
  selector: 'app-order-card',
  templateUrl: './order-card.html',
  styleUrl: './order-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderCard {
  /** Order data to display */
  readonly order = input.required<OrderCardData>();

  /** Whether to show action buttons (ORDER AGAIN, REFILL) */
  readonly showActions = input<boolean>(true);

  /** Emitted when "Order Again" button is clicked */
  readonly orderAgain = output<OrderCardData>();

  /** Emitted when "Refill" button is clicked */
  readonly refill = output<OrderCardData>();

  /** Emitted when the card is clicked */
  readonly cardClick = output<OrderCardData>();

  // ---------------------------------------------------------------------------
  // COMPUTED PROPERTIES
  // ---------------------------------------------------------------------------

  /** Formatted order ID with leading zeros */
  protected readonly formattedId = computed(() => {
    const id = this.order().id;
    // If numeric, pad with zeros
    if (/^\d+$/.test(id)) {
      return id.padStart(5, '0');
    }
    return id;
  });

  /** Status label for display */
  protected readonly statusLabel = computed(() => {
    return this.order().status.toUpperCase();
  });

  /** CSS class for status styling */
  protected readonly statusClass = computed(() => {
    return `order-card__status--${this.order().status}`;
  });

  /** Formatted quantity with thousands separator */
  protected readonly formattedQuantity = computed(() => {
    return this.order().quantity.toLocaleString();
  });

  /** Formatted remains with thousands separator */
  protected readonly formattedRemains = computed(() => {
    return this.order().remains.toLocaleString();
  });

  /** Formatted price with currency */
  protected readonly formattedPrice = computed(() => {
    return this.formatPrice(this.order().price);
  });

  /**
   * Format price with appropriate precision
   * Shows 2 decimals for prices >= $0.01, up to 6 decimals for smaller amounts
   */
  private formatPrice(price: number): string {
    if (price >= 0.01) {
      return `$${price.toFixed(2)}`;
    }
    const formatted = price.toFixed(6).replace(/0+$/, '').replace(/\.$/, '');
    return `$${formatted}`;
  }

  /** Formatted date string */
  protected readonly formattedDate = computed(() => {
    const date = this.order().createdAt;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  });

  /** Whether refill action should be available */
  protected readonly canRefill = computed(() => {
    const status = this.order().status;
    return status === 'completed' || status === 'partial';
  });

  // ---------------------------------------------------------------------------
  // EVENT HANDLERS
  // ---------------------------------------------------------------------------

  /**
   * Handle card click
   */
  protected onClick(): void {
    this.cardClick.emit(this.order());
  }

  /**
   * Handle "Order Again" button click
   */
  protected onOrderAgain(event: Event): void {
    event.stopPropagation();
    this.orderAgain.emit(this.order());
  }

  /**
   * Handle "Refill" button click
   */
  protected onRefill(event: Event): void {
    event.stopPropagation();
    this.refill.emit(this.order());
  }
}
