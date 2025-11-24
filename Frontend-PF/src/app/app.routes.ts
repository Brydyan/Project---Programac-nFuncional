import { Routes } from '@angular/router';
import { AuthComponent } from './pages/auth/auth';


export const routes: Routes = [

  { path: 'auth', component: AuthComponent },


  {
  path: 'dashboard',
  loadComponent: () =>
    import('./pages/dashboard/dashboard').then(m => m.Dashboard),
   children: [
    {
      path: '',
      loadComponent: () =>
        import('./pages/dashboard/welcome/welcome')
          .then(m => m.DashboardWelcome)   
    },
    {
      path: 'conversations',
      loadComponent: () =>
        import('./pages/dashboard/conversations/conversations')
          .then(m => m.Conversations)
    },
    {
      path: 'chat/:id',
      loadComponent: () =>
        import('./pages/dashboard/chat-thread/chat-thread')
          .then(m => m.ChatThread)
    },
  ]
},


  { path: '', redirectTo: 'auth', pathMatch: 'full' }
];
