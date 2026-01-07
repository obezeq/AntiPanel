import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
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

  // -------------------------------------------------------------------------
  // Statistics
  // -------------------------------------------------------------------------

  /**
   * Fetches statistics for the authenticated user's dashboard.
   * Includes order counts by status and current balance.
   *
   * @returns Observable with user statistics
   */
  getStatistics(): Observable<UserStatisticsResponse> {
    return this.http.get<UserStatisticsResponse>(`${this.baseUrl}/statistics`);
  }
}
