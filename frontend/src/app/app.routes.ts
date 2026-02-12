import { Routes } from '@angular/router';
import { authGuard, guestGuard, rootGuard, pendingChangesGuard } from './core/guards';
import { orderResolver } from './core/resolvers';

export const routes: Routes = [
  {
    path: 'home',
    title: 'AntiPanel — Home',
    loadComponent: () =>
      import('./pages/home/home').then(m => m.Home)
  },
  {
    path: 'dashboard',
    title: 'AntiPanel — Dashboard',
    loadComponent: () =>
      import('./pages/dashboard/dashboard').then(m => m.Dashboard),
    canActivate: [authGuard]
  },
  {
    path: 'wallet',
    title: 'AntiPanel — Wallet',
    loadComponent: () =>
      import('./pages/wallet/wallet').then(m => m.Wallet),
    canActivate: [authGuard],
    data: { breadcrumb: 'Wallet' }
  },
  {
    path: 'orders',
    title: 'AntiPanel — Orders',
    loadComponent: () =>
      import('./pages/orders/orders-layout').then(m => m.OrdersLayout),
    canActivate: [authGuard],
    data: { breadcrumb: 'Orders' },
    children: [
      {
        path: '',
        title: 'AntiPanel — Orders',
        loadComponent: () =>
          import('./pages/orders/orders').then(m => m.Orders)
      },
      {
        path: ':id',
        title: 'AntiPanel — Order Details',
        loadComponent: () =>
          import('./pages/order-detail/order-detail').then(m => m.OrderDetail),
        resolve: { order: orderResolver },
        data: { breadcrumb: 'Order Details' }
      }
    ]
  },
  {
    path: 'cliente',
    title: 'AntiPanel — Client Demo',
    loadComponent: () =>
      import('./pages/cliente/cliente-layout').then(m => m.ClienteLayout),
    data: { breadcrumb: 'Cliente Demo' },
    children: [
      {
        path: '',
        title: 'AntiPanel — Client Demo',
        loadComponent: () =>
          import('./pages/cliente/cliente').then(m => m.Cliente)
      },
      {
        path: 'http',
        title: 'AntiPanel — HTTP Demos',
        loadComponent: () =>
          import('./pages/cliente/sections/http-section/http-demos').then(m => m.HttpDemos),
        data: { breadcrumb: 'HTTP Demos' }
      },
      {
        path: 'state',
        title: 'AntiPanel — State Demos',
        loadComponent: () =>
          import('./pages/cliente/sections/state-section/state-demos').then(m => m.StateDemos),
        data: { breadcrumb: 'State Demos' }
      }
    ]
  },
  {
    path: 'style-guide',
    title: 'AntiPanel — Style Guide',
    loadComponent: () =>
      import('./pages/style-guide/style-guide').then(m => m.StyleGuide)
  },
  {
    path: 'formarray',
    title: 'AntiPanel — Form Array Demo',
    loadComponent: () =>
      import('./pages/formarray-demo/formarray-demo').then(m => m.FormArrayDemo)
  },
  {
    path: 'login',
    title: 'AntiPanel — Log In',
    loadComponent: () =>
      import('./pages/login/login').then(m => m.Login),
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    title: 'AntiPanel — Register',
    loadComponent: () =>
      import('./pages/register/register').then(m => m.Register),
    canActivate: [guestGuard],
    canDeactivate: [pendingChangesGuard]
  },
  {
    path: 'terms',
    title: 'AntiPanel — Terms of Service',
    loadComponent: () =>
      import('./pages/terms/terms').then(m => m.Terms)
  },
  {
    path: 'support',
    title: 'AntiPanel — Support',
    loadComponent: () =>
      import('./pages/support/support').then(m => m.Support)
  },
  {
    path: 'accesibilidad',
    title: 'AntiPanel — Showcase',
    loadComponent: () =>
      import('./pages/accesibilidad/accesibilidad').then(m => m.Accesibilidad)
  },
  {
    path: '',
    pathMatch: 'full',
    canActivate: [rootGuard],
    children: []
  },
  {
    path: '**',
    title: 'AntiPanel — Page Not Found',
    loadComponent: () =>
      import('./pages/not-found/not-found').then(m => m.NotFound)
  }
];
