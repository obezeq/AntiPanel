import { Routes } from '@angular/router';
import { authGuard, guestGuard, rootGuard, pendingChangesGuard } from './core/guards';
import { orderResolver } from './core/resolvers';

export const routes: Routes = [
  {
    path: 'home',
    loadComponent: () =>
      import('./pages/home/home').then(m => m.Home)
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/dashboard/dashboard').then(m => m.Dashboard),
    canActivate: [authGuard]
  },
  {
    path: 'wallet',
    loadComponent: () =>
      import('./pages/wallet/wallet').then(m => m.Wallet),
    canActivate: [authGuard],
    data: { breadcrumb: 'Wallet' }
  },
  {
    path: 'orders',
    loadComponent: () =>
      import('./pages/orders/orders').then(m => m.Orders),
    canActivate: [authGuard],
    data: { breadcrumb: 'Orders' }
  },
  {
    path: 'orders/:id',
    loadComponent: () =>
      import('./pages/order-detail/order-detail').then(m => m.OrderDetail),
    canActivate: [authGuard],
    resolve: { order: orderResolver },
    data: { breadcrumb: 'Order Details' }
  },
  {
    path: 'cliente',
    loadComponent: () =>
      import('./pages/cliente/cliente').then(m => m.Cliente),
    data: { breadcrumb: 'Cliente Demo' }
  },
  {
    path: 'style-guide',
    loadComponent: () =>
      import('./pages/style-guide/style-guide').then(m => m.StyleGuide)
  },
  {
    path: 'formarray',
    loadComponent: () =>
      import('./pages/formarray-demo/formarray-demo').then(m => m.FormArrayDemo)
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then(m => m.Login),
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/register/register').then(m => m.Register),
    canActivate: [guestGuard],
    canDeactivate: [pendingChangesGuard]
  },
  {
    path: 'terms',
    loadComponent: () =>
      import('./pages/terms/terms').then(m => m.Terms)
  },
  {
    path: 'support',
    loadComponent: () =>
      import('./pages/support/support').then(m => m.Support)
  },
  {
    path: '',
    pathMatch: 'full',
    canActivate: [rootGuard],
    children: []
  },
  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found').then(m => m.NotFound)
  }
];
