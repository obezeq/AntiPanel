import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Cliente Layout Component
 *
 * Shell component that provides a router-outlet for demo child routes.
 * This enables nested routing for the cliente demo section:
 * - /cliente → Main demo page (cliente.ts)
 * - /cliente/http → HTTP demos (formdata, etc.)
 * - /cliente/state → State management demos (polling, etc.)
 *
 * @example Route configuration
 * ```typescript
 * {
 *   path: 'cliente',
 *   component: ClienteLayout,
 *   children: [
 *     { path: '', component: Cliente },
 *     { path: 'http', component: HttpDemos },
 *     { path: 'state', component: StateDemos }
 *   ]
 * }
 * ```
 */
@Component({
  selector: 'app-cliente-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClienteLayout {}
