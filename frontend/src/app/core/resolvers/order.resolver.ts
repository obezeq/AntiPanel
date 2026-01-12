import { inject } from '@angular/core';
import { ResolveFn, Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { OrderService, type OrderResponse } from '../services/order.service';

/**
 * Order Resolver
 *
 * Preloads order data before activating the /orders/:id route.
 * Handles errors by redirecting to orders list with error state.
 *
 * @example
 * // In app.routes.ts
 * {
 *   path: 'orders/:id',
 *   loadComponent: () => import('./pages/order-detail/order-detail'),
 *   resolve: { order: orderResolver }
 * }
 *
 * @example
 * // In component
 * route.data.subscribe(({ order }) => {
 *   if (order) this.order.set(order);
 * });
 */
export const orderResolver: ResolveFn<OrderResponse | null> = (route) => {
  const orderService = inject(OrderService);
  const router = inject(Router);
  const id = route.paramMap.get('id');

  if (!id) {
    router.navigate(['/orders']);
    return of(null);
  }

  return orderService.getOrderById(Number(id)).pipe(
    catchError(() => {
      router.navigate(['/orders'], {
        state: { error: `Order #${id} not found` }
      });
      return of(null);
    })
  );
};
