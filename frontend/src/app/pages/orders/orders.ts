import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthService } from '../../core/services/auth.service';

/**
 * Orders page component (placeholder).
 * Will display user's order history.
 *
 * @example
 * Route: /orders (requires authentication)
 */
@Component({
  selector: 'app-orders',
  templateUrl: './orders.html',
  styleUrl: './orders.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer]
})
export class Orders {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  /**
   * Handle wallet click from header
   */
  protected onWalletClick(): void {
    this.router.navigate(['/wallet']);
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
   * Navigate back to dashboard
   */
  protected onBackToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}
