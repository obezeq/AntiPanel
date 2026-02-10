import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, timer } from 'rxjs';
import { retry } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

// ============================================================================
// Types
// ============================================================================

export interface AnalyticResponse {
  // El titulo de las metricas analisis
  title: string;
  // La metrica amount del valor de ese analisis
  amount: number;
}

// ============================================================================
// Service
// ============================================================================

/**
 * Service for global analytics operations.
 * Provides methods for fetching global statistics accessible to all users.
 *
 * @example
 * ```typescript
 * const analysisService = inject(AnalysisService);
 *
 * analysisService.getGlobalAnalytics().subscribe(analytics => {
 *   console.log('Global Analytics:', analytics);
 *   // Expected: [{ title: "Money Spent", amount: 15420.50 }, ...]
 * });
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class AnalysisService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/analytics`;

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
  // Global Analytics
  // -------------------------------------------------------------------------

  /**
   * Fetches global analytics visible to all users.
   * Returns array of metrics: Money Spent, Orders Made, Users Registered.
   * No authentication required - public endpoint.
   * Includes retry logic for temporary failures.
   *
   * @returns Observable with array of global analytics
   */
  getGlobalAnalytics(): Observable<AnalyticResponse[]> {
    return this.http.get<AnalyticResponse[]>(this.baseUrl).pipe(
      retry(this.retryConfig)
    );
  }
}
