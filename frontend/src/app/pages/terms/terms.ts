import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { TermsHeaderSection } from './sections/terms-header-section/terms-header-section';
import { TermsContentSection } from './sections/terms-content-section/terms-content-section';
import { AuthService } from '../../core/services/auth.service';
import type { HeaderVariant } from '../../components/layout/header/header';

/**
 * Terms page component.
 * Displays terms of service with:
 * - Header section (title + description with glow effect)
 * - Content section (all terms in bordered container)
 *
 * Public page - accessible without authentication.
 * Header variant auto-detects based on auth state.
 *
 * @example
 * Route: /terms
 */
@Component({
  selector: 'app-terms',
  templateUrl: './terms.html',
  styleUrl: './terms.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer, TermsHeaderSection, TermsContentSection]
})
export class Terms {
  private readonly authService = inject(AuthService);

  /**
   * Auto-detect header variant based on authentication state.
   * Shows 'home' variant for guests, 'loggedIn' variant for authenticated users.
   */
  protected readonly headerVariant = computed<HeaderVariant>(() =>
    this.authService.isAuthenticated() ? 'loggedIn' : 'home'
  );
}
