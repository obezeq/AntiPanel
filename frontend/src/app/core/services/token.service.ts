import { Injectable, signal, computed } from '@angular/core';
import type { UserSummary } from './auth.service';

// ============================================================================
// Constants
// ============================================================================

const ACCESS_TOKEN_KEY = 'antipanel_access_token';
const REFRESH_TOKEN_KEY = 'antipanel_refresh_token';
const USER_KEY = 'antipanel_user';
const TOKEN_EXPIRY_KEY = 'antipanel_token_expiry';

// ============================================================================
// Types
// ============================================================================

export interface StoredTokens {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
}

// ============================================================================
// Service
// ============================================================================

/**
 * Token storage service for managing authentication tokens.
 *
 * Features:
 * - Stores access and refresh tokens in localStorage
 * - Tracks token expiration
 * - Provides signal-based auth state for reactive updates
 * - Handles token refresh window calculation
 *
 * @example
 * ```typescript
 * const tokenService = inject(TokenService);
 *
 * // Store tokens after login
 * tokenService.setTokens(response.accessToken, response.refreshToken, response.expiresIn);
 *
 * // Check auth state
 * if (tokenService.isAuthenticated()) {
 *   // User is logged in
 * }
 *
 * // Get token for API requests
 * const token = tokenService.getAccessToken();
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class TokenService {
  // -------------------------------------------------------------------------
  // Private Signals
  // -------------------------------------------------------------------------

  /** Internal signal for access token */
  private readonly _accessToken = signal<string | null>(this.loadStoredAccessToken());

  /** Internal signal for current user */
  private readonly _currentUser = signal<UserSummary | null>(this.loadStoredUser());

  // -------------------------------------------------------------------------
  // Public Computed Signals
  // -------------------------------------------------------------------------

  /** Whether user is currently authenticated */
  readonly isAuthenticated = computed(() => {
    const token = this._accessToken();
    if (!token) return false;

    // Check if token is expired
    const expiry = this.getTokenExpiry();
    if (expiry && Date.now() >= expiry) {
      return false;
    }

    return true;
  });

  /** Current authenticated user (readonly) */
  readonly currentUser = computed(() => this._currentUser());

  /** Access token for API requests */
  readonly accessToken = computed(() => this._accessToken());

  // -------------------------------------------------------------------------
  // Token Management
  // -------------------------------------------------------------------------

  /**
   * Stores authentication tokens and user info.
   *
   * @param accessToken - JWT access token
   * @param refreshToken - JWT refresh token
   * @param expiresIn - Token expiration time in seconds
   * @param user - User information
   */
  setTokens(
    accessToken: string,
    refreshToken: string,
    expiresIn: number,
    user?: UserSummary
  ): void {
    // Calculate expiry timestamp (current time + expiresIn seconds)
    const expiresAt = Date.now() + (expiresIn * 1000);

    // Store in localStorage
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    localStorage.setItem(TOKEN_EXPIRY_KEY, expiresAt.toString());

    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
      this._currentUser.set(user);
    }

    // Update signal
    this._accessToken.set(accessToken);
  }

  /**
   * Retrieves the current access token.
   *
   * @returns Access token string or null if not authenticated
   */
  getAccessToken(): string | null {
    return this._accessToken();
  }

  /**
   * Retrieves the refresh token.
   *
   * @returns Refresh token string or null if not found
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  /**
   * Retrieves the token expiry timestamp.
   *
   * @returns Expiry timestamp in milliseconds or null
   */
  getTokenExpiry(): number | null {
    const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY);
    return expiry ? parseInt(expiry, 10) : null;
  }

  /**
   * Updates only the access token (used after token refresh).
   *
   * @param accessToken - New access token
   * @param expiresIn - Token expiration time in seconds
   */
  updateAccessToken(accessToken: string, expiresIn: number): void {
    const expiresAt = Date.now() + (expiresIn * 1000);

    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(TOKEN_EXPIRY_KEY, expiresAt.toString());

    this._accessToken.set(accessToken);
  }

  /**
   * Updates the stored user information.
   *
   * @param user - Updated user information
   */
  updateUser(user: UserSummary): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this._currentUser.set(user);
  }

  /**
   * Clears all stored tokens and user data.
   * Used during logout.
   */
  clearTokens(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
    localStorage.removeItem(USER_KEY);

    this._accessToken.set(null);
    this._currentUser.set(null);
  }

  /**
   * Checks if the access token is expired or about to expire.
   *
   * @param bufferSeconds - Time buffer in seconds before actual expiry (default: 60)
   * @returns true if token needs refresh
   */
  isTokenExpired(bufferSeconds = 60): boolean {
    const expiry = this.getTokenExpiry();
    if (!expiry) return true;

    // Add buffer to check if token will expire soon
    const bufferMs = bufferSeconds * 1000;
    return Date.now() >= (expiry - bufferMs);
  }

  /**
   * Checks if refresh is possible (has refresh token).
   *
   * @returns true if refresh token exists
   */
  canRefresh(): boolean {
    return !!this.getRefreshToken();
  }

  // -------------------------------------------------------------------------
  // Private Methods
  // -------------------------------------------------------------------------

  /**
   * Loads stored access token from localStorage.
   */
  private loadStoredAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  /**
   * Loads stored user from localStorage.
   */
  private loadStoredUser(): UserSummary | null {
    const stored = localStorage.getItem(USER_KEY);
    if (!stored) return null;

    try {
      return JSON.parse(stored) as UserSummary;
    } catch {
      return null;
    }
  }
}
