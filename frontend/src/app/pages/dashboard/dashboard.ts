import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
  ViewChild
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { DashboardOverview } from './sections/dashboard-overview/dashboard-overview';
import { DashboardOrderSection } from './sections/dashboard-order-section/dashboard-order-section';
import { DashboardRecentOrdersSection } from './sections/dashboard-recent-orders-section/dashboard-recent-orders-section';
import { ServicesSection } from '../home/sections/services-section/services-section';
import { UserService, type UserStatisticsResponse } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { PendingOrderService } from '../../core/services/pending-order.service';
import type { OrderResponse } from '../../core/services/order.service';
import type { ServiceItemData } from '../../components/shared/service-item-card/service-item-card';
import type { RecentOrderData } from '../../components/shared/recent-order-card/recent-order-card';

/**
 * Dashboard page component.
 * Protected page showing user statistics, order creation, and services.
 *
 * Features:
 * - Flat 2D grid background (same as login/register)
 * - Dashboard overview with real API statistics
 * - Order section with balance verification
 * - Services section (reused from home)
 * - Navigation to wallet for adding funds
 *
 * @example
 * Route: /dashboard (requires authentication)
 */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer, DashboardOverview, DashboardOrderSection, DashboardRecentOrdersSection, ServicesSection]
})
export class Dashboard implements OnInit {
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly pendingOrderService = inject(PendingOrderService);
  private readonly destroyRef = inject(DestroyRef);

  /** Reference to services section for programmatic interaction */
  @ViewChild(ServicesSection) servicesSection?: ServicesSection;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------

  /** User statistics from API */
  protected readonly userStats = signal<UserStatisticsResponse | null>(null);

  /** Loading state for statistics */
  protected readonly isLoading = signal(true);

  /** Error message if stats fail to load */
  protected readonly error = signal<string | null>(null);

  /** Quick order service from services section */
  protected readonly quickOrderService = signal<ServiceItemData | null>(null);

  /** Platform to select in services section (from OrderReady "More Platform") */
  protected readonly platformToSelect = signal<string | null>(null);

  /** Whether to reset platform selection (from OrderReady "Explore More") */
  protected readonly shouldResetPlatform = signal(false);

  /** Initial input text from pending order (home page flow) */
  protected readonly pendingOrderText = signal<string | null>(null);

  // -------------------------------------------------------------------------
  // Computed
  // -------------------------------------------------------------------------

  /** User's balance as formatted string */
  protected readonly balance = computed<string>(() => {
    const stats = this.userStats();
    if (!stats) return '$0.00';
    return `$${stats.balance.toFixed(2)}`;
  });

  /** User's balance as number for order validation */
  protected readonly userBalance = computed<number>(() => {
    const stats = this.userStats();
    return stats?.balance ?? 0;
  });

  // -------------------------------------------------------------------------
  // Lifecycle
  // -------------------------------------------------------------------------

  ngOnInit(): void {
    this.loadUserStatistics();
    this.processPendingOrder();
  }

  // -------------------------------------------------------------------------
  // Data Loading
  // -------------------------------------------------------------------------

  /**
   * Load user statistics from API
   */
  protected loadUserStatistics(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.userService.getStatistics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (stats) => {
          this.userStats.set(stats);
          this.isLoading.set(false);
        },
        error: (err) => {
          console.error('Failed to load user statistics:', err);
          this.error.set('Failed to load dashboard data. Please try again.');
          this.isLoading.set(false);
        }
      });
  }

  /**
   * Process pending order from home page flow.
   * Consumes the pending order and pre-fills the order section input.
   */
  private processPendingOrder(): void {
    const pendingOrder = this.pendingOrderService.consume();
    if (pendingOrder) {
      // Reconstruct input text from parsed order data
      const quantity = this.formatQuantityForInput(pendingOrder.quantity);
      const platform = pendingOrder.service.platform.toLowerCase();
      const type = pendingOrder.service.type;
      const target = pendingOrder.target ?? '';

      // Format: "1k instagram Followers @username"
      const inputText = `${quantity} ${platform} ${type} ${target}`.trim();
      this.pendingOrderText.set(inputText);

      // Scroll to order section after a tick
      setTimeout(() => {
        const orderSection = document.querySelector('app-dashboard-order-section');
        orderSection?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 100);
    }
  }

  /**
   * Format quantity for input (1000 -> "1k", 1500 -> "1.5k")
   */
  private formatQuantityForInput(qty: number): string {
    if (qty >= 1000000) {
      const value = qty / 1000000;
      return Number.isInteger(value) ? `${value}m` : `${value.toFixed(1)}m`;
    }
    if (qty >= 1000) {
      const value = qty / 1000;
      return Number.isInteger(value) ? `${value}k` : `${value.toFixed(1)}k`;
    }
    return qty.toString();
  }

  // -------------------------------------------------------------------------
  // Event Handlers
  // -------------------------------------------------------------------------

  /**
   * Handle add funds click from overview section.
   * Navigates to wallet with returnUrl for back navigation.
   */
  protected onAddFunds(): void {
    this.router.navigate(['/wallet'], {
      queryParams: { returnUrl: '/dashboard' }
    });
  }

  /**
   * Handle logout click from header.
   * Subscribes to logout observable to properly notify backend.
   */
  protected onLogoutClick(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/home']),
      error: () => this.router.navigate(['/home'])
    });
  }

  /**
   * Handle quick order from services section
   */
  protected onQuickOrder(service: ServiceItemData): void {
    this.quickOrderService.set(service);
    // Reset the signal after a tick to allow future same-service orders
    setTimeout(() => {
      this.quickOrderService.set(null);
    }, 100);

    // Scroll to order section
    const orderSection = document.querySelector('app-dashboard-order-section');
    orderSection?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  /**
   * Handle order created from order section
   */
  protected onOrderCreated(response: OrderResponse): void {
    // Refresh statistics to update balance and counts
    this.loadUserStatistics();
  }

  /**
   * Handle navigate to wallet from order section.
   * Passes required amount and returnUrl as query params.
   */
  protected onNavigateToWallet(requiredAmount: number): void {
    this.router.navigate(['/wallet'], {
      queryParams: {
        amount: requiredAmount.toFixed(2),
        returnUrl: '/dashboard'
      }
    });
  }

  /**
   * Handle platform selection from order section
   */
  protected onSelectPlatform(platform: string): void {
    this.platformToSelect.set(platform);
    // Reset the signal after a tick
    setTimeout(() => {
      this.platformToSelect.set(null);
    }, 100);

    // Scroll to services section
    const servicesSection = document.querySelector('app-services-section');
    servicesSection?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  /**
   * Handle reset platform from order section
   */
  protected onResetPlatform(): void {
    this.shouldResetPlatform.set(true);
    // Reset the signal after a tick
    setTimeout(() => {
      this.shouldResetPlatform.set(false);
    }, 100);

    // Scroll to services section
    const servicesSection = document.querySelector('app-services-section');
    servicesSection?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  /**
   * Handle order again from recent orders section.
   * Creates a minimal ServiceItemData with just the name, as the quick order
   * flow only uses the name to populate the input field.
   */
  protected onOrderAgain(order: RecentOrderData): void {
    // The quick order flow only uses the name property to populate input text
    const serviceData = { name: order.serviceName } as ServiceItemData;
    this.onQuickOrder(serviceData);
  }
}
