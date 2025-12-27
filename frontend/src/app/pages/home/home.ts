import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { HeroSection } from './sections/hero-section/hero-section';
import { OrderSection } from './sections/order-section/order-section';
import { ServicesSection } from './sections/services-section/services-section';
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

  /** Quick order data from ServicesSection */
  protected readonly quickOrderData = signal<ServiceItemData | null>(null);

  /** Selected platform slug from OrderSection "More Platform" button */
  protected readonly selectedPlatformSlug = signal<string | null>(null);

  /**
   * Handle place order event from OrderSection.
   * Redirects to registration page since user is not authenticated.
   */
  protected onPlaceOrder(data: OrderReadyData): void {
    // Store order data in session storage for after registration
    sessionStorage.setItem('pendingOrder', JSON.stringify(data));

    // Redirect to registration
    this.router.navigate(['/register']);
  }

  /**
   * Handle quick order event from ServicesSection.
   * Sets the quick order data and scrolls to order section.
   */
  protected onQuickOrder(data: ServiceItemData): void {
    // Set quick order data for OrderSection
    this.quickOrderData.set(data);

    // Scroll to order section
    const orderSection = document.querySelector('app-order-section');
    orderSection?.scrollIntoView({ behavior: 'smooth' });
  }

  /**
   * Handle platform selection from OrderSection "More Platform" button.
   */
  protected onSelectPlatform(slug: string): void {
    this.selectedPlatformSlug.set(slug);
  }
}
