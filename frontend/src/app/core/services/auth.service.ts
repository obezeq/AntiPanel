import { Injectable, inject, computed } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, timer } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';

// ============================================================================
// Types
// ============================================================================

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface UserSummary {
  id: number;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPPORT';
  balance: number;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

export interface UserResponse {
  id: number;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPPORT';
  department: string | null;
  balance: number;
  isBanned: boolean;
  bannedReason: string | null;
  lastLoginAt: string | null;
  loginCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
  rejectedValue: unknown;
}

// ============================================================================
// Service
// ============================================================================

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenService = inject(TokenService);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  // -------------------------------------------------------------------------
  // Auth State (delegated to TokenService)
  // -------------------------------------------------------------------------

  /** Whether user is currently authenticated */
  readonly isAuthenticated = computed(() => this.tokenService.isAuthenticated());

  /** Current authenticated user */
  readonly currentUser = computed(() => this.tokenService.currentUser());

  // -------------------------------------------------------------------------
  // Auth Methods
  // -------------------------------------------------------------------------

  /**
   * Authenticates user with email and password.
   * Stores tokens on success.
   *
   * @param credentials - Login credentials (email and password)
   * @returns Observable with AuthResponse containing tokens and user info
   * @throws ApiError on authentication failure
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, credentials).pipe(
      tap((response) => {
        // Store tokens and user info on successful login
        this.tokenService.setTokens(
          response.accessToken,
          response.refreshToken,
          response.expiresIn,
          response.user
        );
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Registers a new user account.
   *
   * @param data - Registration data (email and password)
   * @returns Observable with UserResponse for the newly created user
   * @throws ApiError with status 409 if email already exists
   */
  register(data: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.baseUrl}/register`, data).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Refreshes the access token using a refresh token.
   *
   * @param refreshToken - The refresh token to use
   * @returns Observable with new AuthResponse containing fresh tokens
   */
  refreshToken(refreshToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/refresh`, { refreshToken }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Logs out the current user.
   * Clears tokens and optionally notifies the backend.
   *
   * @returns Observable that completes on success
   */
  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {}).pipe(
      tap(() => {
        // Clear tokens on successful logout
        this.tokenService.clearTokens();
      }),
      catchError((error) => {
        // Clear tokens even if backend logout fails
        this.tokenService.clearTokens();
        return this.handleError(error);
      })
    );
  }

  /**
   * Logs out locally without calling the backend.
   * Useful for session expiry or offline logout.
   */
  logoutLocal(): void {
    this.tokenService.clearTokens();
  }

  /**
   * Simulates checking if an email is already registered.
   * This is used for async validation during registration.
   *
   * Note: The actual backend doesn't have a dedicated endpoint for this.
   * In production, this would call a real endpoint like GET /auth/check-email.
   *
   * For demonstration purposes, this simulates the check with a delay
   * and a list of "taken" emails.
   *
   * @param email - The email to check
   * @returns Observable<boolean> - true if email exists, false otherwise
   */
  checkEmailExists(email: string): Observable<boolean> {
    // Simulated list of taken emails for demonstration
    const takenEmails = [
      'admin@antipanel.com',
      'test@test.com',
      'user@example.com',
      'demo@demo.com'
    ];

    // Simulate API call with 500ms delay
    return timer(500).pipe(
      map(() => {
        const emailLower = email.toLowerCase().trim();
        return takenEmails.includes(emailLower);
      })
    );
  }

  /**
   * Checks if an API error indicates that the email is already registered.
   *
   * @param error - The HTTP error response
   * @returns true if the error is a 409 Conflict for email already registered
   */
  isEmailAlreadyRegisteredError(error: HttpErrorResponse): boolean {
    return error.status === 409 &&
           error.error?.message?.toLowerCase().includes('email already registered');
  }

  /**
   * Extracts a user-friendly error message from an API error response.
   *
   * @param error - The HTTP error response
   * @returns A user-friendly error message string
   */
  getErrorMessage(error: HttpErrorResponse): string {
    if (error.error?.message) {
      return error.error.message;
    }

    switch (error.status) {
      case 400:
        return 'Invalid request. Please check your input.';
      case 401:
        return 'Invalid email or password.';
      case 409:
        return 'Email already registered.';
      case 500:
        return 'Server error. Please try again later.';
      default:
        return 'An unexpected error occurred.';
    }
  }

  /**
   * Error handler for HTTP requests.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    return throwError(() => error);
  }
}
