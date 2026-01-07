import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
  ViewChild
} from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { DashboardOverview } from './sections/dashboard-overview/dashboard-overview';
import { DashboardOrderSection } from './sections/dashboard-order-section/dashboard-order-section';
import { ServicesSection } from '../home/sections/services-section/services-section';
import { UserService, type UserStatisticsResponse } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import type { OrderResponse } from '../../core/services/order.service';
import type { ServiceItemData } from '../../components/shared/service-item-card/service-item-card';

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
  imports: [Header, Footer, DashboardOverview, DashboardOrderSection, ServicesSection]
})
export class Dashboard implements OnInit {
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);

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

    this.userService.getStatistics().subscribe({
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

  // -------------------------------------------------------------------------
  // Event Handlers
  // -------------------------------------------------------------------------

  /**
   * Handle add funds click from overview section
   */
  protected onAddFunds(): void {
    this.router.navigate(['/wallet']);
  }

  /**
   * Handle wallet click from header
   */
  protected onWalletClick(): void {
    this.router.navigate(['/wallet']);
  }

  /**
   * Handle logout click from header
   */
  protected onLogoutClick(): void {
    this.authService.logout();
    this.router.navigate(['/home']);
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
   * Handle navigate to wallet from order section
   */
  protected onNavigateToWallet(): void {
    this.router.navigate(['/wallet']);
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
}
