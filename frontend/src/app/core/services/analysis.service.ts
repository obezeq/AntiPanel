import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, timer } from 'rxjs';
import { retry } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

// ============================================================================
// Types
// ============================================================================

export interface Analysis {
    title: string,
    amount: number
}

/**
 * Response from the user statistics endpoint.
 * Contains order counts and balance for the dashboard.
 */
export interface AnalyisisResponse {
  /** Total Array list of Analysis from all the users */
  totalAnalysis: Array<Analysis>;
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
 * analysisService.getAnalysis().subscribe(stats => {
 *   console.log('Array Analysis List:', stats.totalAnalysis);
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
  // Analysis
  // -------------------------------------------------------------------------

  /**
   * Fetches analysis from all the users
   * Includes an array with all the analysis
   * Includes retry logic for temporary failures.
   *
   * @returns Observable with user statistics
   */
  getAnalysis(): Observable<AnalyisisResponse> {
    return this.http.get<AnalyisisResponse>(`${this.baseUrl}/analysis`).pipe(
      retry(this.retryConfig)
    );
  }
}
