import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { fromEvent, filter, throttleTime, take } from 'rxjs';
import { NgIcon } from '@ng-icons/core';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { Breadcrumb } from '../../components/shared/breadcrumb/breadcrumb';
import { OrderService, type OrderResponse, type OrderStatus } from '../../core/services/order.service';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../services/notification.service';

/**
 * Order Detail Page Component
 *
 * Displays detailed information about a single order.
 * Accessed via route /orders/:id with data preloaded by orderResolver.
 *
 * Features:
 * - Full order details display
 * - Status badge with color coding
 * - Progress visualization
 * - Back navigation to orders list
 *
 * @example
 * Route: /orders/123 (requires authentication)
 */
@Component({
  selector: 'app-order-detail',
  templateUrl: './order-detail.html',
  styleUrl: './order-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer, Breadcrumb, NgIcon, RouterLink]
})
export class OrderDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly orderService = inject(OrderService);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  // ---------------------------------------------------------------------------
  // State Signals
  // ---------------------------------------------------------------------------

  /** Order data from resolver or direct fetch */
  protected readonly order = signal<OrderResponse | null>(null);

  /** Loading state */
  protected readonly isLoading = signal(true);

  /** Error message */
  protected readonly error = signal<string | null>(null);

  /** User balance for header */
  protected readonly balance = signal('$0.00');

  // ---------------------------------------------------------------------------
  // Computed Properties
  // ---------------------------------------------------------------------------

  /** Formatted order ID with leading zeros */
  protected readonly formattedId = computed(() => {
    const id = this.order()?.id;
    if (!id) return '-----';
    return String(id).padStart(5, '0');
  });

  /** Status label for display */
  protected readonly statusLabel = computed(() => {
    return this.order()?.status ?? 'UNKNOWN';
  });

  /** CSS class for status styling */
  protected readonly statusClass = computed(() => {
    const status = this.order()?.status?.toLowerCase() ?? 'unknown';
    return `order-detail__status--${status}`;
  });

  /** Formatted quantity with thousands separator */
  protected readonly formattedQuantity = computed(() => {
    return this.order()?.quantity?.toLocaleString() ?? '0';
  });

  /** Formatted remains with thousands separator */
  protected readonly formattedRemains = computed(() => {
    return this.order()?.remains?.toLocaleString() ?? '0';
  });

  /** Formatted total charge with currency */
  protected readonly formattedCharge = computed(() => {
    const charge = this.order()?.totalCharge ?? 0;
    return this.formatPrice(charge);
  });

  /** Formatted creation date */
  protected readonly formattedDate = computed(() => {
    const dateStr = this.order()?.createdAt;
    if (!dateStr) return '----/--/-- --:--:--';
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  });

  /** Formatted completion date */
  protected readonly formattedCompletedDate = computed(() => {
    const dateStr = this.order()?.completedAt;
    if (!dateStr) return null;
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  });

  /** Progress percentage for display */
  protected readonly progressPercentage = computed(() => {
    const progress = this.order()?.progress ?? 0;
    return Math.round(progress);
  });

  /** Whether order can be refilled */
  protected readonly canRefill = computed(() => {
    const order = this.order();
    return order?.isRefillable && order?.canRequestRefill;
  });

  /** Refill deadline formatted */
  protected readonly refillDeadline = computed(() => {
    const dateStr = this.order()?.refillDeadline;
    if (!dateStr) return null;
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  });

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  ngOnInit(): void {
    this.loadUserBalance();
    this.loadOrderFromRoute();
    this.setupOrderPolling();
  }

  // ---------------------------------------------------------------------------
  // Data Loading
  // ---------------------------------------------------------------------------

  /**
   * Load order from route data (resolver) or fetch directly
   */
  private loadOrderFromRoute(): void {
    // First try to get order from resolver data
    this.route.data
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ order }) => {
        if (order) {
          this.order.set(order);
          this.isLoading.set(false);
        } else {
          // Fallback: fetch order directly using route param
          this.fetchOrderById();
        }
      });
  }

  /**
   * Fetch order by ID from route params
   */
  private fetchOrderById(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Order ID not provided');
      this.isLoading.set(false);
      return;
    }

    this.orderService.getOrderById(Number(id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (order) => {
          this.order.set(order);
          this.isLoading.set(false);
        },
        error: () => {
          this.error.set(`Order #${id} not found`);
          this.isLoading.set(false);
        }
      });
  }

  /**
   * Load user balance for header display
   */
  private loadUserBalance(): void {
    this.userService.getStatistics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (stats) => {
          this.balance.set(`$${stats.balance.toFixed(2)}`);
        },
        error: () => {
          // Silent fail - balance display is not critical
        }
      });
  }

  // ---------------------------------------------------------------------------
  // Event Handlers
  // ---------------------------------------------------------------------------

  /**
   * Handle logout click from header
   */
  protected onLogoutClick(): void {
    this.authService.logout()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.router.navigate(['/home']);
      });
  }

  /**
   * Handle order again action
   */
  protected onOrderAgain(): void {
    const order = this.order();
    if (order) {
      this.router.navigate(['/dashboard'], {
        queryParams: { service: order.serviceName }
      });
    }
  }

  /**
   * Handle back navigation
   */
  protected onBack(): void {
    this.router.navigate(['/orders']);
  }

  // ---------------------------------------------------------------------------
  // Utility Methods
  // ---------------------------------------------------------------------------

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

  // ---------------------------------------------------------------------------
  // Order Status Polling
  // ---------------------------------------------------------------------------

  /** Final order statuses that don't need polling */
  private readonly finalStatuses: OrderStatus[] = [
    'COMPLETED', 'CANCELLED', 'REFUNDED', 'FAILED'
  ];

  /**
   * Setup polling for order status updates.
   * Polls when user returns to the tab (visibility change).
   */
  private setupOrderPolling(): void {
    fromEvent(document, 'visibilitychange').pipe(
      filter(() => document.visibilityState === 'visible'),
      throttleTime(10000), // 10 seconds minimum between checks
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => {
      this.refreshOrderStatus();
    });
  }

  /**
   * Refresh order status from API.
   * Only refreshes if order is not in a final state.
   */
  private refreshOrderStatus(): void {
    const currentOrder = this.order();
    if (!currentOrder || this.finalStatuses.includes(currentOrder.status)) {
      return; // Don't poll final states
    }

    this.orderService.getOrderById(currentOrder.id).pipe(
      take(1),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: updated => {
        const previousStatus = currentOrder.status;
        if (updated.status !== previousStatus) {
          this.order.set(updated);

          // Notify user of completion
          if (updated.status === 'COMPLETED') {
            this.notificationService.success(
              'Your order has been completed!',
              { title: 'Order Complete' }
            );
          }
        } else {
          // Update progress even if status hasn't changed
          this.order.set(updated);
        }
      },
      error: err => {
        console.warn('Order refresh failed:', err);
      }
    });
  }
}
