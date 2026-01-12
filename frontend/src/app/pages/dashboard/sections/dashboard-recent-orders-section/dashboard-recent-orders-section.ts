import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  output,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { DashboardSectionHeader } from '../../../../components/shared/dashboard-section-header/dashboard-section-header';
import {
  RecentOrderCard,
  type RecentOrderData,
  type OrderStatus as RecentOrderStatus
} from '../../../../components/shared/recent-order-card/recent-order-card';
import {
  OrderService,
  type OrderResponse,
  type OrderStatus as ApiOrderStatus
} from '../../../../core/services/order.service';

/**
 * Dashboard Recent Orders Section.
 * Displays the user's most recent orders with quick "Order Again" functionality.
 *
 * Features:
 * - Shows up to 3 recent orders (configurable)
 * - Hover reveals "Order Again" button
 * - "View All Orders" link to orders page
 * - Empty state when no orders exist
 * - Loading state during API fetch
 *
 * @example
 * <app-dashboard-recent-orders-section
 *   [maxOrders]="3"
 *   (orderAgain)="onOrderAgain($event)"
 * />
 */
@Component({
  selector: 'app-dashboard-recent-orders-section',
  templateUrl: './dashboard-recent-orders-section.html',
  styleUrl: './dashboard-recent-orders-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DashboardSectionHeader, RecentOrderCard, RouterLink]
})
export class DashboardRecentOrdersSection {
  private readonly orderService = inject(OrderService);
  private readonly destroyRef = inject(DestroyRef);

  // -------------------------------------------------------------------------
  // Inputs
  // -------------------------------------------------------------------------

  /** Maximum number of recent orders to display */
  readonly maxOrders = input<number>(3);

  // -------------------------------------------------------------------------
  // Outputs
  // -------------------------------------------------------------------------

  /** Emits when user clicks "Order Again" on an order */
  readonly orderAgain = output<RecentOrderData>();

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------

  /** Recent orders data */
  protected readonly orders = signal<RecentOrderData[]>([]);

  /** Loading state */
  protected readonly isLoading = signal<boolean>(true);

  /** Error state */
  protected readonly hasError = signal<boolean>(false);

  // -------------------------------------------------------------------------
  // Computed
  // -------------------------------------------------------------------------

  /** Whether there are orders to display */
  protected readonly hasOrders = computed(() => this.orders().length > 0);

  // -------------------------------------------------------------------------
  // Lifecycle
  // -------------------------------------------------------------------------

  constructor() {
    this.loadRecentOrders();
  }

  // -------------------------------------------------------------------------
  // Data Loading
  // -------------------------------------------------------------------------

  /**
   * Load recent orders from API
   */
  private loadRecentOrders(): void {
    this.orderService.getOrders(0, this.maxOrders())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.orders.set(this.mapToRecentOrderData(response.content));
          this.isLoading.set(false);
        },
        error: () => {
          this.hasError.set(true);
          this.isLoading.set(false);
        }
      });
  }

  /**
   * Map API response to RecentOrderData format
   */
  private mapToRecentOrderData(orders: OrderResponse[]): RecentOrderData[] {
    return orders.map(order => ({
      id: String(order.id),
      serviceName: order.serviceName,
      quantity: order.quantity,
      price: this.formatPrice(order.totalCharge),
      status: this.mapStatus(order.status)
    }));
  }

  /**
   * Map API status to component status
   */
  private mapStatus(apiStatus: ApiOrderStatus): RecentOrderStatus {
    const statusMap: Record<ApiOrderStatus, RecentOrderStatus> = {
      PENDING: 'pending',
      PROCESSING: 'processing',
      IN_PROGRESS: 'processing',
      PARTIAL: 'partial',
      COMPLETED: 'completed',
      CANCELLED: 'cancelled',
      REFUNDED: 'cancelled',
      FAILED: 'cancelled'
    };
    return statusMap[apiStatus];
  }

  /**
   * Format price with appropriate precision
   */
  private formatPrice(price: number): string {
    if (price >= 0.01) {
      return `$${price.toFixed(2)}`;
    }
    const formatted = price.toFixed(6).replace(/0+$/, '').replace(/\.$/, '');
    return `$${formatted}`;
  }

  // -------------------------------------------------------------------------
  // Event Handlers
  // -------------------------------------------------------------------------

  /**
   * Handle order again click
   */
  protected onOrderAgain(order: RecentOrderData): void {
    this.orderAgain.emit(order);
  }
}
