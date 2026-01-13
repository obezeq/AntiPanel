import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Orders Layout Component
 *
 * Shell component that provides a router-outlet for child routes.
 * This enables nested routing for the orders section:
 * - /orders → Orders list (orders.ts)
 * - /orders/:id → Order detail (order-detail.ts)
 *
 * @example Route configuration
 * ```typescript
 * {
 *   path: 'orders',
 *   component: OrdersLayout,
 *   children: [
 *     { path: '', component: Orders },
 *     { path: ':id', component: OrderDetail }
 *   ]
 * }
 * ```
 */
@Component({
  selector: 'app-orders-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrdersLayout {}
