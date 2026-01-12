import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, timer } from 'rxjs';
import { retry } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

// ============================================================================
// Types
// ============================================================================

/**
 * Response from the user statistics endpoint.
 * Contains order counts and balance for the dashboard.
 */
export interface UserStatisticsResponse {
  /** Total number of orders placed by the user (all time) */
  totalOrders: number;
  /** Orders currently in PENDING or PROCESSING status */
  pendingOrders: number;
  /** Orders in COMPLETED status */
  completedOrders: number;
  /** Orders created in the current calendar month */
  ordersThisMonth: number;
  /** Current account balance */
  balance: number;
}

// ============================================================================
// Service
// ============================================================================

/**
 * Service for user-related operations.
 * Provides methods for fetching user statistics and profile data.
 *
 * @example
 * ```typescript
 * const userService = inject(UserService);
 *
 * userService.getStatistics().subscribe(stats => {
 *   console.log('Total orders:', stats.totalOrders);
 *   console.log('Balance:', stats.balance);
 * });
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/users/me`;

  /** Retry configuration for GET requests */
  private readonly retryConfig = {
    count: 2,
    delay: (error: HttpErrorResponse, retryCount: number) => {
      // Only retry on 5xx errors (server errors) or network errors
      if (error.status >= 500 || error.status === 0) {
        return timer(1000 * retryCount); // Exponential backoff: 1s, 2s
      }
      // Don't retry on client errors (4xx)
      throw error;
    }
  };

  // -------------------------------------------------------------------------
  // Statistics
  // -------------------------------------------------------------------------

  /**
   * Fetches statistics for the authenticated user's dashboard.
   * Includes order counts by status and current balance.
   * Includes retry logic for temporary failures.
   *
   * @returns Observable with user statistics
   */
  getStatistics(): Observable<UserStatisticsResponse> {
    return this.http.get<UserStatisticsResponse>(`${this.baseUrl}/statistics`).pipe(
      retry(this.retryConfig)
    );
  }
}
