import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards';

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
    canActivate: [authGuard]
  },
  {
    path: 'cliente',
    loadComponent: () =>
      import('./pages/cliente/cliente').then(m => m.Cliente)
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
    canActivate: [guestGuard]
  },
  {
    path: 'terms',
    loadComponent: () =>
      import('./pages/terms/terms').then(m => m.Terms)
  },
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found').then(m => m.NotFound)
  }
];
