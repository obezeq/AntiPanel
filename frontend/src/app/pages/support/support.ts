import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { SupportHeaderSection } from './sections/support-header-section/support-header-section';
import { SupportContactSection } from './sections/support-contact-section/support-contact-section';
import { SupportComingSoonSection } from './sections/support-coming-soon-section/support-coming-soon-section';
import { AuthService } from '../../core/services/auth.service';
import type { HeaderVariant } from '../../components/layout/header/header';

/**
 * Support page component.
 * Displays contact options with:
 * - Header section (title + description with glow effect)
 * - Contact section (Telegram and Email cards)
 * - Coming Soon section (ticket support teaser)
 *
 * Public page - accessible without authentication.
 * Header variant auto-detects based on auth state.
 *
 * @example
 * Route: /support
 */
@Component({
  selector: 'app-support',
  templateUrl: './support.html',
  styleUrl: './support.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer, SupportHeaderSection, SupportContactSection, SupportComingSoonSection]
})
export class Support {
  private readonly authService = inject(AuthService);

  /**
   * Auto-detect header variant based on authentication state.
   * Shows 'home' variant for guests, 'loggedIn' variant for authenticated users.
   */
  protected readonly headerVariant = computed<HeaderVariant>(() =>
    this.authService.isAuthenticated() ? 'loggedIn' : 'home'
  );
}
