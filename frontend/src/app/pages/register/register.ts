import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthForm, type AuthFormData } from '../../components/shared/auth-form/auth-form';
import { AuthService } from '../../core/services/auth.service';

/**
 * Register page component.
 * Provides user registration with email, password, and confirmation.
 *
 * Features:
 * - Flat 2D grid background with white glow effect
 * - Uses AuthForm component in 'register' mode
 * - Password strength validation
 * - Email uniqueness validation (async)
 * - Handles registration via AuthService
 * - Redirects to login on success
 * - Displays server errors on failure
 *
 * @example
 * Route: /register
 */
@Component({
  selector: 'app-register',
  templateUrl: './register.html',
  styleUrl: './register.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer, AuthForm]
})
export class Register {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  /** Loading state during registration */
  protected readonly isLoading = signal(false);

  /** Server error message to display */
  protected readonly serverError = signal('');

  /**
   * Handle form submission from AuthForm.
   * Registers user and redirects to login on success.
   */
  protected onFormSubmit(data: AuthFormData): void {
    this.isLoading.set(true);
    this.serverError.set('');

    this.authService.register({
      email: data.email,
      password: data.password
    }).subscribe({
      next: () => {
        this.isLoading.set(false);
        // Redirect to login with success message
        this.router.navigate(['/login'], {
          queryParams: { registered: 'true' }
        });
      },
      error: (error) => {
        this.isLoading.set(false);
        // Handle specific email already registered error
        if (this.authService.isEmailAlreadyRegisteredError(error)) {
          this.serverError.set('This email is already registered. Please login instead.');
        } else {
          this.serverError.set(this.authService.getErrorMessage(error));
        }
      }
    });
  }
}
