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
import { Router } from '@angular/router';
import { NgIcon } from '@ng-icons/core';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { OrderCard, type OrderCardData, type OrderStatus as CardStatus } from '../../components/shared/order-card/order-card';
import { OrderFilters, type FilterCategory, type SortOrder } from '../../components/shared/order-filters/order-filters';
import { OrderPagination } from '../../components/shared/order-pagination/order-pagination';
import { OrderService, type OrderResponse } from '../../core/services/order.service';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';

/**
 * Orders page component.
 * Displays paginated list of user's orders with filtering and search.
 *
 * Features:
 * - Shopping cart icon + ORDERS title header
 * - Filter by category (ALL, PENDING, PROCESSING, COMPLETED)
 * - Toggle sort order (latest/oldest)
 * - Search orders by service name, ID, or target URL
 * - Paginated orders list with page size selector
 * - Order again and refill actions
 *
 * @example
 * Route: /orders (requires authentication)
 */
@Component({
  selector: 'app-orders',
  templateUrl: './orders.html',
  styleUrl: './orders.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer, NgIcon, OrderCard, OrderFilters, OrderPagination]
})
export class Orders implements OnInit {
  private readonly router = inject(Router);
  private readonly orderService = inject(OrderService);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  // -------------------------------------------------------------------------
  // State Signals
  // -------------------------------------------------------------------------

  /** Raw orders from API */
  protected readonly orders = signal<OrderCardData[]>([]);

  /** Loading state */
  protected readonly isLoading = signal(true);

  /** Error message */
  protected readonly error = signal<string | null>(null);

  /** User balance for header */
  protected readonly balance = signal('$0.00');

  // -------------------------------------------------------------------------
  // Pagination State
  // -------------------------------------------------------------------------

  /** Current page number (1-indexed for display) */
  protected readonly currentPage = signal(1);

  /** Total number of pages */
  protected readonly totalPages = signal(1);

  /** Items per page */
  protected readonly pageSize = signal(10);

  /** Total number of orders */
  protected readonly totalElements = signal(0);

  // -------------------------------------------------------------------------
  // Filter State
  // -------------------------------------------------------------------------

  /** Selected category filter */
  protected readonly selectedCategory = signal<FilterCategory>('ALL');

  /** Sort order (latest first by default) */
  protected readonly sortOrder = signal<SortOrder>('latest');

  /** Search query string */
  protected readonly searchQuery = signal('');

  // -------------------------------------------------------------------------
  // Computed Properties
  // -------------------------------------------------------------------------

  /**
   * Orders filtered by category and search query.
   * Client-side filtering for responsive UX.
   */
  protected readonly filteredOrders = computed(() => {
    let result = this.orders();

    // Filter by category
    const category = this.selectedCategory();
    if (category !== 'ALL') {
      const statusMap: Record<FilterCategory, CardStatus[]> = {
        'ALL': [],
        'PENDING': ['pending'],
        'PROCESSING': ['processing'],
        'COMPLETED': ['completed', 'partial']
      };
      const statuses = statusMap[category];
      result = result.filter(order => statuses.includes(order.status));
    }

    // Filter by search query
    const query = this.searchQuery().toLowerCase().trim();
    if (query) {
      result = result.filter(order =>
        order.serviceName.toLowerCase().includes(query) ||
        order.id.includes(query) ||
        order.targetUrl?.toLowerCase().includes(query)
      );
    }

    // Sort by date
    const sortOrder = this.sortOrder();
    result = [...result].sort((a, b) => {
      const dateA = a.createdAt.getTime();
      const dateB = b.createdAt.getTime();
      return sortOrder === 'latest' ? dateB - dateA : dateA - dateB;
    });

    return result;
  });

  /** Whether orders list is empty after filtering */
  protected readonly isEmpty = computed(() =>
    !this.isLoading() && !this.error() && this.filteredOrders().length === 0
  );

  /** Whether to show orders list */
  protected readonly showOrders = computed(() =>
    !this.isLoading() && !this.error() && this.filteredOrders().length > 0
  );

  // -------------------------------------------------------------------------
  // Lifecycle
  // -------------------------------------------------------------------------

  ngOnInit(): void {
    this.loadBalance();
    this.loadOrders();
  }

  // -------------------------------------------------------------------------
  // Data Loading
  // -------------------------------------------------------------------------

  /**
   * Load user balance for header display
   */
  private loadBalance(): void {
    this.userService.getStatistics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: stats => this.balance.set(`$${stats.balance.toFixed(2)}`),
        error: () => this.balance.set('$0.00')
      });
  }

  /**
   * Load paginated orders from API
   */
  protected loadOrders(): void {
    this.isLoading.set(true);
    this.error.set(null);

    const page = this.currentPage() - 1; // API is 0-indexed
    const size = this.pageSize();

    this.orderService.getOrders(page, size)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: response => {
          this.orders.set(this.mapOrders(response.content));
          this.totalPages.set(response.totalPages || 1);
          this.totalElements.set(response.totalElements);
          this.isLoading.set(false);
        },
        error: err => {
          console.error('Failed to load orders:', err);
          this.error.set('Failed to load orders. Please try again.');
          this.isLoading.set(false);
        }
      });
  }

  /**
   * Map API responses to component data format
   */
  private mapOrders(orders: OrderResponse[]): OrderCardData[] {
    return orders.map(order => ({
      id: order.id.toString(),
      serviceName: order.serviceName,
      targetUrl: order.target || undefined,
      quantity: order.quantity,
      remains: order.remains,
      price: order.totalCharge,
      status: this.mapStatus(order.status),
      createdAt: new Date(order.createdAt)
    }));
  }

  /**
   * Map API status to component status
   */
  private mapStatus(status: string): CardStatus {
    const map: Record<string, CardStatus> = {
      'PENDING': 'pending',
      'PROCESSING': 'processing',
      'IN_PROGRESS': 'processing',
      'COMPLETED': 'completed',
      'CANCELLED': 'cancelled',
      'PARTIAL': 'partial',
      'REFUNDED': 'cancelled',
      'FAILED': 'cancelled'
    };
    return map[status] || 'pending';
  }

  // -------------------------------------------------------------------------
  // Event Handlers - Filter
  // -------------------------------------------------------------------------

  /**
   * Handle category filter change
   */
  protected onCategoryChange(category: FilterCategory): void {
    this.selectedCategory.set(category);
  }

  /**
   * Handle sort order change
   */
  protected onSortChange(order: SortOrder): void {
    this.sortOrder.set(order);
  }

  /**
   * Handle search query change
   */
  protected onSearchChange(query: string): void {
    this.searchQuery.set(query);
  }

  // -------------------------------------------------------------------------
  // Event Handlers - Pagination
  // -------------------------------------------------------------------------

  /**
   * Handle page change
   */
  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadOrders();
    // Scroll to top of list
    document.querySelector('.orders-page__list')?.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    });
  }

  /**
   * Handle page size change
   */
  protected onPageSizeChange(size: number): void {
    this.pageSize.set(size);
    this.currentPage.set(1); // Reset to first page
    this.loadOrders();
  }

  // -------------------------------------------------------------------------
  // Event Handlers - Order Actions
  // -------------------------------------------------------------------------

  /**
   * Handle order again action.
   * Navigates to dashboard with service name for quick ordering.
   */
  protected onOrderAgain(order: OrderCardData): void {
    // Navigate to dashboard with service info in query params
    this.router.navigate(['/dashboard'], {
      queryParams: { service: order.serviceName }
    });
  }

  /**
   * Handle refill action.
   * TODO: Implement refill request API
   */
  protected onRefill(order: OrderCardData): void {
    console.log('Refill requested for order:', order.id);
    // TODO: Call refill API endpoint
  }

  // -------------------------------------------------------------------------
  // Event Handlers - Header
  // -------------------------------------------------------------------------

  /**
   * Handle logout click from header
   */
  protected onLogoutClick(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/home']),
      error: () => this.router.navigate(['/home'])
    });
  }
}
