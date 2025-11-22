import { Routes } from '@angular/router';
import { AuthComponent } from './pages/auth/auth';
import { Dashboard } from './pages/dashboard/dashboard';

export const routes: Routes = [
  
  { path: 'auth', component: AuthComponent }, 
  { path: 'dashboard', component: Dashboard },

  { path: '', redirectTo: '/auth', pathMatch: 'full' }, 
];
