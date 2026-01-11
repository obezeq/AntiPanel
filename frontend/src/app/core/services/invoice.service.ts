import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { PageResponse } from './order.service';

// ============================================================================
// Types
// ============================================================================

/**
 * Invoice status enum matching backend InvoiceStatus
 */
export type InvoiceStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'FAILED'
  | 'EXPIRED'
  | 'CANCELLED';

/**
 * User summary in invoice response
 */
export interface InvoiceUserSummary {
  id: number;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPPORT';
  balance: number;
}

/**
 * Payment processor summary in invoice response
 */
export interface PaymentProcessorSummary {
  id: number;
  name: string;
  code: string;
  minAmount: number;
  maxAmount: number | null;
  feePercentage: number;
  feeFixed: number;
}

/**
 * Full payment processor response for listing
 */
export interface PaymentProcessor {
  id: number;
  name: string;
  code: string;
  website: string;
  minAmount: number;
  maxAmount: number | null;
  feePercentage: number;
  feeFixed: number;
  isActive: boolean;
  sortOrder: number;
}

/**
 * Request to create a new invoice
 */
export interface InvoiceCreateRequest {
  /** Payment processor ID */
  processorId: number;
  /** Amount to deposit */
  amount: number;
  /** Currency code (default: USD) */
  currency?: string;
}

/**
 * Response from invoice creation or retrieval
 */
export interface InvoiceResponse {
  id: number;
  user: InvoiceUserSummary;
  processor: PaymentProcessorSummary;
  processorInvoiceId: string | null;
  amount: number;
  fee: number;
  netAmount: number;
  currency: string;
  status: InvoiceStatus;
  paymentUrl: string | null;
  paidAt: string | null;
  createdAt: string;
  updatedAt: string;
}

// ============================================================================
// Service
// ============================================================================

/**
 * Service for invoice-related operations.
 * Provides methods for creating invoices and managing deposits.
 *
 * @example
 * ```typescript
 * const invoiceService = inject(InvoiceService);
 *
 * // Get available payment processors
 * invoiceService.getPaymentProcessors().subscribe(processors => {
 *   console.log('Available:', processors);
 * });
 *
 * // Create deposit invoice
 * invoiceService.createInvoice({
 *   processorId: 1,
 *   amount: 50.00
 * }).subscribe({
 *   next: invoice => {
 *     if (invoice.paymentUrl) {
 *       window.location.href = invoice.paymentUrl;
 *     }
 *   },
 *   error: err => console.error('Failed to create invoice:', err)
 * });
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/invoices`;
  private readonly publicUrl = `${environment.apiUrl}/public`;

  // -------------------------------------------------------------------------
  // Payment Processors
  // -------------------------------------------------------------------------

  /**
   * Gets all active payment processors available for deposits.
   *
   * @returns Observable with list of active payment processors
   */
  getPaymentProcessors(): Observable<PaymentProcessor[]> {
    return this.http.get<PaymentProcessor[]>(`${this.publicUrl}/payment-processors`);
  }

  // -------------------------------------------------------------------------
  // Invoice Creation
  // -------------------------------------------------------------------------

  /**
   * Creates a new deposit invoice.
   * For Paymento processor, returns invoice with paymentUrl for redirect.
   *
   * @param request - Invoice creation request with processorId and amount
   * @returns Observable with created invoice response
   * @throws HTTP 400 if validation fails
   * @throws HTTP 404 if processor not found
   */
  createInvoice(request: InvoiceCreateRequest): Observable<InvoiceResponse> {
    return this.http.post<InvoiceResponse>(this.baseUrl, request).pipe(
      catchError(this.handleError)
    );
  }

  // -------------------------------------------------------------------------
  // Invoice Retrieval
  // -------------------------------------------------------------------------

  /**
   * Gets paginated list of user's invoices.
   *
   * @param page - Page number (0-based)
   * @param size - Page size (default 20)
   * @returns Observable with paginated invoices
   */
  getInvoices(page = 0, size = 20): Observable<PageResponse<InvoiceResponse>> {
    return this.http.get<PageResponse<InvoiceResponse>>(this.baseUrl, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  /**
   * Gets a specific invoice by ID.
   *
   * @param id - Invoice ID
   * @returns Observable with invoice details
   */
  getInvoiceById(id: number): Observable<InvoiceResponse> {
    return this.http.get<InvoiceResponse>(`${this.baseUrl}/${id}`);
  }

  /**
   * Checks and updates payment status by verifying with Paymento API.
   * Used for polling-based payment confirmation (no webhooks required).
   *
   * @param invoiceId - Invoice ID to check
   * @returns Observable with updated invoice response
   */
  checkPaymentStatus(invoiceId: number): Observable<InvoiceResponse> {
    return this.http.post<InvoiceResponse>(
      `${this.baseUrl}/${invoiceId}/check-status`,
      {}
    ).pipe(catchError(this.handleError));
  }

  /**
   * Gets user's pending invoices.
   *
   * @returns Observable with list of pending invoices
   */
  getPendingInvoices(): Observable<InvoiceResponse[]> {
    return this.http.get<InvoiceResponse[]>(`${this.baseUrl}/pending`);
  }

  // -------------------------------------------------------------------------
  // Utility Methods
  // -------------------------------------------------------------------------

  /**
   * Maps backend InvoiceStatus to display-friendly status.
   *
   * @param status - Backend invoice status
   * @returns Display status string
   */
  mapStatus(status: InvoiceStatus): 'pending' | 'processing' | 'paid' | 'failed' | 'expired' | 'cancelled' {
    switch (status) {
      case 'PENDING':
        return 'pending';
      case 'PROCESSING':
        return 'processing';
      case 'COMPLETED':
        return 'paid';
      case 'FAILED':
        return 'failed';
      case 'EXPIRED':
        return 'expired';
      case 'CANCELLED':
        return 'cancelled';
      default:
        return 'pending';
    }
  }

  /**
   * Calculates fee for a given amount using processor rates.
   *
   * @param amount - Deposit amount
   * @param processor - Payment processor with fee configuration
   * @returns Calculated fee amount
   */
  calculateFee(amount: number, processor: PaymentProcessor | PaymentProcessorSummary): number {
    const percentageFee = (amount * processor.feePercentage) / 100;
    return percentageFee + processor.feeFixed;
  }

  /**
   * Checks if amount is valid for processor limits.
   *
   * @param amount - Deposit amount
   * @param processor - Payment processor with min/max limits
   * @returns true if amount is within processor limits
   */
  isAmountValid(amount: number, processor: PaymentProcessor | PaymentProcessorSummary): boolean {
    if (amount < processor.minAmount) return false;
    if (processor.maxAmount !== null && amount > processor.maxAmount) return false;
    return true;
  }

  // -------------------------------------------------------------------------
  // Error Handling
  // -------------------------------------------------------------------------

  /**
   * Handles HTTP errors for invoice operations.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred while processing your request.';

    if (error.status === 400) {
      errorMessage = error.error?.message || 'Invalid request. Please check your inputs.';
    } else if (error.status === 404) {
      errorMessage = 'Payment processor not found.';
    }

    console.error('Invoice error:', errorMessage, error);
    return throwError(() => error);
  }
}
