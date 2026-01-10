import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

// ============================================================================
// Types
// ============================================================================

/**
 * Order status enum matching backend OrderStatus
 */
export type OrderStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'PARTIAL'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'REFUNDED'
  | 'FAILED';

/**
 * Request to create a new order
 */
export interface OrderCreateRequest {
  /** ID of the service to order */
  serviceId: number;
  /** Target URL or username */
  target: string;
  /** Quantity to order */
  quantity: number;
  /** Optional idempotency key to prevent duplicate orders */
  idempotencyKey?: string;
}

/**
 * User summary in order response
 */
export interface OrderUserSummary {
  id: number;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPPORT';
}

/**
 * Response from order creation or retrieval
 */
export interface OrderResponse {
  id: number;
  user: OrderUserSummary;
  serviceId: number;
  serviceName: string;
  target: string;
  quantity: number;
  startCount: number | null;
  remains: number;
  status: OrderStatus;
  progress: number;
  totalCharge: number;
  isRefillable: boolean;
  refillDays: number;
  refillDeadline: string | null;
  canRequestRefill: boolean;
  createdAt: string;
  completedAt: string | null;
  updatedAt: string;
}

/**
 * Paginated response wrapper
 */
export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * API error response for insufficient balance
 */
export interface InsufficientBalanceError {
  status: 402;
  error: 'Payment Required';
  message: string;
}

// ============================================================================
// Service
// ============================================================================

/**
 * Service for order-related operations.
 * Provides methods for creating and managing orders.
 *
 * @example
 * ```typescript
 * const orderService = inject(OrderService);
 *
 * orderService.createOrder({
 *   serviceId: 1,
 *   target: 'https://instagram.com/user',
 *   quantity: 1000
 * }).subscribe({
 *   next: order => console.log('Order created:', order.id),
 *   error: err => {
 *     if (orderService.isInsufficientBalanceError(err)) {
 *       console.log('Not enough balance!');
 *     }
 *   }
 * });
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/orders`;

  // -------------------------------------------------------------------------
  // Order Creation
  // -------------------------------------------------------------------------

  /**
   * Creates a new order with automatic idempotency key generation.
   *
   * @param request - Order creation request with serviceId, target, and quantity
   * @returns Observable with created order response
   * @throws HTTP 402 if user has insufficient balance
   * @throws HTTP 400 if validation fails (quantity limits, inactive service, etc.)
   * @throws HTTP 409 if duplicate order (same idempotency key)
   */
  createOrder(request: OrderCreateRequest): Observable<OrderResponse> {
    const requestWithKey: OrderCreateRequest = {
      ...request,
      idempotencyKey: request.idempotencyKey ?? crypto.randomUUID()
    };

    return this.http.post<OrderResponse>(this.baseUrl, requestWithKey).pipe(
      catchError(this.handleError)
    );
  }

  // -------------------------------------------------------------------------
  // Order Retrieval
  // -------------------------------------------------------------------------

  /**
   * Gets paginated list of user's orders.
   *
   * @param page - Page number (0-based)
   * @param size - Page size (default 20)
   * @returns Observable with paginated orders
   */
  getOrders(page = 0, size = 20): Observable<PageResponse<OrderResponse>> {
    return this.http.get<PageResponse<OrderResponse>>(this.baseUrl, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  /**
   * Gets a specific order by ID.
   *
   * @param id - Order ID
   * @returns Observable with order details
   */
  getOrderById(id: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.baseUrl}/${id}`);
  }

  /**
   * Gets user's active orders (not in final state).
   *
   * @returns Observable with list of active orders
   */
  getActiveOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.baseUrl}/active`);
  }

  /**
   * Gets user's refillable orders.
   *
   * @returns Observable with list of refillable orders
   */
  getRefillableOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.baseUrl}/refillable`);
  }

  // -------------------------------------------------------------------------
  // Error Handling
  // -------------------------------------------------------------------------

  /**
   * Checks if error is an insufficient balance error (HTTP 402).
   *
   * @param error - Error to check
   * @returns true if error is due to insufficient balance
   */
  isInsufficientBalanceError(error: unknown): error is HttpErrorResponse {
    return error instanceof HttpErrorResponse && error.status === 402;
  }

  /**
   * Checks if error is a duplicate order error (HTTP 409).
   *
   * @param error - Error to check
   * @returns true if error is due to duplicate order
   */
  isDuplicateOrderError(error: unknown): error is HttpErrorResponse {
    return error instanceof HttpErrorResponse && error.status === 409;
  }

  /**
   * Handles HTTP errors for order operations.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred while processing your order.';

    if (error.status === 402) {
      errorMessage = error.error?.message || 'Insufficient balance. Please add funds to continue.';
    } else if (error.status === 400) {
      errorMessage = error.error?.message || 'Invalid order request. Please check your inputs.';
    } else if (error.status === 404) {
      errorMessage = 'Service not found.';
    } else if (error.status === 409) {
      errorMessage = error.error?.message || 'Duplicate order detected. Please wait before trying again.';
    }

    console.error('Order error:', errorMessage, error);
    return throwError(() => error);
  }
}
