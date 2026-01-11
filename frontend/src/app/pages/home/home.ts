import { ChangeDetectionStrategy, Component, ElementRef, inject, signal, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { HeroSection } from './sections/hero-section/hero-section';
import { OrderSection } from './sections/order-section/order-section';
import { ServicesSection } from './sections/services-section/services-section';
import { PendingOrderService } from '../../core/services/pending-order.service';
import type { OrderReadyData } from '../../components/shared/order-ready/order-ready';
import type { ServiceItemData } from '../../components/shared/service-item-card/service-item-card';

/**
 * Home page component.
 * Landing page for unauthenticated users with:
 * - Hero section (title, tagline, stats)
 * - Order section (natural language input + preview)
 * - Services section (platform cards + service listings)
 *
 * @example
 * Route: /home
 */
@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrl: './home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer, HeroSection, OrderSection, ServicesSection]
})
export class Home {
  private readonly router = inject(Router);
  private readonly pendingOrderService = inject(PendingOrderService);

  /** Reference to OrderSection for scrolling (Angular best practice) */
  @ViewChild('orderSection', { read: ElementRef })
  private orderSectionRef?: ElementRef<HTMLElement>;

  /** Reference to ServicesSection for scrolling */
  @ViewChild('servicesSection', { read: ElementRef })
  private servicesSectionRef?: ElementRef<HTMLElement>;

  /** Quick order data from ServicesSection */
  protected readonly quickOrderData = signal<ServiceItemData | null>(null);

  /** Selected platform slug from OrderSection "More Platform" button */
  protected readonly selectedPlatformSlug = signal<string | null>(null);

  /** Whether to reset platform selection in ServicesSection */
  protected readonly shouldResetPlatform = signal<boolean>(false);

  /**
   * Handle place order event from OrderSection.
   * Stores order in PendingOrderService and redirects to registration.
   */
  protected onPlaceOrder(data: OrderReadyData): void {
    this.pendingOrderService.set(data);
    this.router.navigate(['/register']);
  }

  /**
   * Handle quick order event from ServicesSection.
   * Sets the quick order data and scrolls to order section.
   */
  protected onQuickOrder(data: ServiceItemData): void {
    // Set quick order data for OrderSection
    this.quickOrderData.set(data);

    // Scroll to order section using ViewChild (Angular best practice)
    // block: 'center' shows context around the element for better UX
    setTimeout(() => {
      this.orderSectionRef?.nativeElement.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      });
    }, 0);
  }

  /**
   * Handle platform selection from OrderSection "More Platform" button.
   * Scrolls to services section using ViewChild (Angular best practice).
   */
  protected onSelectPlatform(slug: string): void {
    this.selectedPlatformSlug.set(slug);

    // Scroll to services section - block: 'center' for better UX
    setTimeout(() => {
      this.servicesSectionRef?.nativeElement.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      });
    }, 0);
  }

  /**
   * Handle platform reset from OrderSection "Explore More" button.
   * Triggers ServicesSection to show all platforms and scrolls to it.
   */
  protected onResetPlatform(): void {
    this.shouldResetPlatform.set(true);
    // Reset the signal after a tick so it can be triggered again
    setTimeout(() => this.shouldResetPlatform.set(false), 0);

    // Scroll to services section - block: 'center' for better UX
    setTimeout(() => {
      this.servicesSectionRef?.nativeElement.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      });
    }, 0);
  }
}
