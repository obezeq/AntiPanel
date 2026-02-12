import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthService } from '../../core/services/auth.service';
import type { HeaderVariant } from '../../components/layout/header/header';

/**
 * Accessibility showcase page.
 * Displays a responsive image gallery demonstrating accessible multimedia techniques:
 * - Semantic <figure> and <figcaption> elements
 * - Descriptive alt text for all images
 * - Native lazy loading (loading="lazy")
 * - Responsive <picture> with format fallbacks (AVIF, WebP, JPG)
 * - Keyboard-navigable gallery items
 *
 * @example
 * Route: /accesibilidad
 */
@Component({
  selector: 'app-accesibilidad',
  templateUrl: './accesibilidad.html',
  styleUrl: './accesibilidad.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer]
})
export class Accesibilidad {
  private readonly authService = inject(AuthService);

  protected readonly headerVariant = computed<HeaderVariant>(() =>
    this.authService.isAuthenticated() ? 'loggedIn' : 'home'
  );

  protected readonly galleryImages = [
    {
      baseName: 'style-guide-colors',
      alt: 'AntiPanel dark mode color system featuring background, text, high-contrast, and semantic color tokens organized in a visual grid',
      caption: 'Design tokens: color palette with dark mode variables and semantic colors'
    },
    {
      baseName: 'style-guide-buttons-and-alerts',
      alt: 'AntiPanel UI components showcasing primary, secondary, ghost, and danger button variants alongside success, error, warning, and info alert styles',
      caption: 'Component library: button variants and alert notification styles'
    },
    {
      baseName: 'showcase-dashboard',
      alt: 'AntiPanel dashboard interface displaying order statistics, wallet balance, quick order form, and recent order activity cards',
      caption: 'Dashboard: real-time statistics, wallet balance, and quick order interface'
    },
    {
      baseName: 'showcase-login',
      alt: 'AntiPanel login page with accessible form fields, visible focus indicators, and validation feedback on a dark background with grid pattern',
      caption: 'Authentication: accessible login form with validation and focus management'
    },
    {
      baseName: 'showcase-orders',
      alt: 'AntiPanel orders page with filterable order cards, category navigation, sort controls, and pagination for browsing order history',
      caption: 'Orders: filterable order list with category navigation and pagination'
    },
    {
      baseName: 'showcase-wallet',
      alt: 'AntiPanel wallet page showing current balance, add funds section with amount input, and invoice history with transaction details',
      caption: 'Wallet: balance overview, fund management, and invoice history'
    }
  ];
}
