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
export interface UsersAnalyisisResponse {
  /** Total number of money spent by all the users */
  totalUsersSpent: number;
  /** Total Orders processed by all the users */
  totalUsersOrders: number;
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
 * const analysisService = inject(AnalysisService);
 *
 * analysisService.getStatistics().subscribe(stats => {
 *   console.log('Total Money Spent by all the users:', stats.totalUsersSpent);
 *   console.log('Total Users Orders made by all the users:', stats.totalUsersOrders);
 * });
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class AnalysisService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/analysis`;

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
  getMoneyAnalysis(): Observable<UsersAnalyisisResponse> {
    return this.http.get<UsersAnalyisisResponse>(`${this.baseUrl}/money`).pipe(
      retry(this.retryConfig)
    );
  }

  getOrdersAnalysis(): Observable<UsersAnalyisisResponse> {
    return this.http.get<UsersAnalyisisResponse>(`${this.baseUrl}/orders`).pipe(
      retry(this.retryConfig)
    );
  }
}
