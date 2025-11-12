import { Routes } from '@angular/router';
import { LoginComponent } from './pages/auth/login/login';
import { RegisterComponent } from './pages/auth/register/register';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  
  // Redirige a login por defecto
  { path: '', redirectTo: '/login', pathMatch: 'full' }, 
  
  // Otras rutas de tu aplicación...
  // { path: 'dashboard', component: DashboardComponent }, 
];