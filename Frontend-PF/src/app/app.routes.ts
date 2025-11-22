import { Routes } from '@angular/router';
import { AuthComponent } from './pages/auth/auth';
<<<<<<< HEAD

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: '', redirectTo: 'auth', pathMatch: 'full' },
  { path: 'dashboard', loadComponent: () => import('./pages/dashboard/dashboard').then(m => m.Dashboard) }
];
=======
import { Dashboard } from './pages/dashboard/dashboard';

export const routes: Routes = [
  
  { path: 'auth', component: AuthComponent }, 
  { path: 'dashboard', component: Dashboard },

  { path: '', redirectTo: '/auth', pathMatch: 'full' }, 
];
>>>>>>> feature/Dashboard_interfaz
