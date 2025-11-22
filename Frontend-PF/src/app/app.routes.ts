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
      path: 'conversations',
      loadComponent: () =>
        import('./pages/dashboard/conversations/conversations')
          .then(m => m.Conversations)
    },

    {
      path: 'chat/:contactId',
      loadComponent: () =>
        import('./pages/dashboard/chat-thread/chat-thread')
          .then(m => m.ChatThread)
    },

    // por defecto, que abra la lista de conversaciones
    { path: '', redirectTo: 'conversations', pathMatch: 'full' }
  ]
},


  { path: '', redirectTo: 'auth', pathMatch: 'full' }
];
