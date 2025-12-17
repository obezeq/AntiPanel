import { Routes } from '@angular/router';

export const routes: Routes = [
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
    path: '',
    redirectTo: 'style-guide',
    pathMatch: 'full'
  },
  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found').then(m => m.NotFound)
  }
];
