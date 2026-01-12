import { Injectable, inject, effect, DestroyRef } from '@angular/core';
import { TokenService } from './token.service';
import { AuthService } from './auth.service';

/**
 * Proactive token refresh service.
 *
 * Automatically refreshes JWT tokens before they expire using silent refresh.
 * Uses Angular's effect() to react to authentication state changes and
 * schedules token refresh 1 minute before expiration.
 *
 * Features:
 * - Silent background refresh (no user interruption)
 * - Automatic scheduling based on token expiry
 * - Re-schedules after each successful refresh
 * - Cleanup on service destroy
 *
 * @example
 * ```typescript
 * // Register in app.config.ts with APP_INITIALIZER
 * {
 *   provide: APP_INITIALIZER,
 *   useFactory: (service: TokenRefreshService) => () => service,
 *   deps: [TokenRefreshService],
 *   multi: true
 * }
 * ```
 */
@Injectable({ providedIn: 'root' })
export class TokenRefreshService {
  private readonly tokenService = inject(TokenService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  /** Refresh 1 minute before expiration (safe buffer) */
  private readonly REFRESH_BUFFER_MS = 60 * 1000;

  /** Current scheduled timeout ID */
  private refreshTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    // React to auth state changes with Angular's effect()
    effect(() => {
      if (this.tokenService.isAuthenticated()) {
        this.scheduleRefresh();
      } else {
        this.cancelRefresh();
      }
    });

    // Cleanup on destroy
    this.destroyRef.onDestroy(() => this.cancelRefresh());
  }

  /**
   * Schedules token refresh based on current expiry time.
   * Cancels any existing scheduled refresh first.
   */
  private scheduleRefresh(): void {
    this.cancelRefresh();

    const expiry = this.tokenService.getTokenExpiry();
    if (!expiry) return;

    const refreshAt = expiry - this.REFRESH_BUFFER_MS;
    const delayMs = Math.max(0, refreshAt - Date.now());

    // If token expires in less than buffer time, refresh immediately
    if (delayMs === 0) {
      this.performRefresh();
      return;
    }

    this.refreshTimeout = setTimeout(() => this.performRefresh(), delayMs);
  }

  /**
   * Performs the actual token refresh API call.
   * On success, stores new tokens (effect() will re-schedule).
   * On failure, silently fails - interceptor handles 401 on next request.
   */
  private performRefresh(): void {
    const refreshToken = this.tokenService.getRefreshToken();
    if (!refreshToken) return;

    this.authService.refreshToken(refreshToken).subscribe({
      next: (response) => {
        this.tokenService.setTokens(
          response.accessToken,
          response.refreshToken,
          response.expiresIn,
          response.user
        );
        // effect() will automatically re-trigger scheduleRefresh with new expiry
      },
      error: () => {
        // Silent failure - interceptor will handle 401 on next API request
        // This prevents logout interruption if refresh fails temporarily
      }
    });
  }

  /**
   * Cancels any scheduled token refresh.
   */
  private cancelRefresh(): void {
    if (this.refreshTimeout) {
      clearTimeout(this.refreshTimeout);
      this.refreshTimeout = null;
    }
  }
}
