import { ChangeDetectionStrategy, Component, inject, signal, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthForm, type AuthFormData } from '../../components/shared/auth-form/auth-form';
import { AuthService } from '../../core/services/auth.service';
import { type HasUnsavedChanges } from '../../core/guards';

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
export class Register implements HasUnsavedChanges {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  /** Reference to AuthForm for checking unsaved changes */
  private readonly authForm = viewChild(AuthForm);

  /** Loading state during registration */
  protected readonly isLoading = signal(false);

  /** Server error message to display */
  protected readonly serverError = signal('');

  /**
   * Check if form has unsaved changes.
   * Used by pendingChangesGuard to prompt before navigation.
   */
  hasUnsavedChanges(): boolean {
    const authForm = this.authForm();
    // Don't prompt if currently submitting (loading state)
    if (this.isLoading()) return false;
    // Check if form exists and has been modified
    return authForm?.isDirty ?? false;
  }

  /**
   * Handle form submission from AuthForm.
   * Registers user and redirects to login on success.
   */
  protected onFormSubmit(data: AuthFormData): void {
    this.isLoading.set(true);
    this.serverError.set('');

    this.authService.register({
      email: data.email,
      password: data.password,
      role: 'USER'
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
