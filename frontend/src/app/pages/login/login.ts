import { ChangeDetectionStrategy, Component, inject, signal, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthForm, type AuthFormData } from '../../components/shared/auth-form/auth-form';
import { AuthService } from '../../core/services/auth.service';
import { PendingOrderService } from '../../core/services/pending-order.service';

/**
 * Login page component.
 * Provides user authentication with email and password.
 *
 * Features:
 * - Flat 2D grid background with white glow effect
 * - Uses AuthForm component in 'login' mode
 * - Handles authentication via AuthService
 * - Supports returnUrl for post-login redirect
 * - Shows success message after registration
 * - Shows session expired message
 * - Redirects to dashboard on success
 * - Displays server errors on failure
 *
 * @example
 * Route: /login
 * Route: /login?returnUrl=/dashboard
 * Route: /login?registered=true
 * Route: /login?sessionExpired=true
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Header, Footer, AuthForm]
})
export class Login implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly pendingOrderService = inject(PendingOrderService);

  /** Loading state during authentication */
  protected readonly isLoading = signal(false);

  /** Server error message to display */
  protected readonly serverError = signal('');

  /** Success message to display (e.g., after registration) */
  protected readonly successMessage = signal('');

  /** Info message to display (e.g., session expired) */
  protected readonly infoMessage = signal('');

  /** Return URL after successful login */
  private returnUrl = '/dashboard';

  ngOnInit(): void {
    // Get return URL from query params
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    if (returnUrl) {
      this.returnUrl = returnUrl;
    }

    // Check for registration success
    const registered = this.route.snapshot.queryParamMap.get('registered');
    if (registered === 'true') {
      this.successMessage.set('Account created successfully! Please login.');
    }

    // Check for session expired
    const sessionExpired = this.route.snapshot.queryParamMap.get('sessionExpired');
    if (sessionExpired === 'true') {
      this.infoMessage.set('Your session has expired. Please login again.');
    }
  }

  /**
   * Handle form submission from AuthForm.
   * Authenticates user and redirects to returnUrl or dashboard on success.
   */
  protected onFormSubmit(data: AuthFormData): void {
    this.isLoading.set(true);
    this.serverError.set('');
    this.successMessage.set('');
    this.infoMessage.set('');

    this.authService.login({
      email: data.email,
      password: data.password
    }).subscribe({
      next: () => {
        this.isLoading.set(false);

        // Check for pending order and redirect to dashboard
        if (this.pendingOrderService.hasPendingOrder()) {
          this.router.navigate(['/dashboard']);
        } else {
          this.router.navigateByUrl(this.returnUrl);
        }
      },
      error: (error) => {
        this.isLoading.set(false);
        this.serverError.set(this.authService.getErrorMessage(error));
      }
    });
  }
}
