import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthForm, type AuthFormData } from '../../components/shared/auth-form/auth-form';
import { AuthService } from '../../core/services/auth.service';

/**
 * Login page component.
 * Provides user authentication with email and password.
 *
 * Features:
 * - Flat 2D grid background with white glow effect
 * - Uses AuthForm component in 'login' mode
 * - Handles authentication via AuthService
 * - Redirects to dashboard on success
 * - Displays server errors on failure
 *
 * @example
 * Route: /login
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer, AuthForm]
})
export class Login {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  /** Loading state during authentication */
  protected readonly isLoading = signal(false);

  /** Server error message to display */
  protected readonly serverError = signal('');

  /**
   * Handle form submission from AuthForm.
   * Authenticates user and redirects to dashboard on success.
   */
  protected onFormSubmit(data: AuthFormData): void {
    this.isLoading.set(true);
    this.serverError.set('');

    this.authService.login({
      email: data.email,
      password: data.password
    }).subscribe({
      next: () => {
        this.isLoading.set(false);
        // Check for pending order from home page
        const pendingOrder = sessionStorage.getItem('pendingOrder');
        if (pendingOrder) {
          sessionStorage.removeItem('pendingOrder');
          this.router.navigate(['/new-order'], {
            state: { orderData: JSON.parse(pendingOrder) }
          });
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (error) => {
        this.isLoading.set(false);
        this.serverError.set(this.authService.getErrorMessage(error));
      }
    });
  }
}
