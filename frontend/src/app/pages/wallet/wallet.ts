import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthService } from '../../core/services/auth.service';

/**
 * Wallet page component (placeholder).
 * Will handle adding funds to user balance.
 *
 * @example
 * Route: /wallet (requires authentication)
 */
@Component({
  selector: 'app-wallet',
  templateUrl: './wallet.html',
  styleUrl: './wallet.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer]
})
export class Wallet {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  /**
   * Handle wallet click from header
   */
  protected onWalletClick(): void {
    // Already on wallet page
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
