import { Injectable, signal, computed } from '@angular/core';
import type { OrderReadyData } from '../../components/shared/order-ready/order-ready';

const STORAGE_KEY = 'pendingOrder';
const EXPIRY_MS = 30 * 60 * 1000; // 30 minutes

interface StoredPendingOrder {
  data: OrderReadyData;
  timestamp: number;
}

/**
 * Service for managing pending orders from unauthenticated users.
 *
 * When an unauthenticated user tries to place an order from the home page,
 * the order data is stored here. After login/registration, the dashboard
 * consumes this data to pre-fill the order form.
 *
 * Features:
 * - Reactive state with Angular signals
 * - SessionStorage persistence for page refresh survival
 * - 30-minute expiry to prevent stale orders
 *
 * @example
 * // Store pending order (home page)
 * pendingOrderService.set(orderData);
 *
 * // Check for pending order (login page)
 * if (pendingOrderService.hasPendingOrder()) {
 *   router.navigate(['/dashboard']);
 * }
 *
 * // Consume pending order (dashboard)
 * const order = pendingOrderService.consume();
 */
@Injectable({ providedIn: 'root' })
export class PendingOrderService {
  private readonly _pendingOrder = signal<OrderReadyData | null>(null);

  /** Reactive pending order data */
  readonly pendingOrder = this._pendingOrder.asReadonly();

  /** Whether there's a valid pending order */
  readonly hasPendingOrder = computed(() => this._pendingOrder() !== null);

  constructor() {
    this.loadFromStorage();
  }

  /** Store a pending order */
  set(data: OrderReadyData): void {
    const stored: StoredPendingOrder = {
      data,
      timestamp: Date.now()
    };
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
    this._pendingOrder.set(data);
  }

  /** Consume and clear the pending order */
  consume(): OrderReadyData | null {
    const data = this._pendingOrder();
    this.clear();
    return data;
  }

  /** Clear the pending order */
  clear(): void {
    sessionStorage.removeItem(STORAGE_KEY);
    this._pendingOrder.set(null);
  }

  /** Load from sessionStorage (handles expiry) */
  private loadFromStorage(): void {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) return;

      const stored: StoredPendingOrder = JSON.parse(raw);
      const isExpired = Date.now() - stored.timestamp > EXPIRY_MS;

      if (isExpired) {
        this.clear();
      } else {
        this._pendingOrder.set(stored.data);
      }
    } catch {
      this.clear();
    }
  }
}
